import 'package:flutter_test/flutter_test.dart';
import 'package:case_platform_mobile/core/api/api_client.dart';
import 'package:case_platform_mobile/data/local/database/app_database.dart';
import 'package:case_platform_mobile/data/local/dao/cliente_dao.dart';
import 'package:case_platform_mobile/data/local/dao/reserva_dao.dart';
import 'package:case_platform_mobile/data/local/dao/servicio_dao.dart';
import 'package:case_platform_mobile/data/local/dao/sync_queue_dao.dart';
import 'package:case_platform_mobile/data/local/dao/sync_error_dao.dart';
import 'package:case_platform_mobile/data/local/entities/cliente_local.dart';
import 'package:case_platform_mobile/data/local/entities/sync_operation_entity.dart';
import 'package:case_platform_mobile/data/local/entities/sync_status.dart';
import 'package:case_platform_mobile/services/cliente_service.dart';
import 'package:case_platform_mobile/services/connectivity/connectivity_service.dart';
import 'package:case_platform_mobile/services/reserva_service.dart';
import 'package:case_platform_mobile/services/servicio_service.dart';
import 'package:case_platform_mobile/services/sync/sync_service.dart';

void main() {
  group('SyncService Synchronization Engine Tests', () {
    late AppDatabase db;
    late ClienteDao clienteDao;
    late ReservaDao reservaDao;
    late ServicioDao servicioDao;
    late SyncQueueDao queueDao;
    late SyncErrorDao errorDao;
    late ConnectivityService conn;
    late SyncService syncService;

    setUp(() async {
      db = AppDatabase(inMemory: true);
      await db.initialize();
      clienteDao = ClienteDao(database: db);
      reservaDao = ReservaDao(database: db);
      servicioDao = ServicioDao(database: db);
      queueDao = SyncQueueDao(database: db);
      errorDao = SyncErrorDao(database: db);

      conn = ConnectivityService(startPolling: false);
      conn.setForcedStatus(ConnectivityStatus.online);

      final apiClient = ApiClient(baseUrl: 'http://localhost:9999/api');
      final clienteService = ClienteService(apiClient: apiClient);
      final reservaService = ReservaService(apiClient: apiClient);
      final servicioService = ServicioService(apiClient: apiClient);

      syncService = SyncService(
        syncQueueDao: queueDao,
        syncErrorDao: errorDao,
        clienteDao: clienteDao,
        reservaDao: reservaDao,
        servicioDao: servicioDao,
        clienteService: clienteService,
        reservaService: reservaService,
        servicioService: servicioService,
        connectivityService: conn,
        autoStart: false,
      );
    });

    test('Synchronize does not run when offline', () async {
      conn.setForcedStatus(ConnectivityStatus.offline);

      final report = await syncService.synchronize();
      expect(report.processedCount, 0);
      expect(report.message, contains('No hay conexión'));
    });

    test('Logs error and increments retry count when remote endpoint fails', () async {
      // Creamos un cliente local con estado PENDIENTE
      final cliente = await clienteDao.insert(
        ClienteLocal(
          nombre: 'Cliente Pendiente',
          email: 'pendiente@correo.com',
          telefono: '700-0000',
          estadoSincronizacion: SyncStatus.PENDIENTE,
        ),
      );

      // Encolamos la operación en SyncQueue
      await queueDao.enqueue(
        SyncOperationEntity(
          idOperacion: 'op-fail-1',
          tipoOperacion: SyncOperationType.CREATE,
          entidad: 'CLIENTE',
          datos: cliente.toJson(),
        ),
      );

      expect(await queueDao.count(), 1);

      // Sincronizamos (fallará debido al puerto inactivo 9999)
      final report = await syncService.synchronize();
      expect(report.processedCount, 1);
      expect(report.errorCount, 1);
      expect(report.isSuccess, false);

      // La operación debe permanecer en la cola con reintentos = 1
      final op = await queueDao.peek();
      expect(op, isNotNull);
      expect(op!.reintentos, 1);

      // Debe haberse registrado en SyncErrorLog
      expect(await errorDao.countActive(), 1);
      final error = (await errorDao.getActiveErrors()).first;
      expect(error.idOperacion, 'op-fail-1');
    });
  });
}
