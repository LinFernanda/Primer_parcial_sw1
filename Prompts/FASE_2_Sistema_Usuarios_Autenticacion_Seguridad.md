# FASE 2 --- Sistema de usuarios, autenticación y seguridad base

# Proyecto

## Plataforma CASE colaborativa inteligente para diseño UML y generación automática de software

------------------------------------------------------------------------

# Instrucciones para el agente Antigravity

## Objetivo de la fase

Implementar el módulo base de identidad y seguridad de la plataforma.

Esta fase tiene como finalidad crear la infraestructura necesaria para
controlar:

-   Usuarios del sistema.
-   Roles.
-   Autenticación.
-   Autorización.
-   Protección de recursos.

Este módulo será utilizado posteriormente por:

-   Editor UML colaborativo.
-   Gestión de proyectos.
-   Inteligencia artificial.
-   Generador de código.
-   Control de permisos dentro de equipos de trabajo.

No implementar todavía funcionalidades UML ni colaboración en tiempo
real.

------------------------------------------------------------------------

# Contexto del módulo

La plataforma será utilizada por ingenieros de software que trabajarán
en proyectos colaborativos.

Cada usuario debe tener una identidad dentro del sistema para:

-   Crear proyectos.
-   Participar en diseños UML.
-   Ejecutar acciones mediante IA.
-   Compartir modelos con otros usuarios.

El sistema debe estar preparado para una arquitectura profesional y
escalable.

------------------------------------------------------------------------

# Tecnologías obligatorias

## Backend

Utilizar:

-   Java 21.
-   Spring Boot 3.
-   Spring Security.
-   Spring Data JPA.
-   PostgreSQL.
-   Maven.

------------------------------------------------------------------------

## Seguridad

Implementar:

-   JWT (JSON Web Token).
-   Encriptación de contraseñas.
-   Control basado en roles.

------------------------------------------------------------------------

# Arquitectura del módulo

Mantener la arquitectura por capas:

    Controller
          |
    Service
          |
    Repository
          |
    Entity / Model

Utilizar DTO para comunicación externa:

    Entity
      |
     Service
      |
     DTO
      |
     Controller

------------------------------------------------------------------------

# 1. Creación de entidad Usuario

Crear entidad principal:

    Usuario

Ubicación:

    model/

------------------------------------------------------------------------

## Atributos obligatorios

Crear los siguientes campos:

    id
    nombreCompleto
    email
    password
    rol
    estado
    fechaCreacion
    fechaActualizacion

------------------------------------------------------------------------

## Reglas

El campo email debe ser:

-   Único.
-   Obligatorio.
-   Utilizado como identificador de acceso.

La contraseña debe:

-   Nunca almacenarse en texto plano.
-   Guardarse utilizando hashing seguro.

------------------------------------------------------------------------

# 2. Implementación de roles

Crear sistema de roles.

Roles iniciales:

    ADMIN
    INGENIERO

------------------------------------------------------------------------

## ADMIN

Permisos:

-   Gestionar usuarios.
-   Administrar configuración general.
-   Controlar acceso.

------------------------------------------------------------------------

## INGENIERO

Permisos:

-   Crear proyectos.
-   Diseñar modelos UML.
-   Utilizar herramientas colaborativas.

------------------------------------------------------------------------

# 3. Creación del Repository

Crear:

    UsuarioRepository

Debe permitir:

-   Buscar usuario por email.
-   Verificar existencia.
-   Consultar usuarios.

------------------------------------------------------------------------

# 4. Creación de servicios

Crear:

    UsuarioService

Responsabilidades:

-   Crear usuarios.
-   Consultar usuarios.
-   Actualizar información.
-   Gestionar estados.

------------------------------------------------------------------------

No colocar lógica de negocio dentro del Controller.

------------------------------------------------------------------------

# 5. Creación de DTOs

Crear:

    dto/

Implementar:

## UsuarioRequestDTO

Utilizado para:

-   Registro.
-   Actualización.

------------------------------------------------------------------------

## UsuarioResponseDTO

Utilizado para:

-   Respuestas del sistema.

------------------------------------------------------------------------

Nunca devolver directamente la entidad Usuario.

------------------------------------------------------------------------

# 6. Implementación del registro de usuarios

Crear endpoint:

    POST /api/auth/register

Funcionalidad:

Permitir crear nuevos usuarios.

Proceso:

    Solicitud
        |
    Validación
        |
    Encriptación password
        |
    Guardar usuario
        |
    Respuesta DTO

------------------------------------------------------------------------

Validaciones:

-   Email válido.
-   Campos obligatorios.
-   Email no repetido.

------------------------------------------------------------------------

# 7. Implementación del inicio de sesión

Crear endpoint:

    POST /api/auth/login

Proceso:

    Email + Password

            |

    Validación

            |

    Generar JWT

            |

    Enviar token

------------------------------------------------------------------------

Respuesta esperada:

    {
     token,
     usuario,
     rol
    }

------------------------------------------------------------------------

# 8. Implementación JWT

Crear componentes:

    security/

    ├── JwtService
    ├── JwtAuthenticationFilter
    ├── SecurityConfig
    └── UserDetailsServiceImpl

------------------------------------------------------------------------

Responsabilidades:

## JwtService

-   Crear tokens.
-   Validar tokens.

------------------------------------------------------------------------

## Filter

-   Interceptar solicitudes.
-   Validar autenticación.

------------------------------------------------------------------------

## SecurityConfig

Configurar:

-   Rutas públicas.
-   Rutas protegidas.
-   Roles.

------------------------------------------------------------------------

# 9. Protección de endpoints

Configurar seguridad.

Ejemplo:

Públicos:

    /api/auth/register
    /api/auth/login

Protegidos:

    /api/users/**
    /api/projects/**
    /api/models/**

------------------------------------------------------------------------

# 10. Manejo de excepciones

Crear sistema básico:

    exception/

    ├── GlobalExceptionHandler
    ├── ResourceNotFoundException
    └── ValidationException

------------------------------------------------------------------------

Debe devolver respuestas controladas:

Ejemplo:

    {
     timestamp,
     status,
     message
    }

------------------------------------------------------------------------

# 11. Pruebas del módulo

Implementar pruebas:

## Registro

Validar:

-   Usuario creado correctamente.
-   Email duplicado rechazado.

------------------------------------------------------------------------

## Login

Validar:

-   Credenciales correctas.
-   Credenciales incorrectas.

------------------------------------------------------------------------

## Seguridad

Validar:

-   Acceso con token válido.
-   Rechazo sin token.

------------------------------------------------------------------------

Tecnologías:

-   JUnit.
-   Mockito.
-   Spring Boot Test.

------------------------------------------------------------------------

# 12. Documentación del módulo

Actualizar:

    documentation/

Crear:

    documentation/architecture/security.md

Debe documentar:

-   Arquitectura de seguridad.
-   Flujo JWT.
-   Roles.
-   Endpoints.

------------------------------------------------------------------------

# Criterios obligatorios para finalizar la fase

La fase se considera terminada únicamente si:

## Usuarios

✅ Se pueden registrar usuarios.

✅ Se almacenan correctamente en PostgreSQL.

✅ Password almacenada de forma segura.

------------------------------------------------------------------------

## Autenticación

✅ Login funcionando.

✅ JWT generado correctamente.

✅ Rutas protegidas funcionando.

------------------------------------------------------------------------

## Autorización

✅ Roles implementados.

✅ Permisos aplicados.

------------------------------------------------------------------------

## Calidad

✅ Código separado por capas.

✅ DTO implementados.

✅ Excepciones controladas.

✅ Pruebas ejecutadas.

------------------------------------------------------------------------

# Resultado final esperado

Al finalizar esta fase la plataforma debe contar con un sistema
profesional de identidad:

    Usuario

        |
        |
    Autenticación JWT

        |
        |
    Control de Roles

        |
        |
    Acceso seguro a módulos futuros

------------------------------------------------------------------------

# Restricciones de esta fase

No desarrollar todavía:

-   Editor UML.
-   Gestión de diagramas.
-   Colaboración WebSocket.
-   Inteligencia artificial.
-   Generador de código.
-   Enterprise Architect.
-   Aplicación móvil.

Esta fase solamente construye la base de usuarios y seguridad sobre la
cual funcionarán todos los módulos posteriores.
