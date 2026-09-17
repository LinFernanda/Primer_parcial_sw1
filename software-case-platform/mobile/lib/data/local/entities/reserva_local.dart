import 'sync_status.dart';

class ReservaLocal {
  final int? idLocal;
  final int? idServidor;
  final String clienteNombre;
  final String barberoNombre;
  final String servicioNombre;
  final DateTime fechaHora;
  final String estado;
  final String? notas;
  final SyncStatus estadoSincronizacion;
  final DateTime fechaModificacion;
  final DateTime fechaCreacion;

  ReservaLocal({
    this.idLocal,
    this.idServidor,
    required this.clienteNombre,
    required this.barberoNombre,
    required this.servicioNombre,
    required this.fechaHora,
    this.estado = 'CONFIRMADA',
    this.notas,
    this.estadoSincronizacion = SyncStatus.PENDIENTE,
    DateTime? fechaModificacion,
    DateTime? fechaCreacion,
  })  : fechaModificacion = fechaModificacion ?? DateTime.now(),
        fechaCreacion = fechaCreacion ?? DateTime.now();

  factory ReservaLocal.fromJson(Map<String, dynamic> json) {
    return ReservaLocal(
      idLocal: json['idLocal'] != null ? (json['idLocal'] as num).toInt() : null,
      idServidor: json['idServidor'] != null ? (json['idServidor'] as num).toInt() : null,
      clienteNombre: json['clienteNombre'] ?? '',
      barberoNombre: json['barberoNombre'] ?? '',
      servicioNombre: json['servicioNombre'] ?? '',
      fechaHora: json['fechaHora'] != null
          ? DateTime.tryParse(json['fechaHora']) ?? DateTime.now()
          : DateTime.now(),
      estado: json['estado'] ?? 'CONFIRMADA',
      notas: json['notas'],
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
      'clienteNombre': clienteNombre,
      'barberoNombre': barberoNombre,
      'servicioNombre': servicioNombre,
      'fechaHora': fechaHora.toIso8601String(),
      'estado': estado,
      if (notas != null) 'notas': notas,
      'estadoSincronizacion': estadoSincronizacion.toJson(),
      'fechaModificacion': fechaModificacion.toIso8601String(),
      'fechaCreacion': fechaCreacion.toIso8601String(),
    };
  }

  ReservaLocal copyWith({
    int? idLocal,
    int? idServidor,
    String? clienteNombre,
    String? barberoNombre,
    String? servicioNombre,
    DateTime? fechaHora,
    String? estado,
    String? notas,
    SyncStatus? estadoSincronizacion,
    DateTime? fechaModificacion,
    DateTime? fechaCreacion,
  }) {
    return ReservaLocal(
      idLocal: idLocal ?? this.idLocal,
      idServidor: idServidor ?? this.idServidor,
      clienteNombre: clienteNombre ?? this.clienteNombre,
      barberoNombre: barberoNombre ?? this.barberoNombre,
      servicioNombre: servicioNombre ?? this.servicioNombre,
      fechaHora: fechaHora ?? this.fechaHora,
      estado: estado ?? this.estado,
      notas: notas ?? this.notas,
      estadoSincronizacion: estadoSincronizacion ?? this.estadoSincronizacion,
      fechaModificacion: fechaModificacion ?? DateTime.now(),
      fechaCreacion: fechaCreacion ?? this.fechaCreacion,
    );
  }
}
