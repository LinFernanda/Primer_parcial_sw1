# Guía de Configuración y Ejecución de la Aplicación Móvil

## 1. Requisitos Previos
- **Flutter SDK:** 3.24+ (Dart 3.5+).
- **Emulador / Dispositivo:** Android Studio Emulator, dispositivo físico o Chrome/Windows desktop target.

## 2. Instalación de Dependencias
```bash
cd mobile
flutter pub get
```

## 3. Ejecución de la Aplicación
```bash
flutter run
```

Para seleccionar un dispositivo objetivo específico:
```bash
flutter devices
flutter run -d chrome
# o
flutter run -d windows
```

## 4. Calidad y Pruebas
- **Formateo de código Dart:**
  ```bash
  dart format lib test
  ```
- **Análisis estático:**
  ```bash
  dart analyze
  ```
- **Pruebas unitarias y de widgets:**
  ```bash
  flutter test
  ```
