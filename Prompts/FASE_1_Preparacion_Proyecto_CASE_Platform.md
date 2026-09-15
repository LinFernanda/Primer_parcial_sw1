# FASE 1 --- Preparación del proyecto, arquitectura base y configuración inicial del entorno

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Crear la estructura inicial profesional del proyecto, configurando la
arquitectura base, repositorios, ambientes de desarrollo, backend,
frontend web, aplicación móvil, base de datos y documentación.

Esta fase no implementa funcionalidades de negocio todavía.

El objetivo es preparar una base sólida, escalable y mantenible para
construir posteriormente:

-   Editor UML colaborativo.
-   Inteligencia artificial asistente.
-   Generador automático de backend.
-   Integración con Enterprise Architect.
-   Aplicación móvil Flutter.
-   Funcionamiento offline.
-   Despliegue AWS.

------------------------------------------------------------------------

# Principios obligatorios

El desarrollo debe cumplir:

-   Código limpio.
-   Separación de responsabilidades.
-   Arquitectura escalable.
-   Buenas prácticas profesionales.
-   Uso de control de versiones.
-   Documentación técnica.
-   Configuración mediante variables de entorno.

------------------------------------------------------------------------

# 1. Crear estructura principal del proyecto

Crear la carpeta raíz:

    software-case-platform

Estructura requerida:

    software-case-platform/

    ├── backend/
    ├── frontend/
    ├── mobile/
    ├── database/
    ├── documentation/
    ├── scripts/
    └── README.md

------------------------------------------------------------------------

# 2. Configuración del control de versiones

Configurar Git.

Crear:

    .gitignore

Debe ignorar:

## Backend

    target/
    *.class
    .env

## Frontend

    node_modules/
    dist/
    .env

## Flutter

    .dart_tool/
    build/

Realizar commit inicial:

    Initial project structure

------------------------------------------------------------------------

# 3. Configuración del Backend

Crear proyecto Spring Boot.

## Tecnologías obligatorias

-   Java 21.
-   Spring Boot 3.
-   Maven.
-   PostgreSQL.

------------------------------------------------------------------------

# Dependencias iniciales

Agregar:

-   Spring Web.
-   Spring Data JPA.
-   PostgreSQL Driver.
-   Spring Validation.
-   Lombok.
-   Spring Security.
-   Spring Boot DevTools.

------------------------------------------------------------------------

# Estructura del backend

Crear:

    backend/src/main/java/com/caseplatform/

    ├── config/
    ├── controller/
    ├── service/
    ├── repository/
    ├── model/
    ├── dto/
    ├── exception/
    └── CasePlatformApplication.java

------------------------------------------------------------------------

# Responsabilidad de capas

## Controller

Responsable de:

-   Recibir solicitudes HTTP.
-   Exponer APIs REST.

## Service

Responsable de:

-   Lógica de negocio.

## Repository

Responsable de:

-   Acceso a base de datos.

## Model

Contendrá:

-   Entidades.
-   Objetos del dominio.

## DTO

Preparación para:

-   Separar datos internos.
-   Evitar exponer entidades directamente.

## Config

Contendrá configuraciones del sistema.

------------------------------------------------------------------------

# 4. Configuración PostgreSQL

Crear base de datos:

    case_platform_db

Configurar mediante variables de entorno.

Crear:

    .env

Ejemplo:

    DB_HOST=localhost
    DB_PORT=5432
    DB_NAME=case_platform_db
    DB_USER=postgres
    DB_PASSWORD=password

Nunca colocar credenciales directamente en código.

------------------------------------------------------------------------

# 5. Configuración Frontend Web

Crear aplicación utilizando:

-   React.
-   TypeScript.
-   Vite.

Estructura:

    frontend/

    src/

    ├── components/
    ├── pages/
    ├── services/
    ├── hooks/
    ├── routes/
    ├── models/
    └── App.tsx

Instalar:

-   Axios.

Motivo:

Será utilizado posteriormente para comunicación con APIs REST.

------------------------------------------------------------------------

# 6. Configuración aplicación móvil

Crear aplicación Flutter.

Tecnologías:

-   Flutter.
-   Dart.

Estructura:

    lib/

    ├── screens/
    ├── widgets/
    ├── services/
    ├── models/
    ├── providers/
    └── main.dart

Preparar estructura para:

-   Consumo de APIs.
-   Base local.
-   Manejo de estado.

------------------------------------------------------------------------

# 7. Configuración de documentación

Crear:

    documentation/

    ├── architecture/
    ├── requirements/
    ├── diagrams/
    └── manuals/

Crear README principal con:

-   Descripción del proyecto.
-   Tecnologías utilizadas.
-   Arquitectura inicial.

------------------------------------------------------------------------

# 8. Configuración de calidad inicial

Configurar:

## Backend

-   Formato de código.
-   Manejo básico de excepciones.
-   Sistema de logs.

## Frontend

-   ESLint.
-   Prettier.

## Flutter

-   Dart formatter.

------------------------------------------------------------------------

# 9. Validación de funcionamiento

El agente debe comprobar:

## Backend

Ejecutar:

    mvn spring-boot:run

Resultado esperado:

Servidor iniciado correctamente.

------------------------------------------------------------------------

## Frontend

Ejecutar:

    npm run dev

Resultado esperado:

Aplicación React ejecutándose.

------------------------------------------------------------------------

## Flutter

Ejecutar:

    flutter run

Resultado esperado:

Aplicación inicial funcionando.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase se considera terminada únicamente si:

## Proyecto

-   La estructura está creada.
-   Git está configurado.

## Backend

-   Spring Boot inicia correctamente.
-   PostgreSQL está configurado.
-   Arquitectura por capas creada.

## Frontend

-   React ejecuta correctamente.
-   TypeScript configurado.

## Mobile

-   Flutter ejecuta correctamente.

## Documentación

-   README creado.
-   Carpetas documentales preparadas.

------------------------------------------------------------------------

# Resultado final esperado

Debe existir una base profesional del proyecto:

    CASE PLATFORM

            |
     -------------------------
     |           |           |
    Backend   Frontend    Mobile

            |
        PostgreSQL

            |
     Documentation

------------------------------------------------------------------------

# Restricción de esta fase

No desarrollar todavía:

-   Editor UML.
-   IA.
-   Colaboración.
-   Generación de código.
-   Enterprise Architect.
-   Sincronización offline.

Esta fase únicamente prepara la infraestructura necesaria para el
desarrollo posterior.
