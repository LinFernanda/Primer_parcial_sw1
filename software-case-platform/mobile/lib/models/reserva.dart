class Reserva {
  final int? id;
  final int? clienteId;
  final String clienteNombre;
  final int? barberoId;
  final String barberoNombre;
  final int? servicioId;
  final String servicioNombre;
  final DateTime fechaHora;
  final String estado; // 'PENDIENTE', 'CONFIRMADA', 'COMPLETADA', 'CANCELADA'
  final String? notas;

  Reserva({
    this.id,
    this.clienteId,
    required this.clienteNombre,
    this.barberoId,
    required this.barberoNombre,
    this.servicioId,
    required this.servicioNombre,
    required this.fechaHora,
    this.estado = 'CONFIRMADA',
    this.notas,
  });

  factory Reserva.fromJson(Map<String, dynamic> json) {
    DateTime parsedFecha = DateTime.now();
    if (json['fechaHora'] != null) {
      parsedFecha = DateTime.tryParse(json['fechaHora'].toString()) ?? DateTime.now();
    }

    return Reserva(
      id: json['id'] != null ? (json['id'] as num).toInt() : null,
      clienteId: json['clienteId'] != null ? (json['clienteId'] as num).toInt() : null,
      clienteNombre: json['clienteNombre'] ?? json['cliente']?['nombre'] ?? 'Cliente General',
      barberoId: json['barberoId'] != null ? (json['barberoId'] as num).toInt() : null,
      barberoNombre: json['barberoNombre'] ?? json['barbero']?['nombre'] ?? 'Barbero de Turno',
      servicioId: json['servicioId'] != null ? (json['servicioId'] as num).toInt() : null,
      servicioNombre: json['servicioNombre'] ?? json['servicio']?['nombre'] ?? 'Servicio General',
      fechaHora: parsedFecha,
      estado: json['estado'] ?? 'CONFIRMADA',
      notas: json['notas'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      if (clienteId != null) 'clienteId': clienteId,
      'clienteNombre': clienteNombre,
      if (barberoId != null) 'barberoId': barberoId,
      'barberoNombre': barberoNombre,
      if (servicioId != null) 'servicioId': servicioId,
      'servicioNombre': servicioNombre,
      'fechaHora': fechaHora.toIso8601String(),
      'estado': estado,
      if (notas != null) 'notas': notas,
    };
  }

  Reserva copyWith({
    int? id,
    int? clienteId,
    String? clienteNombre,
    int? barberoId,
    String? barberoNombre,
    int? servicioId,
    String? servicioNombre,
    DateTime? fechaHora,
    String? estado,
    String? notas,
  }) {
    return Reserva(
      id: id ?? this.id,
      clienteId: clienteId ?? this.clienteId,
      clienteNombre: clienteNombre ?? this.clienteNombre,
      barberoId: barberoId ?? this.barberoId,
      barberoNombre: barberoNombre ?? this.barberoNombre,
      servicioId: servicioId ?? this.servicioId,
      servicioNombre: servicioNombre ?? this.servicioNombre,
      fechaHora: fechaHora ?? this.fechaHora,
      estado: estado ?? this.estado,
      notas: notas ?? this.notas,
    );
  }
}
