class SystemHealth {
  final String status;
  final String application;
  final String version;
  final String environment;
  final String databaseStatus;

  SystemHealth({
    required this.status,
    required this.application,
    required this.version,
    required this.environment,
    required this.databaseStatus,
  });

  factory SystemHealth.fromJson(Map<String, dynamic> json) {
    return SystemHealth(
      status: json['status'] ?? 'UNKNOWN',
      application: json['application'] ?? '',
      version: json['version'] ?? '',
      environment: json['environment'] ?? '',
      databaseStatus: json['databaseStatus'] ?? 'UNKNOWN',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'status': status,
      'application': application,
      'version': version,
      'environment': environment,
      'databaseStatus': databaseStatus,
    };
  }
}
