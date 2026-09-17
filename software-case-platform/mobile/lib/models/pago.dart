class Pago {
  final int? id;
  final int? reservaId;
  final double monto;
  final String metodoPago; // 'EFECTIVO', 'TARJETA', 'TRANSFERENCIA'
  final DateTime fechaPago;
  final String estado; // 'PAGADO', 'PENDIENTE'

  Pago({
    this.id,
    this.reservaId,
    required this.monto,
    required this.metodoPago,
    required this.fechaPago,
    this.estado = 'PAGADO',
  });

  factory Pago.fromJson(Map<String, dynamic> json) {
    return Pago(
      id: json['id'] != null ? (json['id'] as num).toInt() : null,
      reservaId: json['reservaId'] != null ? (json['reservaId'] as num).toInt() : null,
      monto: (json['monto'] as num?)?.toDouble() ?? 0.0,
      metodoPago: json['metodoPago'] ?? 'EFECTIVO',
      fechaPago: json['fechaPago'] != null
          ? DateTime.tryParse(json['fechaPago'].toString()) ?? DateTime.now()
          : DateTime.now(),
      estado: json['estado'] ?? 'PAGADO',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      if (reservaId != null) 'reservaId': reservaId,
      'monto': monto,
      'metodoPago': metodoPago,
      'fechaPago': fechaPago.toIso8601String(),
      'estado': estado,
    };
  }
}
