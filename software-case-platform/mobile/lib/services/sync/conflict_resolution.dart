enum ConflictStrategy {
  lastWriteWins,
  serverWins,
  clientWins,
}

class ConflictResolutionResult {
  final bool useLocal;
  final bool useServer;
  final String reason;
  final Map<String, dynamic> resolvedData;

  const ConflictResolutionResult({
    required this.useLocal,
    required this.useServer,
    required this.reason,
    required this.resolvedData,
  });
}

class ConflictResolver {
  static ConflictResolutionResult resolve({
    required DateTime localTimestamp,
    required DateTime serverTimestamp,
    required Map<String, dynamic> localData,
    required Map<String, dynamic> serverData,
    ConflictStrategy strategy = ConflictStrategy.lastWriteWins,
  }) {
    switch (strategy) {
      case ConflictStrategy.serverWins:
        return ConflictResolutionResult(
          useLocal: false,
          useServer: true,
          reason: 'Estrategia ServerWins aplicada: Se prioriza el estado del servidor.',
          resolvedData: Map<String, dynamic>.from(serverData),
        );

      case ConflictStrategy.clientWins:
        return ConflictResolutionResult(
          useLocal: true,
          useServer: false,
          reason: 'Estrategia ClientWins aplicada: Se prioriza el cambio local del cliente.',
          resolvedData: Map<String, dynamic>.from(localData),
        );

      case ConflictStrategy.lastWriteWins:
      default:
        // Comparación de marcas de tiempo según la especificación de la Fase 12
        if (serverTimestamp.isAfter(localTimestamp)) {
          return ConflictResolutionResult(
            useLocal: false,
            useServer: true,
            reason: 'El servidor tiene una modificación más reciente ($serverTimestamp > $localTimestamp). Se mantiene el servidor.',
            resolvedData: Map<String, dynamic>.from(serverData),
          );
        } else {
          return ConflictResolutionResult(
            useLocal: true,
            useServer: false,
            reason: 'La versión local es más reciente ($localTimestamp >= $serverTimestamp). Se envía el cambio local.',
            resolvedData: Map<String, dynamic>.from(localData),
          );
        }
    }
  }
}
