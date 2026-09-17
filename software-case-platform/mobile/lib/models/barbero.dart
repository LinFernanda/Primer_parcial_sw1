class Barbero {
  final int? id;
  final String nombre;
  final String especialidad;
  final double calificacion;
  final bool activo;

  Barbero({
    this.id,
    required this.nombre,
    required this.especialidad,
    this.calificacion = 5.0,
    this.activo = true,
  });

  factory Barbero.fromJson(Map<String, dynamic> json) {
    return Barbero(
      id: json['id'] != null ? (json['id'] as num).toInt() : null,
      nombre: json['nombre'] ?? '',
      especialidad: json['especialidad'] ?? 'Estilista',
      calificacion: (json['calificacion'] as num?)?.toDouble() ?? 5.0,
      activo: json['activo'] ?? true,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'nombre': nombre,
      'especialidad': especialidad,
      'calificacion': calificacion,
      'activo': activo,
    };
  }

  Barbero copyWith({
    int? id,
    String? nombre,
    String? especialidad,
    double? calificacion,
    bool? activo,
  }) {
    return Barbero(
      id: id ?? this.id,
      nombre: nombre ?? this.nombre,
      especialidad: especialidad ?? this.especialidad,
      calificacion: calificacion ?? this.calificacion,
      activo: activo ?? this.activo,
    );
  }
}
