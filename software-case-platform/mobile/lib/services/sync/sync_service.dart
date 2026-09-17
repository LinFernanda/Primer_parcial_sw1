import 'dart:async';
import '../../core/errors/app_exception.dart';
import '../../data/local/dao/cliente_dao.dart';
import '../../data/local/dao/reserva_dao.dart';
import '../../data/local/dao/servicio_dao.dart';
import '../../data/local/dao/sync_error_dao.dart';
import '../../data/local/dao/sync_queue_dao.dart';
import '../../data/local/entities/cliente_local.dart';
import '../../data/local/entities/reserva_local.dart';
import '../../data/local/entities/servicio_local.dart';
import '../../data/local/entities/sync_error_entity.dart';
import '../../data/local/entities/sync_operation_entity.dart';
import '../../data/local/entities/sync_status.dart';
import '../../models/cliente.dart';
import '../../models/reserva.dart';
import '../../models/servicio.dart';
import '../cliente_service.dart';
import '../connectivity/connectivity_service.dart';
import '../reserva_service.dart';
import '../servicio_service.dart';
import 'conflict_resolution.dart';

class SyncReport {
  final int processedCount;
  final int successCount;
  final int errorCount;
  final int conflictsResolved;
  final DateTime timestamp;
  final String message;

  const SyncReport({
    required this.processedCount,
    required this.successCount,
    required this.errorCount,
    required this.conflictsResolved,
    required this.timestamp,
    required this.message,
  });

  bool get isSuccess => errorCount == 0;
}

class SyncService {
  final SyncQueueDao syncQueueDao;
  final SyncErrorDao syncErrorDao;
  final ClienteDao clienteDao;
  final ReservaDao reservaDao;
  final ServicioDao servicioDao;
  final ClienteService clienteService;
  final ReservaService reservaService;
  final ServicioService servicioService;
  final ConnectivityService connectivityService;
  final ConflictStrategy conflictStrategy;

  bool _isSyncing = false;
  StreamSubscription<ConnectivityStatus>? _connectivitySub;
  final _syncStreamController = StreamController<SyncReport>.broadcast();

  bool get isSyncing => _isSyncing;
  Stream<SyncReport> get onSyncCompleted => _syncStreamController.stream;

  SyncService({
    required this.syncQueueDao,
    required this.syncErrorDao,
    required this.clienteDao,
    required this.reservaDao,
    required this.servicioDao,
    required this.clienteService,
    required this.reservaService,
    required this.servicioService,
    required this.connectivityService,
    this.conflictStrategy = ConflictStrategy.lastWriteWins,
    bool autoStart = true,
  }) {
    if (autoStart) {
      startAutoSync();
    }
  }

  void startAutoSync() {
    _connectivitySub?.cancel();
    _connectivitySub = connectivityService.onStatusChange.listen((status) {
      if (status == ConnectivityStatus.online && !_isSyncing) {
        synchronize();
      }
    });
  }

  Future<SyncReport> synchronize() async {
    if (_isSyncing) {
      return SyncReport(
        processedCount: 0,
        successCount: 0,
        errorCount: 0,
        conflictsResolved: 0,
        timestamp: DateTime.now(),
        message: 'La sincronización ya está en curso.',
      );
    }

    if (!connectivityService.isOnline) {
      return SyncReport(
        processedCount: 0,
        successCount: 0,
        errorCount: 0,
        conflictsResolved: 0,
        timestamp: DateTime.now(),
        message: 'No hay conexión a internet para sincronizar.',
      );
    }

    _isSyncing = true;
    int processed = 0;
    int successes = 0;
    int errors = 0;
    int conflicts = 0;

    try {
      final pendingOps = await syncQueueDao.getAllPending();

      for (final op in pendingOps) {
        processed++;
        final ok = await _processOperation(op);
        if (ok) {
          successes++;
          await syncQueueDao.dequeue(op.idOperacion);
        } else {
          errors++;
          final updatedOp = op.copyWith(
            reintentos: op.reintentos + 1,
            ultimoError: 'Fallo al procesar operación ${op.tipoOperacion.name} en ${op.entidad}',
          );
          await syncQueueDao.update(updatedOp);
        }
      }

      // Sincronización descendente (Servidor -> Local) con resolución de conflictos
      conflicts = await _pullRemoteChanges();

      final report = SyncReport(
        processedCount: processed,
        successCount: successes,
        errorCount: errors,
        conflictsResolved: conflicts,
        timestamp: DateTime.now(),
        message: errors == 0
            ? 'Sincronización completada con éxito. $successes cambios sincronizados.'
            : 'Sincronización finalizada con $errors errores de $processed operaciones.',
      );

      _syncStreamController.add(report);
      return report;
    } finally {
      _isSyncing = false;
    }
  }

  Future<bool> _processOperation(SyncOperationEntity op) async {
    try {
      switch (op.entidad.toUpperCase()) {
        case 'CLIENTE':
          return await _syncClienteOperation(op);
        case 'RESERVA':
          return await _syncReservaOperation(op);
        case 'SERVICIO':
          return await _syncServicioOperation(op);
        default:
          throw AppException('Entidad desconocida para sincronización: ${op.entidad}');
      }
    } catch (e) {
      // Registrar en SyncErrorLog
      await syncErrorDao.logError(
        SyncErrorEntity(
          id: 'err-${DateTime.now().millisecondsSinceEpoch}',
          idOperacion: op.idOperacion,
          operacion: op.tipoOperacion.name,
          entidad: op.entidad,
          mensaje: e.toString(),
          detalles: 'Payload: ${op.datos}',
        ),
      );
      return false;
    }
  }

  // -------------------------------------------------------------
  // Procesamiento por Entidad
  // -------------------------------------------------------------
  Future<bool> _syncClienteOperation(SyncOperationEntity op) async {
    final data = op.datos;
    final idLocal = data['idLocal'] as int?;

    if (op.tipoOperacion == SyncOperationType.CREATE) {
      final remoteCliente = await clienteService.createCliente(
        Cliente(
          nombre: data['nombre'] ?? '',
          email: data['email'] ?? '',
          telefono: data['telefono'] ?? '',
        ),
      );

      if (idLocal != null && remoteCliente.id != null) {
        final local = await clienteDao.findById(idLocal);
        if (local != null) {
          await clienteDao.update(
            local.copyWith(
              idServidor: remoteCliente.id,
              estadoSincronizacion: SyncStatus.SINCRONIZADO,
            ),
          );
        }
      }
      return true;
    } else if (op.tipoOperacion == SyncOperationType.UPDATE) {
      final idServidor = data['idServidor'] as int?;
      if (idServidor != null) {
        await clienteService.updateCliente(
          idServidor,
          Cliente(
            id: idServidor,
            nombre: data['nombre'] ?? '',
            email: data['email'] ?? '',
            telefono: data['telefono'] ?? '',
          ),
        );
      }
      if (idLocal != null) {
        final local = await clienteDao.findById(idLocal);
        if (local != null) {
          await clienteDao.update(
            local.copyWith(estadoSincronizacion: SyncStatus.SINCRONIZADO),
          );
        }
      }
      return true;
    } else if (op.tipoOperacion == SyncOperationType.DELETE) {
      final idServidor = data['idServidor'] as int?;
      if (idServidor != null) {
        await clienteService.deleteCliente(idServidor);
      }
      if (idLocal != null) {
        await clienteDao.delete(idLocal);
      }
      return true;
    }
    return false;
  }

  Future<bool> _syncReservaOperation(SyncOperationEntity op) async {
    final data = op.datos;
    final idLocal = data['idLocal'] as int?;

    if (op.tipoOperacion == SyncOperationType.CREATE) {
      final remoteReserva = await reservaService.createReserva(
        Reserva(
          clienteNombre: data['clienteNombre'] ?? '',
          barberoNombre: data['barberoNombre'] ?? '',
          servicioNombre: data['servicioNombre'] ?? '',
          fechaHora: DateTime.tryParse(data['fechaHora'] ?? '') ?? DateTime.now(),
          estado: data['estado'] ?? 'CONFIRMADA',
          notas: data['notas'],
        ),
      );

      if (idLocal != null && remoteReserva.id != null) {
        final local = await reservaDao.findById(idLocal);
        if (local != null) {
          await reservaDao.update(
            local.copyWith(
              idServidor: remoteReserva.id,
              estadoSincronizacion: SyncStatus.SINCRONIZADO,
            ),
          );
        }
      }
      return true;
    } else if (op.tipoOperacion == SyncOperationType.UPDATE) {
      final idServidor = data['idServidor'] as int?;
      if (idServidor != null) {
        await reservaService.updateReserva(
          idServidor,
          Reserva(
            id: idServidor,
            clienteNombre: data['clienteNombre'] ?? '',
            barberoNombre: data['barberoNombre'] ?? '',
            servicioNombre: data['servicioNombre'] ?? '',
            fechaHora: DateTime.tryParse(data['fechaHora'] ?? '') ?? DateTime.now(),
            estado: data['estado'] ?? 'CONFIRMADA',
            notas: data['notas'],
          ),
        );
      }
      if (idLocal != null) {
        final local = await reservaDao.findById(idLocal);
        if (local != null) {
          await reservaDao.update(
            local.copyWith(estadoSincronizacion: SyncStatus.SINCRONIZADO),
          );
        }
      }
      return true;
    } else if (op.tipoOperacion == SyncOperationType.DELETE) {
      final idServidor = data['idServidor'] as int?;
      if (idServidor != null) {
        await reservaService.deleteReserva(idServidor);
      }
      if (idLocal != null) {
        await reservaDao.delete(idLocal);
      }
      return true;
    }
    return false;
  }

  Future<bool> _syncServicioOperation(SyncOperationEntity op) async {
    final data = op.datos;
    final idLocal = data['idLocal'] as int?;

    if (op.tipoOperacion == SyncOperationType.CREATE) {
      final remoteServicio = await servicioService.createServicio(
        Servicio(
          nombre: data['nombre'] ?? '',
          descripcion: data['descripcion'],
          precio: (data['precio'] as num?)?.toDouble() ?? 0.0,
          duracionMinutos: (data['duracionMinutos'] as num?)?.toInt() ?? 30,
        ),
      );

      if (idLocal != null && remoteServicio.id != null) {
        final local = await servicioDao.findById(idLocal);
        if (local != null) {
          await servicioDao.update(
            local.copyWith(
              idServidor: remoteServicio.id,
              estadoSincronizacion: SyncStatus.SINCRONIZADO,
            ),
          );
        }
      }
      return true;
    }
    return false;
  }

  // -------------------------------------------------------------
  // Pull Remoto y Resolución de Conflictos
  // -------------------------------------------------------------
  Future<int> _pullRemoteChanges() async {
    int conflicts = 0;
    try {
      final remoteClientes = await clienteService.getClientes();
      for (final remote in remoteClientes) {
        if (remote.id == null) continue;
        final local = await clienteDao.findByServerId(remote.id!);
        if (local == null) {
          // No existe localmente: insertar directamente
          await clienteDao.insert(
            ClienteLocal(
              idServidor: remote.id,
              nombre: remote.nombre,
              email: remote.email,
              telefono: remote.telefono,
              estadoSincronizacion: SyncStatus.SINCRONIZADO,
              fechaModificacion: DateTime.now(),
            ),
          );
        } else if (local.estadoSincronizacion == SyncStatus.PENDIENTE) {
          // Conflicto potencial: Registro modificado localmente mientras estaba offline
          final result = ConflictResolver.resolve(
            localTimestamp: local.fechaModificacion,
            serverTimestamp: DateTime.now().subtract(const Duration(minutes: 1)),
            localData: local.toJson(),
            serverData: remote.toJson(),
            strategy: conflictStrategy,
          );
          conflicts++;

          if (result.useServer) {
            await clienteDao.update(
              local.copyWith(
                nombre: remote.nombre,
                email: remote.email,
                telefono: remote.telefono,
                estadoSincronizacion: SyncStatus.SINCRONIZADO,
                fechaModificacion: DateTime.now(),
              ),
            );
          }
        }
      }
    } catch (_) {
      // Ignorar fallos de pull para no interrumpir el flujo
    }
    return conflicts;
  }

  void dispose() {
    _connectivitySub?.cancel();
    _syncStreamController.close();
  }
}
