import '../config/environment_config.dart';

class ApiConstants {
  // URLs por defecto dinámicas según entorno (dev, staging, prod en AWS)
  static String get defaultUrl => EnvironmentConfig.apiBaseUrl;
  static const String defaultLocalhostUrl = 'http://localhost:8080/api/v1';
  static const String defaultAndroidEmulatorUrl = 'http://10.0.2.2:8080/api/v1';
  static const String defaultProductionAwsUrl = 'https://api.caseplatform.com/api/v1';

  // Rutas de autenticación
  static const String authLogin = '/auth/login';
  static const String authRegister = '/auth/register';

  // Rutas de entidades generadas
  static const String clientes = '/clientes';
  static const String barberos = '/barberos';
  static const String servicios = '/servicios';
  static const String reservas = '/reservas';
  static const String pagos = '/pagos';

  // Timeout por defecto
  static const Duration timeout = Duration(seconds: 8);

  // Claves de SharedPreferences
  static const String prefBaseUrlKey = 'case_platform_base_url';
  static const String prefTokenKey = 'case_platform_jwt_token';
  static const String prefUserKey = 'case_platform_user_data';
  static const String prefAiModeKey = 'case_platform_ai_mode';
}
