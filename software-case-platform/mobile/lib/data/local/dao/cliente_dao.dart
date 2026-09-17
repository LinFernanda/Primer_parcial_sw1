import '../database/app_database.dart';
import '../entities/cliente_local.dart';
import '../entities/sync_status.dart';

class ClienteDao {
  static const String tableName = 'clientes';
  final AppDatabase database;

  ClienteDao({required this.database});

  Future<ClienteLocal> insert(ClienteLocal cliente) async {
    final row = await database.insert(tableName, cliente.toJson());
    return ClienteLocal.fromJson(row);
  }

  Future<List<ClienteLocal>> findAll() async {
    final rows = await database.findAll(tableName);
    return rows.map((r) => ClienteLocal.fromJson(r)).toList();
  }

  Future<ClienteLocal?> findById(int idLocal) async {
    final row = await database.findById(tableName, idLocal);
    if (row == null) return null;
    return ClienteLocal.fromJson(row);
  }

  Future<ClienteLocal?> findByServerId(int idServidor) async {
    final rows = await database.query(tableName, (r) => r['idServidor'] == idServidor);
    if (rows.isEmpty) return null;
    return ClienteLocal.fromJson(rows.first);
  }

  Future<List<ClienteLocal>> findBySyncStatus(SyncStatus status) async {
    final rows = await database.query(tableName, (r) => r['estadoSincronizacion'] == status.name);
    return rows.map((r) => ClienteLocal.fromJson(r)).toList();
  }

  Future<bool> update(ClienteLocal cliente) async {
    return database.update(tableName, cliente.toJson());
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
