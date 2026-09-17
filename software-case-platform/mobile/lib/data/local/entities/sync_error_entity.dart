class SyncErrorEntity {
  final String id;
  final String idOperacion;
  final String operacion;
  final String entidad;
  final DateTime fecha;
  final String mensaje;
  final String estado; // 'ACTIVO', 'RESUELTO', 'IGNORADO'
  final String? detalles;

  SyncErrorEntity({
    required this.id,
    required this.idOperacion,
    required this.operacion,
    required this.entidad,
    DateTime? fecha,
    required this.mensaje,
    this.estado = 'ACTIVO',
    this.detalles,
  }) : fecha = fecha ?? DateTime.now();

  factory SyncErrorEntity.fromJson(Map<String, dynamic> json) {
    return SyncErrorEntity(
      id: json['id'] ?? '',
      idOperacion: json['idOperacion'] ?? '',
      operacion: json['operacion'] ?? '',
      entidad: json['entidad'] ?? '',
      fecha: json['fecha'] != null
          ? DateTime.tryParse(json['fecha']) ?? DateTime.now()
          : DateTime.now(),
      mensaje: json['mensaje'] ?? '',
      estado: json['estado'] ?? 'ACTIVO',
      detalles: json['detalles'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'idOperacion': idOperacion,
      'operacion': operacion,
      'entidad': entidad,
      'fecha': fecha.toIso8601String(),
      'mensaje': mensaje,
      'estado': estado,
      if (detalles != null) 'detalles': detalles,
    };
  }

  SyncErrorEntity copyWith({
    String? id,
    String? idOperacion,
    String? operacion,
    String? entidad,
    DateTime? fecha,
    String? mensaje,
    String? estado,
    String? detalles,
  }) {
    return SyncErrorEntity(
      id: id ?? this.id,
      idOperacion: idOperacion ?? this.idOperacion,
      operacion: operacion ?? this.operacion,
      entidad: entidad ?? this.entidad,
      fecha: fecha ?? this.fecha,
      mensaje: mensaje ?? this.mensaje,
      estado: estado ?? this.estado,
      detalles: detalles ?? this.detalles,
    );
  }
}
