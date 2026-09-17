enum SyncStatus {
  PENDIENTE,
  SINCRONIZADO,
  ERROR;

  String toJson() => name;

  static SyncStatus fromJson(String? value) {
    if (value == null) return SyncStatus.PENDIENTE;
    try {
      return SyncStatus.values.byName(value.toUpperCase());
    } catch (_) {
      return SyncStatus.PENDIENTE;
    }
  }
}
