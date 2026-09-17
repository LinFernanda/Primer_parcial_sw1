enum SyncOperationType {
  CREATE,
  UPDATE,
  DELETE;

  String toJson() => name;

  static SyncOperationType fromJson(String? value) {
    if (value == null) return SyncOperationType.CREATE;
    try {
      return SyncOperationType.values.byName(value.toUpperCase());
    } catch (_) {
      return SyncOperationType.CREATE;
    }
  }
}

class SyncOperationEntity {
  final String idOperacion;
  final SyncOperationType tipoOperacion;
  final String entidad; // 'CLIENTE', 'RESERVA', 'SERVICIO'
  final Map<String, dynamic> datos;
  final DateTime fecha;
  final String usuario;
  final int reintentos;
  final String? ultimoError;

  SyncOperationEntity({
    required this.idOperacion,
    required this.tipoOperacion,
    required this.entidad,
    required this.datos,
    DateTime? fecha,
    this.usuario = 'sistema',
    this.reintentos = 0,
    this.ultimoError,
  }) : fecha = fecha ?? DateTime.now();

  factory SyncOperationEntity.fromJson(Map<String, dynamic> json) {
    return SyncOperationEntity(
      idOperacion: json['idOperacion'] ?? '',
      tipoOperacion: SyncOperationType.fromJson(json['tipoOperacion']),
      entidad: json['entidad'] ?? '',
      datos: json['datos'] is Map ? Map<String, dynamic>.from(json['datos']) : {},
      fecha: json['fecha'] != null
          ? DateTime.tryParse(json['fecha']) ?? DateTime.now()
          : DateTime.now(),
      usuario: json['usuario'] ?? 'sistema',
      reintentos: json['reintentos'] != null ? (json['reintentos'] as num).toInt() : 0,
      ultimoError: json['ultimoError'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'idOperacion': idOperacion,
      'tipoOperacion': tipoOperacion.toJson(),
      'entidad': entidad,
      'datos': datos,
      'fecha': fecha.toIso8601String(),
      'usuario': usuario,
      'reintentos': reintentos,
      if (ultimoError != null) 'ultimoError': ultimoError,
    };
  }

  SyncOperationEntity copyWith({
    String? idOperacion,
    SyncOperationType? tipoOperacion,
    String? entidad,
    Map<String, dynamic>? datos,
    DateTime? fecha,
    String? usuario,
    int? reintentos,
    String? ultimoError,
  }) {
    return SyncOperationEntity(
      idOperacion: idOperacion ?? this.idOperacion,
      tipoOperacion: tipoOperacion ?? this.tipoOperacion,
      entidad: entidad ?? this.entidad,
      datos: datos ?? this.datos,
      fecha: fecha ?? this.fecha,
      usuario: usuario ?? this.usuario,
      reintentos: reintentos ?? this.reintentos,
      ultimoError: ultimoError ?? this.ultimoError,
    );
  }
}
