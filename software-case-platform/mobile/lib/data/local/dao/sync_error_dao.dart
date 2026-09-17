import '../database/app_database.dart';
import '../entities/sync_error_entity.dart';

class SyncErrorDao {
  static const String tableName = 'sync_errors';
  final AppDatabase database;

  SyncErrorDao({required this.database});

  Future<void> logError(SyncErrorEntity error) async {
    await database.insert(
      tableName,
      error.toJson(),
      primaryKey: 'id',
    );
  }

  Future<List<SyncErrorEntity>> getActiveErrors() async {
    final rows = await database.query(tableName, (r) => r['estado'] == 'ACTIVO');
    final list = rows.map((r) => SyncErrorEntity.fromJson(r)).toList();
    list.sort((a, b) => b.fecha.compareTo(a.fecha)); // Más recientes primero
    return list;
  }

  Future<List<SyncErrorEntity>> getAll() async {
    final rows = await database.findAll(tableName);
    final list = rows.map((r) => SyncErrorEntity.fromJson(r)).toList();
    list.sort((a, b) => b.fecha.compareTo(a.fecha));
    return list;
  }

  Future<bool> resolveError(String id) async {
    final error = await database.findById(tableName, id, primaryKey: 'id');
    if (error == null) return false;
    final updated = Map<String, dynamic>.from(error);
    updated['estado'] = 'RESUELTO';
    return database.update(tableName, updated, primaryKey: 'id');
  }

  Future<int> countActive() async {
    final active = await getActiveErrors();
    return active.length;
  }

  Future<void> clear() async {
    return database.clear(tableName);
  }
}
