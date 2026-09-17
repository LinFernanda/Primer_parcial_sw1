import 'dart:convert';
import 'dart:io';
import '../migrations/migration_manager.dart';

class AppDatabase {
  static AppDatabase? _instance;
  final String? storagePath;
  final bool inMemory;
  final MigrationManager _migrationManager = MigrationManager();

  int _schemaVersion = 0;
  final Map<String, List<Map<String, dynamic>>> _tables = {};
  bool _isInitialized = false;

  int get schemaVersion => _schemaVersion;
  bool get isInitialized => _isInitialized;

  AppDatabase._internal({this.storagePath, this.inMemory = false});

  factory AppDatabase({String? storagePath, bool inMemory = false}) {
    if (inMemory || storagePath != null) {
      return AppDatabase._internal(storagePath: storagePath, inMemory: inMemory);
    }
    _instance ??= AppDatabase._internal(
      storagePath: 'case_platform_offline.json',
      inMemory: false,
    );
    return _instance!;
  }

  static void resetInstanceForTesting() {
    _instance = null;
  }

  Future<void> initialize() async {
    if (_isInitialized) return;

    if (!inMemory && storagePath != null) {
      await _loadFromFile();
    }

    _schemaVersion = await _migrationManager.runMigrations(
      tables: _tables,
      currentVersion: _schemaVersion,
    );

    _isInitialized = true;
  }

  Future<void> _loadFromFile() async {
    try {
      final file = File(storagePath!);
      if (await file.exists()) {
        final content = await file.readAsString();
        if (content.trim().isNotEmpty) {
          final decoded = jsonDecode(content);
          if (decoded is Map) {
            _schemaVersion = (decoded['_schemaVersion'] as num?)?.toInt() ?? 0;
            final tables = decoded['tables'];
            if (tables is Map) {
              tables.forEach((key, val) {
                if (val is List) {
                  _tables[key.toString()] = List<Map<String, dynamic>>.from(
                    val.map((item) => Map<String, dynamic>.from(item)),
                  );
                }
              });
            }
          }
        }
      }
    } catch (_) {
      // Si falla lectura, se inicia con estado limpio
    }
  }

  Future<void> _persist() async {
    if (inMemory || storagePath == null) return;
    try {
      final file = File(storagePath!);
      final data = {
        '_schemaVersion': _schemaVersion,
        'tables': _tables,
      };
      await file.writeAsString(jsonEncode(data), flush: true);
    } catch (_) {
      // Ignorar fallos de I/O en entornos restringidos
    }
  }

  // -----------------------------------------------------------------
  // Operaciones CRUD genéricas
  // -----------------------------------------------------------------
  Future<Map<String, dynamic>> insert(String table, Map<String, dynamic> row, {String primaryKey = 'idLocal'}) async {
    await initialize();
    _tables.putIfAbsent(table, () => []);

    final copy = Map<String, dynamic>.from(row);
    if (copy[primaryKey] == null) {
      final existing = _tables[table]!;
      int maxId = 0;
      for (final r in existing) {
        final val = r[primaryKey];
        if (val is num && val > maxId) maxId = val.toInt();
      }
      copy[primaryKey] = maxId + 1;
    }

    _tables[table]!.add(copy);
    await _persist();
    return copy;
  }

  Future<List<Map<String, dynamic>>> findAll(String table) async {
    await initialize();
    final list = _tables[table] ?? [];
    return list.map((item) => Map<String, dynamic>.from(item)).toList();
  }

  Future<Map<String, dynamic>?> findById(
    String table,
    dynamic id, {
    String primaryKey = 'idLocal',
  }) async {
    await initialize();
    final list = _tables[table] ?? [];
    for (final r in list) {
      if (r[primaryKey] == id) {
        return Map<String, dynamic>.from(r);
      }
    }
    return null;
  }

  Future<List<Map<String, dynamic>>> query(
    String table,
    bool Function(Map<String, dynamic> row) predicate,
  ) async {
    await initialize();
    final list = _tables[table] ?? [];
    return list
        .where(predicate)
        .map((item) => Map<String, dynamic>.from(item))
        .toList();
  }

  Future<bool> update(
    String table,
    Map<String, dynamic> row, {
    String primaryKey = 'idLocal',
  }) async {
    await initialize();
    final list = _tables[table] ?? [];
    final id = row[primaryKey];
    if (id == null) return false;

    final index = list.indexWhere((r) => r[primaryKey] == id);
    if (index != -1) {
      list[index] = Map<String, dynamic>.from(row);
      await _persist();
      return true;
    }
    return false;
  }

  Future<bool> delete(
    String table,
    dynamic id, {
    String primaryKey = 'idLocal',
  }) async {
    await initialize();
    final list = _tables[table] ?? [];
    final initialLength = list.length;
    list.removeWhere((r) => r[primaryKey] == id);
    final deleted = list.length < initialLength;
    if (deleted) await _persist();
    return deleted;
  }

  Future<int> count(String table) async {
    await initialize();
    return (_tables[table] ?? []).length;
  }

  Future<void> clear(String table) async {
    await initialize();
    _tables[table]?.clear();
    await _persist();
  }

  Future<void> clearAll() async {
    await initialize();
    for (final t in _tables.values) {
      t.clear();
    }
    await _persist();
  }
}
