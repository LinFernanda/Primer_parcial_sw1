import '../database/app_database.dart';
import '../entities/sync_operation_entity.dart';

class SyncQueueDao {
  static const String tableName = 'sync_queue';
  final AppDatabase database;

  SyncQueueDao({required this.database});

  Future<void> enqueue(SyncOperationEntity operation) async {
    await database.insert(
      tableName,
      operation.toJson(),
      primaryKey: 'idOperacion',
    );
  }

  Future<List<SyncOperationEntity>> getAllPending() async {
    final rows = await database.findAll(tableName);
    final ops = rows.map((r) => SyncOperationEntity.fromJson(r)).toList();
    // Ordenar cronológicamente (FIFO)
    ops.sort((a, b) => a.fecha.compareTo(b.fecha));
    return ops;
  }

  Future<SyncOperationEntity?> peek() async {
    final ops = await getAllPending();
    return ops.isNotEmpty ? ops.first : null;
  }

  Future<bool> dequeue(String idOperacion) async {
    return database.delete(tableName, idOperacion, primaryKey: 'idOperacion');
  }

  Future<bool> update(SyncOperationEntity operation) async {
    return database.update(
      tableName,
      operation.toJson(),
      primaryKey: 'idOperacion',
    );
  }

  Future<int> count() async {
    return database.count(tableName);
  }

  Future<void> clear() async {
    return database.clear(tableName);
  }
}
