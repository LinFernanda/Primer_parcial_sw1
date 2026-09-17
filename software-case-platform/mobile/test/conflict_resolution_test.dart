import 'package:flutter_test/flutter_test.dart';
import 'package:case_platform_mobile/services/sync/conflict_resolution.dart';

void main() {
  group('Conflict Resolution Tests', () {
    final localData = {
      'id': 1,
      'nombre': 'Carlos Mendoza Local Modificado',
      'telefono': '+591 700-11111',
    };

    final serverData = {
      'id': 1,
      'nombre': 'Carlos Mendoza Servidor Remoto',
      'telefono': '+591 700-99999',
    };

    test('LastWriteWins selects Server when server timestamp is newer', () {
      final localTime = DateTime(2026, 3, 15, 10, 0);
      final serverTime = DateTime(2026, 3, 15, 10, 5); // 5 min después

      final result = ConflictResolver.resolve(
        localTimestamp: localTime,
        serverTimestamp: serverTime,
        localData: localData,
        serverData: serverData,
        strategy: ConflictStrategy.lastWriteWins,
      );

      expect(result.useServer, true);
      expect(result.useLocal, false);
      expect(result.resolvedData['nombre'], 'Carlos Mendoza Servidor Remoto');
    });

    test('LastWriteWins selects Client when local timestamp is newer', () {
      final localTime = DateTime(2026, 3, 15, 10, 15); // 15 min después
      final serverTime = DateTime(2026, 3, 15, 10, 5);

      final result = ConflictResolver.resolve(
        localTimestamp: localTime,
        serverTimestamp: serverTime,
        localData: localData,
        serverData: serverData,
        strategy: ConflictStrategy.lastWriteWins,
      );

      expect(result.useLocal, true);
      expect(result.useServer, false);
      expect(result.resolvedData['nombre'], 'Carlos Mendoza Local Modificado');
    });

    test('ServerWins strategy always enforces server data', () {
      final localTime = DateTime(2026, 3, 15, 12, 0);
      final serverTime = DateTime(2026, 3, 15, 10, 0);

      final result = ConflictResolver.resolve(
        localTimestamp: localTime,
        serverTimestamp: serverTime,
        localData: localData,
        serverData: serverData,
        strategy: ConflictStrategy.serverWins,
      );

      expect(result.useServer, true);
      expect(result.resolvedData['nombre'], 'Carlos Mendoza Servidor Remoto');
    });

    test('ClientWins strategy always enforces local data', () {
      final localTime = DateTime(2026, 3, 15, 10, 0);
      final serverTime = DateTime(2026, 3, 15, 12, 0);

      final result = ConflictResolver.resolve(
        localTimestamp: localTime,
        serverTimestamp: serverTime,
        localData: localData,
        serverData: serverData,
        strategy: ConflictStrategy.clientWins,
      );

      expect(result.useLocal, true);
      expect(result.resolvedData['nombre'], 'Carlos Mendoza Local Modificado');
    });
  });
}
