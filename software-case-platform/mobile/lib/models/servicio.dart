class Servicio {
  final int? id;
  final String nombre;
  final String descripcion;
  final double precio;
  final int duracionMinutos;

  Servicio({
    this.id,
    required this.nombre,
    required this.descripcion,
    required this.precio,
    this.duracionMinutos = 30,
  });

  factory Servicio.fromJson(Map<String, dynamic> json) {
    return Servicio(
      id: json['id'] != null ? (json['id'] as num).toInt() : null,
      nombre: json['nombre'] ?? '',
      descripcion: json['descripcion'] ?? '',
      precio: (json['precio'] as num?)?.toDouble() ?? 0.0,
      duracionMinutos: json['duracionMinutos'] != null ? (json['duracionMinutos'] as num).toInt() : 30,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'nombre': nombre,
      'descripcion': descripcion,
      'precio': precio,
      'duracionMinutos': duracionMinutos,
    };
  }

  Servicio copyWith({
    int? id,
    String? nombre,
    String? descripcion,
    double? precio,
    int? duracionMinutos,
  }) {
    return Servicio(
      id: id ?? this.id,
      nombre: nombre ?? this.nombre,
      descripcion: descripcion ?? this.descripcion,
      precio: precio ?? this.precio,
      duracionMinutos: duracionMinutos ?? this.duracionMinutos,
    );
  }
}
