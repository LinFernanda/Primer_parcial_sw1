import 'migration.dart';

class MigrationManager {
  final List<Migration> _migrations = [
    const MigrationV1InitialSchema(),
    const MigrationV2SyncIndexes(),
  ];

  List<Migration> get migrations => List.unmodifiable(_migrations);

  Future<int> runMigrations({
    required Map<String, List<Map<String, dynamic>>> tables,
    required int currentVersion,
  }) async {
    int appliedVersion = currentVersion;
    for (final migration in _migrations) {
      if (migration.version > currentVersion) {
        await migration.up(tables);
        appliedVersion = migration.version;
      }
    }
    return appliedVersion;
  }
}
