import '../database/app_database.dart';
import '../entities/reserva_local.dart';
import '../entities/sync_status.dart';

class ReservaDao {
  static const String tableName = 'reservas';
  final AppDatabase database;

  ReservaDao({required this.database});

  Future<ReservaLocal> insert(ReservaLocal reserva) async {
    final row = await database.insert(tableName, reserva.toJson());
    return ReservaLocal.fromJson(row);
  }

  Future<List<ReservaLocal>> findAll() async {
    final rows = await database.findAll(tableName);
    return rows.map((r) => ReservaLocal.fromJson(r)).toList();
  }

  Future<ReservaLocal?> findById(int idLocal) async {
    final row = await database.findById(tableName, idLocal);
    if (row == null) return null;
    return ReservaLocal.fromJson(row);
  }

  Future<ReservaLocal?> findByServerId(int idServidor) async {
    final rows = await database.query(tableName, (r) => r['idServidor'] == idServidor);
    if (rows.isEmpty) return null;
    return ReservaLocal.fromJson(rows.first);
  }

  Future<List<ReservaLocal>> findBySyncStatus(SyncStatus status) async {
    final rows = await database.query(tableName, (r) => r['estadoSincronizacion'] == status.name);
    return rows.map((r) => ReservaLocal.fromJson(r)).toList();
  }

  Future<bool> update(ReservaLocal reserva) async {
    return database.update(tableName, reserva.toJson());
  }

  Future<bool> delete(int id) async {
    final byLocal = await database.delete(tableName, id, primaryKey: 'idLocal');
    final byServer = await database.delete(tableName, id, primaryKey: 'idServidor');
    return byLocal || byServer;
  }

  Future<int> count() async {
    return database.count(tableName);
  }

  Future<void> clear() async {
    return database.clear(tableName);
  }
}
