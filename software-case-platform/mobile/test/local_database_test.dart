import 'package:flutter_test/flutter_test.dart';
import 'package:case_platform_mobile/data/local/database/app_database.dart';
import 'package:case_platform_mobile/data/local/dao/cliente_dao.dart';
import 'package:case_platform_mobile/data/local/dao/reserva_dao.dart';
import 'package:case_platform_mobile/data/local/dao/servicio_dao.dart';
import 'package:case_platform_mobile/data/local/entities/cliente_local.dart';
import 'package:case_platform_mobile/data/local/entities/reserva_local.dart';
import 'package:case_platform_mobile/data/local/entities/servicio_local.dart';
import 'package:case_platform_mobile/data/local/entities/sync_status.dart';

void main() {
  group('AppDatabase & Local DAOs Tests', () {
    late AppDatabase database;
    late ClienteDao clienteDao;
    late ReservaDao reservaDao;
    late ServicioDao servicioDao;

    setUp(() async {
      database = AppDatabase(inMemory: true);
      await database.initialize();
      clienteDao = ClienteDao(database: database);
      reservaDao = ReservaDao(database: database);
      servicioDao = ServicioDao(database: database);
    });

    test('Initializes schema with version 2 and default collections', () {
      expect(database.isInitialized, true);
      expect(database.schemaVersion, 2);
    });

    test('ClienteDao performs CRUD and filters by SyncStatus', () async {
      final nuevo = ClienteLocal(
        nombre: 'Mario Estrada',
        email: 'mario@correo.com',
        telefono: '76543210',
        estadoSincronizacion: SyncStatus.PENDIENTE,
      );

      final insertado = await clienteDao.insert(nuevo);
      expect(insertado.idLocal, isNotNull);
      expect(insertado.nombre, 'Mario Estrada');
      expect(insertado.estadoSincronizacion, SyncStatus.PENDIENTE);

      // Consulta por SyncStatus
      final pendientes = await clienteDao.findBySyncStatus(SyncStatus.PENDIENTE);
      expect(pendientes.length, 1);
      expect(pendientes.first.nombre, 'Mario Estrada');

      // Actualizar a SINCRONIZADO
      final actualizado = insertado.copyWith(
        idServidor: 101,
        estadoSincronizacion: SyncStatus.SINCRONIZADO,
      );
      final okUpdate = await clienteDao.update(actualizado);
      expect(okUpdate, true);

      final enSincronizado = await clienteDao.findBySyncStatus(SyncStatus.SINCRONIZADO);
      expect(enSincronizado.length, 1);
      expect(enSincronizado.first.idServidor, 101);

      // Eliminar
      final okDelete = await clienteDao.delete(insertado.idLocal!);
      expect(okDelete, true);
      expect(await clienteDao.count(), 0);
    });

    test('ReservaDao performs CRUD operations', () async {
      final reserva = ReservaLocal(
        clienteNombre: 'Gonzalo Pérez',
        barberoNombre: 'Mateo Fernández',
        servicioNombre: 'Corte Clásico',
        fechaHora: DateTime.now().add(const Duration(hours: 3)),
        estado: 'CONFIRMADA',
        estadoSincronizacion: SyncStatus.PENDIENTE,
      );

      final insertada = await reservaDao.insert(reserva);
      expect(insertada.idLocal, isNotNull);
      expect(insertada.clienteNombre, 'Gonzalo Pérez');

      final encontrada = await reservaDao.findById(insertada.idLocal!);
      expect(encontrada, isNotNull);
      expect(encontrada!.servicioNombre, 'Corte Clásico');
    });

    test('ServicioDao stores and retrieves services', () async {
      final servicio = ServicioLocal(
        nombre: 'Tratamiento Capilar Spa',
        descripcion: 'Masaje y exfoliación',
        precio: 45.0,
        duracionMinutos: 40,
      );

      final insertado = await servicioDao.insert(servicio);
      expect(insertado.idLocal, isNotNull);
      expect(insertado.precio, 45.0);

      final todos = await servicioDao.findAll();
      expect(todos.any((s) => s.nombre == 'Tratamiento Capilar Spa'), true);
    });
  });
}
