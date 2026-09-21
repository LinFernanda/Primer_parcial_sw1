# CASE Platform (Computer-Aided Software Engineering)

> **Plataforma CASE Colaborativa Inteligente para Diseño UML y Generación Automática de Software**

---

## 1. Descripción del Proyecto

**CASE Platform** es un ecosistema de ingeniería de software de última generación diseñado para acelerar y simplificar el modelado de arquitecturas de software bajo el estándar **UML 2.5**, complementado con capacidades avanzadas de:
- **Colaboración en tiempo real:** Múltiples ingenieros modelando concurrentemente sobre el mismo lienzo.
- **Asistencia con Inteligencia Artificial:** Sugerencias contextuales de diseño, generación de diagramas a partir de lenguaje natural y reconocimiento de bocetos dibujados a mano (Visión Artificial).
- **Generador de Backend Automático:** Transformación directa de modelos conceptuales UML a proyectos completos y funcionales en Spring Boot.
- **Interoperabilidad:** Importación y exportación de archivos XMI compatibles con herramientas de la industria como Enterprise Architect.
- **Ecosistema Multiplataforma:** Experiencia web de escritorio de alto rendimiento y aplicación móvil complementaria con capacidades offline.

---

## 2. Tecnologías Utilizadas

| Módulo | Tecnologías y Herramientas | Propósito |
| :--- | :--- | :--- |
| **Backend** | Java 21 LTS, Spring Boot 3.3.4, Spring Data JPA, Spring Security 6, Maven | Núcleo de servicios REST, seguridad y lógica del negocio |
| **Base de Datos**| PostgreSQL 16+, HikariCP, UUID v4 Extension | Persistencia relacional, concurrencia y atomicidad |
| **Frontend Web**| React 18, TypeScript 5, Vite 5, Axios, ESLint, Prettier | Editor visual UML y consola de gestión web |
| **Aplicación Móvil**| Flutter 3.24+, Dart 3.5+, Provider, SharedPreferences | Visualizador móvil y sincronización offline |
| **Calidad & Linters**| Checkstyle, JUnit 5, ESLint, Prettier, Dart Analyze & Format | Integridad, formato uniforme y estabilidad continua |

---

## 3. Arquitectura del Proyecto

El proyecto sigue una estricta separación de responsabilidades y modularización por capas:

```
software-case-platform/
├── backend/                  # API REST y lógica de dominio (Spring Boot 3 + Java 21)
│   ├── src/main/java/com/caseplatform/
│   │   ├── config/           # Configuraciones (Seguridad, CORS, Variables de Entorno)
│   │   ├── controller/       # Endpoints REST (Controladores HTTP)
│   │   ├── dto/              # Objetos de Transferencia de Datos y ApiResponse genérico
│   │   ├── exception/        # Manejo centralizado de excepciones (GlobalExceptionHandler)
│   │   ├── model/            # Entidades del modelo de datos y metamodelo UML
│   │   ├── repository/       # Repositorios de persistencia Spring Data JPA
│   │   ├── service/          # Lógica de negocio (Interfaces e Implementaciones)
│   │   └── CasePlatformApplication.java
│   ├── pom.xml               # Descriptor Maven de dependencias y construcción
│   └── .env                  # Variables de entorno locales
│
├── frontend/                 # Aplicación Web SPA (React + TypeScript + Vite)
│   ├── src/
│   │   ├── components/       # Componentes visuales reutilizables
│   │   ├── hooks/            # Custom hooks de React para estado y peticiones
│   │   ├── models/           # Interfaces y tipos de TypeScript
│   │   ├── pages/            # Vistas de la aplicación (Dashboard, Editor, etc.)
│   │   ├── routes/           # Enrutamiento de la aplicación
│   │   ├── services/         # Clientes Axios y comunicación con APIs REST
│   │   ├── App.tsx           # Contenedor raíz
│   │   ├── index.css         # Diseño y variables de estilo
│   │   └── main.tsx          # Punto de anclaje React DOM
│   ├── package.json          # Dependencias y scripts NPM
│   └── vite.config.ts        # Configuración del bundler y proxy
│
├── mobile/                   # Aplicación Móvil (Flutter / Dart)
│   ├── lib/
│   │   ├── models/           # Modelos de datos Dart
│   │   ├── providers/        # Gestión de estado reactivo (ChangeNotifier)
│   │   ├── screens/          # Pantallas de la aplicación móvil
│   │   ├── services/         # Servicios de red (HTTP) y persistencia local
│   │   ├── widgets/          # Widgets reutilizables
│   │   └── main.dart         # Punto de entrada de la aplicación
│   ├── pubspec.yaml          # Dependencias Flutter
│   └── analysis_options.yaml # Reglas de análisis estático y formateo
│
├── database/                 # Persistencia y esquemas relacionales
│   ├── init.sql              # Script DDL inicial para PostgreSQL
│   └── README.md             # Instrucciones de configuración de base de datos
│
├── documentation/            # Documentación de ingeniería y diseño
│   ├── architecture/         # Especificación técnica y diagramas de capas
│   ├── requirements/         # Requisitos funcionales y no funcionales
│   ├── diagrams/             # Diagramas arquitecturales en Mermaid
│   └── manuals/              # Manuales de instalación para cada subproyecto
│
├── scripts/                  # Automatización de ejecución y arranque
│   ├── start-all.bat         # Lanzador conjunto de backend y frontend
│   ├── start-backend.bat     # Lanzador individual de backend
│   ├── start-frontend.bat    # Lanzador individual de frontend
│   └── start-mobile.bat      # Lanzador de la aplicación móvil
│
├── .gitignore                # Control de versiones para backend, frontend y móvil
├── .env                      # Variables de entorno globales del proyecto
├── .env.example              # Plantilla de variables de entorno
└── README.md                 # Documento principal
```

---

## 4. Guía Rápida de Inicio

### 1. Requisitos Previos
- **Java 21 LTS**
- **Maven 3.9+**
- **Node.js 18+ / 20+**
- **PostgreSQL 15+ / 16+** (con la base `case_platform_db` creada)
- **Flutter SDK 3.24+** (opcional para el cliente móvil)

### 2. Configurar la Base de Datos
```bash
psql -U postgres -c "CREATE DATABASE case_platform_db;"
psql -U postgres -d case_platform_db -f database/init.sql
```

### 3. Iniciar el Backend
```bash
cd backend
mvn spring-boot:run
```
> El servicio iniciará en: `http://localhost:8080`  
> Verificación de salud: `http://localhost:8080/api/v1/health`

### 4. Iniciar el Frontend Web
```bash
cd frontend
npm install
npm run dev
```
> La interfaz estará disponible en: `http://localhost:5173`

### 5. Iniciar la Aplicación Móvil
```bash
cd mobile
flutter pub get
flutter run
```

---

## 5. Criterios de Calidad Implementados
- **Backend:** Manejo global de excepciones (`@RestControllerAdvice`), DTOs desacoplados, logging estructurado con Logback y pruebas unitarias de contexto.
- **Frontend:** Tipado estricto con TypeScript, interceptores de peticiones en Axios, ESLint y Prettier integrados.
- **Móvil:** Arquitectura limpia por capas, Provider para gestión de estado reactivo, análisis estático con `flutter_lints` y pruebas con `flutter test`.
- **Seguridad:** Control centralizado de credenciales mediante variables de entorno (`.env`), protección contra exposición de datos sensibles.

---

## 6. Hoja de Ruta de Fases y Alcances
Para consultar la especificación exhaustiva de alcances dividida por módulos y fases, consulte [ALCANCES_DEL_PROYECTO.md](../documentation/requirements/ALCANCES_DEL_PROYECTO.md).

- [x] **Fase 1:** Preparación del proyecto, arquitectura base y configuración inicial del entorno.
- [x] **Fase 2:** Sistema de usuarios, autenticación y seguridad (JWT / RBAC).
- [x] **Fase 3:** Núcleo UML 2.5 y modelo conceptual.
- [x] **Fase 4:** Editor visual UML 2.5.
- [x] **Fase 5:** Colaboración en tiempo real (WebSocket / STOMP).
- [x] **Fase 6:** Versiones, historial y control de cambios UML.
- [x] **Fase 7:** Agente IA para edición UML inteligente.
- [x] **Fase 8:** Conversión de imágenes a modelos UML (Visión Artificial).
- [x] **Fase 9:** Generador de backend Spring Boot desde UML.
- [x] **Fase 10:** Integración Enterprise Architect (XMI).
- [x] **Fase 11:** Aplicación móvil Flutter conectada al backend generado.
- [x] **Fase 12:** Modo offline y sincronización con IA local en móvil.
- [x] **Fase 13:** Despliegue en AWS, seguridad perimetral y entrega final.
