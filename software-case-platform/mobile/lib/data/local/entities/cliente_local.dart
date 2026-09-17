import 'sync_status.dart';

class ClienteLocal {
  final int? idLocal;
  final int? idServidor;
  final String nombre;
  final String email;
  final String telefono;
  final SyncStatus estadoSincronizacion;
  final DateTime fechaModificacion;
  final DateTime fechaCreacion;

  ClienteLocal({
    this.idLocal,
    this.idServidor,
    required this.nombre,
    required this.email,
    required this.telefono,
    this.estadoSincronizacion = SyncStatus.PENDIENTE,
    DateTime? fechaModificacion,
    DateTime? fechaCreacion,
  })  : fechaModificacion = fechaModificacion ?? DateTime.now(),
        fechaCreacion = fechaCreacion ?? DateTime.now();

  factory ClienteLocal.fromJson(Map<String, dynamic> json) {
    return ClienteLocal(
      idLocal: json['idLocal'] != null ? (json['idLocal'] as num).toInt() : null,
      idServidor: json['idServidor'] != null ? (json['idServidor'] as num).toInt() : null,
      nombre: json['nombre'] ?? '',
      email: json['email'] ?? '',
      telefono: json['telefono'] ?? '',
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
      'email': email,
      'telefono': telefono,
      'estadoSincronizacion': estadoSincronizacion.toJson(),
      'fechaModificacion': fechaModificacion.toIso8601String(),
      'fechaCreacion': fechaCreacion.toIso8601String(),
    };
  }

  ClienteLocal copyWith({
    int? idLocal,
    int? idServidor,
    String? nombre,
    String? email,
    String? telefono,
    SyncStatus? estadoSincronizacion,
    DateTime? fechaModificacion,
    DateTime? fechaCreacion,
  }) {
    return ClienteLocal(
      idLocal: idLocal ?? this.idLocal,
      idServidor: idServidor ?? this.idServidor,
      nombre: nombre ?? this.nombre,
      email: email ?? this.email,
      telefono: telefono ?? this.telefono,
      estadoSincronizacion: estadoSincronizacion ?? this.estadoSincronizacion,
      fechaModificacion: fechaModificacion ?? DateTime.now(),
      fechaCreacion: fechaCreacion ?? this.fechaCreacion,
    );
  }
}
