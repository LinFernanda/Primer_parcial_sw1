import 'sync_status.dart';

class ServicioLocal {
  final int? idLocal;
  final int? idServidor;
  final String nombre;
  final String? descripcion;
  final double precio;
  final int duracionMinutos;
  final SyncStatus estadoSincronizacion;
  final DateTime fechaModificacion;
  final DateTime fechaCreacion;

  ServicioLocal({
    this.idLocal,
    this.idServidor,
    required this.nombre,
    this.descripcion,
    required this.precio,
    required this.duracionMinutos,
    this.estadoSincronizacion = SyncStatus.PENDIENTE,
    DateTime? fechaModificacion,
    DateTime? fechaCreacion,
  })  : fechaModificacion = fechaModificacion ?? DateTime.now(),
        fechaCreacion = fechaCreacion ?? DateTime.now();

  factory ServicioLocal.fromJson(Map<String, dynamic> json) {
    return ServicioLocal(
      idLocal: json['idLocal'] != null ? (json['idLocal'] as num).toInt() : null,
      idServidor: json['idServidor'] != null ? (json['idServidor'] as num).toInt() : null,
      nombre: json['nombre'] ?? '',
      descripcion: json['descripcion'],
      precio: json['precio'] != null ? (json['precio'] as num).toDouble() : 0.0,
      duracionMinutos: json['duracionMinutos'] != null ? (json['duracionMinutos'] as num).toInt() : 30,
      estadoSincronizacion: SyncStatus.fromJson(json['estadoSincronizacion']),
      fechaModificacion: json['fechaModificacion'] != null
          ? DateTime.tryParse(json['fechaModificacion']) ?? DateTime.now()
          : DateTime.now(),
      fechaCreacion: json['fechaCreacion'] != null
          ? DateTime.tryParse(json['fechaCreacion']) ?? DateTime.now()
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (idLocal != null) 'idLocal': idLocal,
      if (idServidor != null) 'idServidor': idServidor,
      'nombre': nombre,
      if (descripcion != null) 'descripcion': descripcion,
      'precio': precio,
      'duracionMinutos': duracionMinutos,
      'estadoSincronizacion': estadoSincronizacion.toJson(),
      'fechaModificacion': fechaModificacion.toIso8601String(),
      'fechaCreacion': fechaCreacion.toIso8601String(),
    };
  }

  ServicioLocal copyWith({
    int? idLocal,
    int? idServidor,
    String? nombre,
    String? descripcion,
    double? precio,
    int? duracionMinutos,
    SyncStatus? estadoSincronizacion,
    DateTime? fechaModificacion,
    DateTime? fechaCreacion,
  }) {
    return ServicioLocal(
      idLocal: idLocal ?? this.idLocal,
      idServidor: idServidor ?? this.idServidor,
      nombre: nombre ?? this.nombre,
      descripcion: descripcion ?? this.descripcion,
      precio: precio ?? this.precio,
      duracionMinutos: duracionMinutos ?? this.duracionMinutos,
      estadoSincronizacion: estadoSincronizacion ?? this.estadoSincronizacion,
      fechaModificacion: fechaModificacion ?? DateTime.now(),
      fechaCreacion: fechaCreacion ?? this.fechaCreacion,
    );
  }
}
