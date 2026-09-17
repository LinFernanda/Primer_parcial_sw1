import '../data/local/dao/cliente_dao.dart';
import '../data/local/dao/reserva_dao.dart';
import '../data/local/dao/servicio_dao.dart';
import '../data/local/dao/sync_queue_dao.dart';
import '../data/local/entities/cliente_local.dart';
import '../data/local/entities/reserva_local.dart';
import '../data/local/entities/servicio_local.dart';
import '../data/local/entities/sync_operation_entity.dart';
import '../data/local/entities/sync_status.dart';
import '../models/barbero.dart';
import '../models/cliente.dart';
import '../models/reserva.dart';
import '../models/servicio.dart';
import '../services/barbero_service.dart';
import '../services/cliente_service.dart';
import '../services/connectivity/connectivity_service.dart';
import '../services/reserva_service.dart';
import '../services/servicio_service.dart';

class BarberiaRepository {
  final ClienteDao clienteDao;
  final ReservaDao reservaDao;
  final ServicioDao servicioDao;
  final SyncQueueDao syncQueueDao;
  final ClienteService clienteService;
  final BarberoService barberoService;
  final ServicioService servicioService;
  final ReservaService reservaService;
  final ConnectivityService connectivityService;

  // Barberos locales fijos para la demostración
  final List<Barbero> _localBarberos = [
    Barbero(id: 1, nombre: 'Mateo Fernández', especialidad: 'Degradados & Fade', calificacion: 4.9),
    Barbero(id: 2, nombre: 'Rodrigo Gómez', especialidad: 'Afeitado Clásico & Navaja', calificacion: 4.8),
    Barbero(id: 3, nombre: 'Lucas Silva', especialidad: 'Estilismo & Tijera', calificacion: 5.0),
  ];

  bool get isOffline => connectivityService.isOffline;
  set isOffline(bool value) {
    connectivityService.setForcedStatus(value ? ConnectivityStatus.offline : ConnectivityStatus.online);
  }

  void setOffline(bool val) {
    isOffline = val;
  }

  BarberiaRepository({
    required this.clienteDao,
    required this.reservaDao,
    required this.servicioDao,
    required this.syncQueueDao,
    required this.clienteService,
    required this.barberoService,
    required this.servicioService,
    required this.reservaService,
    required this.connectivityService,
  }) {
    _seedInitialDataIfEmpty();
  }

  Future<void> _seedInitialDataIfEmpty() async {
    final countClientes = await clienteDao.count();
    if (countClientes == 0) {
      await clienteDao.insert(
        ClienteLocal(
          idServidor: 1,
          nombre: 'Carlos Mendoza',
          email: 'carlos.m@correo.com',
          telefono: '+591 701-23456',
          estadoSincronizacion: SyncStatus.SINCRONIZADO,
        ),
      );
      await clienteDao.insert(
        ClienteLocal(
          idServidor: 2,
          nombre: 'Alejandro Ramos',
          email: 'alejandro.r@correo.com',
          telefono: '+591 712-34567',
          estadoSincronizacion: SyncStatus.SINCRONIZADO,
        ),
      );
      await clienteDao.insert(
        ClienteLocal(
          idServidor: 3,
          nombre: 'Diego Morales',
          email: 'diego.m@correo.com',
          telefono: '+591 723-45678',
          estadoSincronizacion: SyncStatus.SINCRONIZADO,
        ),
      );
    }

    final countServicios = await servicioDao.count();
    if (countServicios == 0) {
      await servicioDao.insert(
        ServicioLocal(
          idServidor: 1,
          nombre: 'Corte de Cabello Clásico',
          descripcion: 'Lavado, corte moderno o tradicional y peinado con pomada.',
          precio: 35.0,
          duracionMinutos: 30,
          estadoSincronizacion: SyncStatus.SINCRONIZADO,
        ),
      );
      await servicioDao.insert(
        ServicioLocal(
          idServidor: 2,
          nombre: 'Perfilado y Afeitado de Barba',
          descripcion: 'Tratamiento con toalla caliente, aceite de argán y navaja.',
          precio: 25.0,
          duracionMinutos: 25,
          estadoSincronizacion: SyncStatus.SINCRONIZADO,
        ),
      );
      await servicioDao.insert(
        ServicioLocal(
          idServidor: 3,
          nombre: 'Combo Barbershop Premium',
          descripcion: 'Corte completo + Barba spa + Mascarilla facial de carbón.',
          precio: 50.0,
          duracionMinutos: 55,
          estadoSincronizacion: SyncStatus.SINCRONIZADO,
        ),
      );
    }

    final countReservas = await reservaDao.count();
    if (countReservas == 0) {
      await reservaDao.insert(
        ReservaLocal(
          idServidor: 1,
          clienteNombre: 'Carlos Mendoza',
          barberoNombre: 'Mateo Fernández',
          servicioNombre: 'Combo Barbershop Premium',
          fechaHora: DateTime.now().add(const Duration(hours: 2)),
          estado: 'CONFIRMADA',
          notas: 'Puntual por favor',
          estadoSincronizacion: SyncStatus.SINCRONIZADO,
        ),
      );
      await reservaDao.insert(
        ReservaLocal(
          idServidor: 2,
          clienteNombre: 'Alejandro Ramos',
          barberoNombre: 'Rodrigo Gómez',
          servicioNombre: 'Corte de Cabello Clásico',
          fechaHora: DateTime.now().add(const Duration(hours: 4)),
          estado: 'PENDIENTE',
          estadoSincronizacion: SyncStatus.SINCRONIZADO,
        ),
      );
    }
  }

  // -------------------------------------------------------------
  // CLIENTES
  // -------------------------------------------------------------
  Future<void> seedInitialDataIfEmpty() => _seedInitialDataIfEmpty();

  Future<List<Cliente>> getClientes() async {
    await _seedInitialDataIfEmpty();
    if (connectivityService.isOnline) {
      try {
        final remote = await clienteService.getClientes();
        for (final r in remote) {
          if (r.id != null) {
            final existing = await clienteDao.findByServerId(r.id!);
            if (existing == null) {
              await clienteDao.insert(
                ClienteLocal(
                  idServidor: r.id,
                  nombre: r.nombre,
                  email: r.email,
                  telefono: r.telefono,
                  estadoSincronizacion: SyncStatus.SINCRONIZADO,
                ),
              );
            }
          }
        }
      } catch (_) {
        // Fallback silencioso a base de datos local
      }
    }

    final locals = await clienteDao.findAll();
    return locals
        .map((l) => Cliente(
              id: l.idServidor ?? l.idLocal,
              nombre: l.nombre,
              email: l.email,
              telefono: l.telefono,
              fechaRegistro: l.fechaCreacion.toIso8601String(),
            ))
        .toList();
  }

  Future<Cliente> createCliente(Cliente cliente) async {
    // 1. Guardar siempre en la base de datos local primero
    final nuevoLocal = ClienteLocal(
      nombre: cliente.nombre,
      email: cliente.email,
      telefono: cliente.telefono,
      estadoSincronizacion: connectivityService.isOnline ? SyncStatus.SINCRONIZADO : SyncStatus.PENDIENTE,
    );
    final insertado = await clienteDao.insert(nuevoLocal);

    if (connectivityService.isOnline) {
      try {
        final remote = await clienteService.createCliente(cliente);
        await clienteDao.update(
          insertado.copyWith(
            idServidor: remote.id,
            estadoSincronizacion: SyncStatus.SINCRONIZADO,
          ),
        );
        return remote;
      } catch (_) {
        // Si falla la red durante la llamada, encolar para sincronización
        await _enqueueOperation(
          tipo: SyncOperationType.CREATE,
          entidad: 'CLIENTE',
          datos: insertado.toJson(),
        );
        return cliente.copyWith(id: insertado.idLocal);
      }
    } else {
      // Offline: Encolar en SyncQueue
      await _enqueueOperation(
        tipo: SyncOperationType.CREATE,
        entidad: 'CLIENTE',
        datos: insertado.toJson(),
      );
      return cliente.copyWith(id: insertado.idLocal);
    }
  }

  Future<Cliente> updateCliente(int id, Cliente cliente) async {
    ClienteLocal? local = await clienteDao.findByServerId(id);
    local ??= await clienteDao.findById(id);

    if (local != null) {
      final updatedLocal = local.copyWith(
        nombre: cliente.nombre,
        email: cliente.email,
        telefono: cliente.telefono,
        estadoSincronizacion: connectivityService.isOnline ? SyncStatus.SINCRONIZADO : SyncStatus.PENDIENTE,
      );
      await clienteDao.update(updatedLocal);

      if (connectivityService.isOnline) {
        try {
          final remote = await clienteService.updateCliente(id, cliente);
          return remote;
        } catch (_) {
          await _enqueueOperation(
            tipo: SyncOperationType.UPDATE,
            entidad: 'CLIENTE',
            datos: updatedLocal.toJson(),
          );
        }
      } else {
        await _enqueueOperation(
          tipo: SyncOperationType.UPDATE,
          entidad: 'CLIENTE',
          datos: updatedLocal.toJson(),
        );
      }
    }
    return cliente;
  }

  Future<void> deleteCliente(int id) async {
    ClienteLocal? local = await clienteDao.findByServerId(id);
    local ??= await clienteDao.findById(id);

    await clienteDao.delete(id);
    if (local != null && local.idLocal != null) {
      await clienteDao.delete(local.idLocal!);
    }

    if (connectivityService.isOnline) {
      try {
        await clienteService.deleteCliente(id);
      } catch (_) {
        await _enqueueOperation(
          tipo: SyncOperationType.DELETE,
          entidad: 'CLIENTE',
          datos: {'idServidor': id, 'idLocal': local?.idLocal ?? id},
        );
      }
    } else {
      await _enqueueOperation(
        tipo: SyncOperationType.DELETE,
        entidad: 'CLIENTE',
        datos: {'idServidor': id, 'idLocal': local?.idLocal ?? id},
      );
    }
  }

  // -------------------------------------------------------------
  // SERVICIOS
  // -------------------------------------------------------------
  Future<List<Servicio>> getServicios() async {
    await _seedInitialDataIfEmpty();
    if (connectivityService.isOnline) {
      try {
        final remote = await servicioService.getServicios();
        for (final r in remote) {
          if (r.id != null) {
            final existing = await servicioDao.findByServerId(r.id!);
            if (existing == null) {
              await servicioDao.insert(
                ServicioLocal(
                  idServidor: r.id,
                  nombre: r.nombre,
                  descripcion: r.descripcion,
                  precio: r.precio,
                  duracionMinutos: r.duracionMinutos,
                  estadoSincronizacion: SyncStatus.SINCRONIZADO,
                ),
              );
            }
          }
        }
      } catch (_) {}
    }

    final locals = await servicioDao.findAll();
    return locals
        .map((l) => Servicio(
              id: l.idServidor ?? l.idLocal,
              nombre: l.nombre,
              descripcion: l.descripcion ?? '',
              precio: l.precio,
              duracionMinutos: l.duracionMinutos,
            ))
        .toList();
  }

  Future<Servicio> createServicio(Servicio servicio) async {
    final nuevoLocal = ServicioLocal(
      nombre: servicio.nombre,
      descripcion: servicio.descripcion,
      precio: servicio.precio,
      duracionMinutos: servicio.duracionMinutos,
      estadoSincronizacion: connectivityService.isOnline ? SyncStatus.SINCRONIZADO : SyncStatus.PENDIENTE,
    );
    final insertado = await servicioDao.insert(nuevoLocal);

    if (connectivityService.isOnline) {
      try {
        final remote = await servicioService.createServicio(servicio);
        await servicioDao.update(
          insertado.copyWith(
            idServidor: remote.id,
            estadoSincronizacion: SyncStatus.SINCRONIZADO,
          ),
        );
        return remote;
      } catch (_) {
        await _enqueueOperation(
          tipo: SyncOperationType.CREATE,
          entidad: 'SERVICIO',
          datos: insertado.toJson(),
        );
        return servicio.copyWith(id: insertado.idLocal);
      }
    } else {
      await _enqueueOperation(
        tipo: SyncOperationType.CREATE,
        entidad: 'SERVICIO',
        datos: insertado.toJson(),
      );
      return servicio.copyWith(id: insertado.idLocal);
    }
  }

  // -------------------------------------------------------------
  // BARBEROS
  // -------------------------------------------------------------
  Future<List<Barbero>> getBarberos() async {
    if (connectivityService.isOnline) {
      try {
        final remote = await barberoService.getBarberos();
        if (remote.isNotEmpty) return remote;
      } catch (_) {}
    }
    return List.unmodifiable(_localBarberos);
  }

  // -------------------------------------------------------------
  // RESERVAS
  // -------------------------------------------------------------
  Future<List<Reserva>> getReservas() async {
    await _seedInitialDataIfEmpty();
    if (connectivityService.isOnline) {
      try {
        final remote = await reservaService.getReservas();
        for (final r in remote) {
          if (r.id != null) {
            final existing = await reservaDao.findByServerId(r.id!);
            if (existing == null) {
              await reservaDao.insert(
                ReservaLocal(
                  idServidor: r.id,
                  clienteNombre: r.clienteNombre,
                  barberoNombre: r.barberoNombre,
                  servicioNombre: r.servicioNombre,
                  fechaHora: r.fechaHora,
                  estado: r.estado,
                  notas: r.notas,
                  estadoSincronizacion: SyncStatus.SINCRONIZADO,
                ),
              );
            }
          }
        }
      } catch (_) {}
    }

    final locals = await reservaDao.findAll();
    return locals
        .map((l) => Reserva(
              id: l.idServidor ?? l.idLocal,
              clienteNombre: l.clienteNombre,
              barberoNombre: l.barberoNombre,
              servicioNombre: l.servicioNombre,
              fechaHora: l.fechaHora,
              estado: l.estado,
              notas: l.notas,
            ))
        .toList();
  }

  Future<Reserva> createReserva(Reserva reserva) async {
    final nuevoLocal = ReservaLocal(
      clienteNombre: reserva.clienteNombre,
      barberoNombre: reserva.barberoNombre,
      servicioNombre: reserva.servicioNombre,
      fechaHora: reserva.fechaHora,
      estado: reserva.estado,
      notas: reserva.notas,
      estadoSincronizacion: connectivityService.isOnline ? SyncStatus.SINCRONIZADO : SyncStatus.PENDIENTE,
    );
    final insertado = await reservaDao.insert(nuevoLocal);

    if (connectivityService.isOnline) {
      try {
        final remote = await reservaService.createReserva(reserva);
        await reservaDao.update(
          insertado.copyWith(
            idServidor: remote.id,
            estadoSincronizacion: SyncStatus.SINCRONIZADO,
          ),
        );
        return remote;
      } catch (_) {
        await _enqueueOperation(
          tipo: SyncOperationType.CREATE,
          entidad: 'RESERVA',
          datos: insertado.toJson(),
        );
        return reserva.copyWith(id: insertado.idLocal);
      }
    } else {
      await _enqueueOperation(
        tipo: SyncOperationType.CREATE,
        entidad: 'RESERVA',
        datos: insertado.toJson(),
      );
      return reserva.copyWith(id: insertado.idLocal);
    }
  }

  Future<Reserva> updateReserva(int id, Reserva reserva) async {
    ReservaLocal? local = await reservaDao.findByServerId(id);
    local ??= await reservaDao.findById(id);

    if (local != null) {
      final updatedLocal = local.copyWith(
        estado: reserva.estado,
        fechaHora: reserva.fechaHora,
        notas: reserva.notas,
        estadoSincronizacion: connectivityService.isOnline ? SyncStatus.SINCRONIZADO : SyncStatus.PENDIENTE,
      );
      await reservaDao.update(updatedLocal);

      if (connectivityService.isOnline) {
        try {
          final remote = await reservaService.updateReserva(id, reserva);
          return remote;
        } catch (_) {
          await _enqueueOperation(
            tipo: SyncOperationType.UPDATE,
            entidad: 'RESERVA',
            datos: updatedLocal.toJson(),
          );
        }
      } else {
        await _enqueueOperation(
          tipo: SyncOperationType.UPDATE,
          entidad: 'RESERVA',
          datos: updatedLocal.toJson(),
        );
      }
    }
    return reserva;
  }

  Future<void> deleteReserva(int id) async {
    ReservaLocal? local = await reservaDao.findByServerId(id);
    local ??= await reservaDao.findById(id);

    await reservaDao.delete(id);
    if (local != null && local.idLocal != null) {
      await reservaDao.delete(local.idLocal!);
    }

    if (connectivityService.isOnline) {
      try {
        await reservaService.deleteReserva(id);
      } catch (_) {
        await _enqueueOperation(
          tipo: SyncOperationType.DELETE,
          entidad: 'RESERVA',
          datos: {'idServidor': id, 'idLocal': local?.idLocal ?? id},
        );
      }
    } else {
      await _enqueueOperation(
        tipo: SyncOperationType.DELETE,
        entidad: 'RESERVA',
        datos: {'idServidor': id, 'idLocal': local?.idLocal ?? id},
      );
    }
  }

  Future<void> _enqueueOperation({
    required SyncOperationType tipo,
    required String entidad,
    required Map<String, dynamic> datos,
  }) async {
    await syncQueueDao.enqueue(
      SyncOperationEntity(
        idOperacion: 'op-${DateTime.now().millisecondsSinceEpoch}-${tipo.name}',
        tipoOperacion: tipo,
        entidad: entidad,
        datos: datos,
      ),
    );
  }
}
