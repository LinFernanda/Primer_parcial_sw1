class Cliente {
  final int? id;
  final String nombre;
  final String email;
  final String telefono;
  final String? fechaRegistro;

  Cliente({
    this.id,
    required this.nombre,
    required this.email,
    required this.telefono,
    this.fechaRegistro,
  });

  factory Cliente.fromJson(Map<String, dynamic> json) {
    return Cliente(
      id: json['id'] != null ? (json['id'] as num).toInt() : null,
      nombre: json['nombre'] ?? '',
      email: json['email'] ?? '',
      telefono: json['telefono'] ?? '',
      fechaRegistro: json['fechaRegistro'] ?? json['fechaCreacion'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'nombre': nombre,
      'email': email,
      'telefono': telefono,
      if (fechaRegistro != null) 'fechaRegistro': fechaRegistro,
    };
  }

  Cliente copyWith({
    int? id,
    String? nombre,
    String? email,
    String? telefono,
    String? fechaRegistro,
  }) {
    return Cliente(
      id: id ?? this.id,
      nombre: nombre ?? this.nombre,
      email: email ?? this.email,
      telefono: telefono ?? this.telefono,
      fechaRegistro: fechaRegistro ?? this.fechaRegistro,
    );
  }
}
