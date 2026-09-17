class AuthUser {
  final int? id;
  final String email;
  final String nombreCompleto;
  final String rol;
  final String token;

  AuthUser({
    this.id,
    required this.email,
    required this.nombreCompleto,
    required this.rol,
    required this.token,
  });

  factory AuthUser.fromJson(Map<String, dynamic> json, String token) {
    return AuthUser(
      id: json['id'] != null
          ? (json['id'] is num ? (json['id'] as num).toInt() : int.tryParse(json['id'].toString()))
          : null,
      email: json['email'] ?? '',
      nombreCompleto: json['nombreCompleto'] ?? json['nombre'] ?? 'Usuario',
      rol: json['rol'] ?? 'INGENIERO',
      token: token,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'email': email,
      'nombreCompleto': nombreCompleto,
      'rol': rol,
      'token': token,
    };
  }
}
