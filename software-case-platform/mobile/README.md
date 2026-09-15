# CASE Platform - Mobile Application

Aplicación móvil multiplataforma desarrollada en Flutter / Dart para la plataforma CASE colaborativa.

## Arquitectura por Capas
```
lib/
├── screens/       # Pantallas de la aplicación (UI principal)
├── widgets/       # Componentes visuales reutilizables
├── services/      # Servicios de comunicación API REST y almacenamiento local
├── models/        # Modelos y entidades de datos tipadas
├── providers/     # Controladores de estado reactivo (ChangeNotifier/Provider)
└── main.dart      # Punto de entrada de la aplicación
```

## Preparación de Capacidades
- **Consumo de APIs REST:** `ApiService` implementa peticiones HTTP con manejo de timeouts, decodificación JSON y mapeo a DTOs.
- **Base Local / Persistencia Offline:** `LocalStorageService` implementado sobre SharedPreferences para tokens de sesión y caché de configuraciones.
- **Manejo de Estado:** `AppStateProvider` centraliza el estado del sistema, monitoreo de salud del backend y reactividad en la interfaz de usuario.

## Requisitos y Ejecución
1. Instalar Flutter SDK (3.24+).
2. Ejecutar dependencias:
   ```bash
   flutter pub get
   ```
3. Ejecutar aplicación:
   ```bash
   flutter run
   ```
4. Formatear y verificar código:
   ```bash
   dart format lib test
   dart analyze
   ```
