import 'package:flutter/foundation.dart';

/// Enumeración de entornos de ejecución soportados
enum Environment {
  development,
  staging,
  production,
}

/// Configuración central de entornos para la aplicación móvil Flutter.
/// Soporta inyección en tiempo de compilación con --dart-define.
class EnvironmentConfig {
  static const String _envString = String.fromEnvironment('ENV', defaultValue: 'dev');
  static const String _customApiUrl = String.fromEnvironment('API_URL', defaultValue: '');

  static Environment get currentEnvironment {
    switch (_envString.toLowerCase()) {
      case 'prod':
      case 'production':
        return Environment.production;
      case 'staging':
      case 'test':
        return Environment.staging;
      case 'dev':
      case 'development':
      default:
        return Environment.development;
    }
  }

  static String get apiBaseUrl {
    // Si se especificó una URL personalizada por --dart-define, tiene prioridad máxima
    if (_customApiUrl.isNotEmpty) {
      return _customApiUrl;
    }

    switch (currentEnvironment) {
      case Environment.production:
        return 'https://api.caseplatform.com/api/v1';
      case Environment.staging:
        return 'https://staging-api.caseplatform.com/api/v1';
      case Environment.development:
      default:
        // En emulador Android estándar, localhost apunta a 10.0.2.2
        if (!kIsWeb && defaultTargetPlatform == TargetPlatform.android) {
          return 'http://10.0.2.2:8080/api/v1';
        }
        return 'http://localhost:8080/api/v1';
    }
  }

  static bool get isProduction => currentEnvironment == Environment.production;
  static bool get isDevelopment => currentEnvironment == Environment.development;
}
