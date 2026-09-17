import '../database/app_database.dart';
import '../entities/servicio_local.dart';
import '../entities/sync_status.dart';

class ServicioDao {
  static const String tableName = 'servicios';
  final AppDatabase database;

  ServicioDao({required this.database});

  Future<ServicioLocal> insert(ServicioLocal servicio) async {
    final row = await database.insert(tableName, servicio.toJson());
    return ServicioLocal.fromJson(row);
  }

  Future<List<ServicioLocal>> findAll() async {
    final rows = await database.findAll(tableName);
    return rows.map((r) => ServicioLocal.fromJson(r)).toList();
  }

  Future<ServicioLocal?> findById(int idLocal) async {
    final row = await database.findById(tableName, idLocal);
    if (row == null) return null;
    return ServicioLocal.fromJson(row);
  }

  Future<ServicioLocal?> findByServerId(int idServidor) async {
    final rows = await database.query(tableName, (r) => r['idServidor'] == idServidor);
    if (rows.isEmpty) return null;
    return ServicioLocal.fromJson(rows.first);
  }

  Future<List<ServicioLocal>> findBySyncStatus(SyncStatus status) async {
    final rows = await database.query(tableName, (r) => r['estadoSincronizacion'] == status.name);
    return rows.map((r) => ServicioLocal.fromJson(r)).toList();
  }

  Future<bool> update(ServicioLocal servicio) async {
    return database.update(tableName, servicio.toJson());
  }

  Future<bool> delete(int idLocal) async {
    return database.delete(tableName, idLocal);
  }

  Future<int> count() async {
    return database.count(tableName);
  }

  Future<void> clear() async {
    return database.clear(tableName);
  }
}
