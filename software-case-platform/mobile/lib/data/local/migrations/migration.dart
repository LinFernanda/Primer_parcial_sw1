abstract class Migration {
  final int version;
  final String description;

  const Migration({required this.version, required this.description});

  Future<void> up(Map<String, List<Map<String, dynamic>>> tables);
  Future<void> down(Map<String, List<Map<String, dynamic>>> tables);
}

class MigrationV1InitialSchema extends Migration {
  const MigrationV1InitialSchema()
      : super(version: 1, description: 'Creación de colecciones iniciales offline');

  @override
  Future<void> up(Map<String, List<Map<String, dynamic>>> tables) async {
    tables.putIfAbsent('clientes', () => []);
    tables.putIfAbsent('reservas', () => []);
    tables.putIfAbsent('servicios', () => []);
    tables.putIfAbsent('sync_queue', () => []);
    tables.putIfAbsent('sync_errors', () => []);
  }

  @override
  Future<void> down(Map<String, List<Map<String, dynamic>>> tables) async {
    tables.clear();
  }
}

class MigrationV2SyncIndexes extends Migration {
  const MigrationV2SyncIndexes()
      : super(version: 2, description: 'Campos de auditoría y resolución de conflictos');

  @override
  Future<void> up(Map<String, List<Map<String, dynamic>>> tables) async {
    // Asegurar que todos los registros existentes cuenten con estadoSincronizacion
    for (final collection in ['clientes', 'reservas', 'servicios']) {
      final list = tables[collection] ?? [];
      for (final item in list) {
        item.putIfAbsent('estadoSincronizacion', () => 'SINCRONIZADO');
        item.putIfAbsent('fechaModificacion', () => DateTime.now().toIso8601String());
      }
    }
  }

  @override
  Future<void> down(Map<String, List<Map<String, dynamic>>> tables) async {
    // Revert logic if necessary
  }
}
