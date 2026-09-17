import 'package:flutter_test/flutter_test.dart';
import 'package:case_platform_mobile/data/local/database/app_database.dart';
import 'package:case_platform_mobile/data/local/dao/sync_queue_dao.dart';
import 'package:case_platform_mobile/data/local/dao/sync_error_dao.dart';
import 'package:case_platform_mobile/data/local/entities/sync_operation_entity.dart';
import 'package:case_platform_mobile/data/local/entities/sync_error_entity.dart';

void main() {
  group('SyncQueueDao & SyncErrorDao Tests', () {
    late AppDatabase database;
    late SyncQueueDao queueDao;
    late SyncErrorDao errorDao;

    setUp(() async {
      database = AppDatabase(inMemory: true);
      await database.initialize();
      queueDao = SyncQueueDao(database: database);
      errorDao = SyncErrorDao(database: database);
    });

    test('Enqueue and dequeue maintains chronological FIFO ordering', () async {
      final now = DateTime.now();
      final op1 = SyncOperationEntity(
        idOperacion: 'op-1',
        tipoOperacion: SyncOperationType.CREATE,
        entidad: 'CLIENTE',
        datos: {'nombre': 'Primer Cliente'},
        fecha: now.subtract(const Duration(minutes: 5)),
      );

      final op2 = SyncOperationEntity(
        idOperacion: 'op-2',
        tipoOperacion: SyncOperationType.CREATE,
        entidad: 'RESERVA',
        datos: {'clienteNombre': 'Segundo Cliente'},
        fecha: now,
      );

      await queueDao.enqueue(op2);
      await queueDao.enqueue(op1);

      expect(await queueDao.count(), 2);

      // El primero en la cola debe ser op1 por ser más antiguo
      final primerOp = await queueDao.peek();
      expect(primerOp, isNotNull);
      expect(primerOp!.idOperacion, 'op-1');

      // Dequeue op1
      final ok = await queueDao.dequeue('op-1');
      expect(ok, true);
      expect(await queueDao.count(), 1);

      final siguienteOp = await queueDao.peek();
      expect(siguienteOp!.idOperacion, 'op-2');
    });

    test('SyncErrorDao records errors and marks them resolved', () async {
      final error = SyncErrorEntity(
        id: 'err-01',
        idOperacion: 'op-99',
        operacion: 'CREATE',
        entidad: 'CLIENTE',
        mensaje: 'SocketException: Connection refused (503)',
      );

      await errorDao.logError(error);
      expect(await errorDao.countActive(), 1);

      final active = await errorDao.getActiveErrors();
      expect(active.first.id, 'err-01');
      expect(active.first.estado, 'ACTIVO');

      final okResolve = await errorDao.resolveError('err-01');
      expect(okResolve, true);
      expect(await errorDao.countActive(), 0);
    });
  });
}
