import 'package:flutter_test/flutter_test.dart';
import 'package:case_platform_mobile/core/api/api_client.dart';
import 'package:case_platform_mobile/data/local/database/app_database.dart';
import 'package:case_platform_mobile/data/local/dao/cliente_dao.dart';
import 'package:case_platform_mobile/data/local/dao/reserva_dao.dart';
import 'package:case_platform_mobile/data/local/dao/servicio_dao.dart';
import 'package:case_platform_mobile/data/local/dao/sync_queue_dao.dart';
import 'package:case_platform_mobile/models/cliente.dart';
import 'package:case_platform_mobile/models/reserva.dart';
import 'package:case_platform_mobile/repositories/barberia_repository.dart';
import 'package:case_platform_mobile/services/barbero_service.dart';
import 'package:case_platform_mobile/services/cliente_service.dart';
import 'package:case_platform_mobile/services/connectivity/connectivity_service.dart';
import 'package:case_platform_mobile/services/reserva_service.dart';
import 'package:case_platform_mobile/services/servicio_service.dart';

void main() {
  group('BarberiaRepository Offline Fallback & Domain Operations', () {
    late BarberiaRepository repository;
    late AppDatabase db;
    late ConnectivityService conn;
    late SyncQueueDao queueDao;

    setUp(() async {
      db = AppDatabase(inMemory: true);
      await db.initialize();

      final clienteDao = ClienteDao(database: db);
      final reservaDao = ReservaDao(database: db);
      final servicioDao = ServicioDao(database: db);
      queueDao = SyncQueueDao(database: db);

      conn = ConnectivityService(startPolling: false);
      conn.setForcedStatus(ConnectivityStatus.offline); // Modo offline para pruebas locales

      final apiClient = ApiClient(baseUrl: 'http://localhost:9999/api');

      repository = BarberiaRepository(
        clienteDao: clienteDao,
        reservaDao: reservaDao,
        servicioDao: servicioDao,
        syncQueueDao: queueDao,
        clienteService: ClienteService(apiClient: apiClient),
        barberoService: BarberoService(apiClient: apiClient),
        servicioService: ServicioService(apiClient: apiClient),
        reservaService: ReservaService(apiClient: apiClient),
        connectivityService: conn,
      );
    });

    test('Loads initial seed cache on network failure and marks isOffline true', () async {
      final clientes = await repository.getClientes();
      expect(clientes, isNotEmpty);
      expect(repository.isOffline, true);
      expect(clientes.any((c) => c.nombre == 'Carlos Mendoza'), true);
    });

    test('Creates new client in offline cache and enqueues in SyncQueue successfully', () async {
      final nuevo = Cliente(
        nombre: 'Valeria Vargas',
        email: 'valeria@correo.com',
        telefono: '777-12345',
      );

      final creado = await repository.createCliente(nuevo);
      expect(creado.id, isNotNull);
      expect(creado.nombre, 'Valeria Vargas');
      expect(repository.isOffline, true);

      final todos = await repository.getClientes();
      expect(todos.any((c) => c.nombre == 'Valeria Vargas'), true);

      // Verificamos que se encoló en SyncQueue
      expect(await queueDao.count(), 1);
      final op = await queueDao.peek();
      expect(op, isNotNull);
      expect(op!.entidad, 'CLIENTE');
    });

    test('Creates and lists reservas in local cache', () async {
      final nuevaReserva = Reserva(
        clienteNombre: 'Andrés Castro',
        barberoNombre: 'Mateo Fernández',
        servicioNombre: 'Corte de Cabello Clásico',
        fechaHora: DateTime.now().add(const Duration(hours: 4)),
        estado: 'CONFIRMADA',
      );

      final creada = await repository.createReserva(nuevaReserva);
      expect(creada.id, isNotNull);
      expect(creada.clienteNombre, 'Andrés Castro');

      final reservas = await repository.getReservas();
      expect(reservas.any((r) => r.clienteNombre == 'Andrés Castro'), true);
    });

    test('Updates reservation state in local cache', () async {
      final reservasIniciales = await repository.getReservas();
      final id = reservasIniciales.first.id!;

      final actualizada = reservasIniciales.first.copyWith(estado: 'COMPLETADA');
      final guardada = await repository.updateReserva(id, actualizada);

      expect(guardada.estado, 'COMPLETADA');
    });

    test('Deletes client from local cache', () async {
      final clientesAntes = await repository.getClientes();
      final target = clientesAntes.first;

      await repository.deleteCliente(target.id!);

      final clientesDespues = await repository.getClientes();
      expect(clientesDespues.any((c) => c.id == target.id), false);
    });
  });
}
