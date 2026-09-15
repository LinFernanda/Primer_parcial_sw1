# Arquitectura del Sistema - CASE Platform

## 1. Visión General
La **Plataforma CASE Colaborativa Inteligente** es un entorno de ingeniería de software orientado a la modelación UML 2.5, colaboración multiusuario en tiempo real, asistencia mediante modelos de lenguaje (IA) y generación automatizada de código ejecutable.

```
+-------------------------------------------------------------------------+
|                                CLIENTES                                 |
+------------------------------------+------------------------------------+
|         Frontend Web (React/Vite)  |       Mobile App (Flutter)         |
|         - Editor visual UML        |       - Dashboard y visualizador   |
|         - Colaboración tiempo real |       - Sincronización offline     |
|         - Consumo de REST APIs     |       - Modo desconectado          |
+------------------------------------+------------------------------------+
                                     | (HTTP/REST + WebSocket)
                                     v
+-------------------------------------------------------------------------+
|                          BACKEND (Spring Boot 3)                        |
|                                                                         |
|  [Controller Layer]  -> Expone APIs REST, STOMP Endpoints               |
|  [Service Layer]     -> Reglas de negocio, coordinación IA y generador  |
|  [Repository Layer]  -> Spring Data JPA / Hibernate                      |
|  [Model / DTO]       -> Metamodelo UML 2.5 y aislamiento de capas       |
|  [Config Layer]      -> Seguridad JWT, CORS, WebSocket, Ambientes       |
+------------------------------------+------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                         BASE DE DATOS (PostgreSQL)                      |
|                                                                         |
|  - case_platform_db                                                     |
|  - Soporte nativo para UUIDs                                            |
|  - Esquema normalizado para proyectos, diagramas, entidades y roles     |
+-------------------------------------------------------------------------+
```

## 2. Principios de Arquitectura
- **Clean Architecture & SOLID:** Alta cohesión y bajo acoplamiento entre módulos.
- **Layered Architecture (Arquitectura por Capas):** Aislamiento estricto entre presentación (Controller), negocio (Service), persistencia (Repository) y dominio (Model).
- **Twelve-Factor App:** Configuración desacoplada mediante variables de entorno (`.env`), paridad en desarrollo/producción, procesos sin estado (Stateless).
- **Stateless REST APIs:** Seguridad basada en tokens JWT sin almacenamiento de sesión en memoria del servidor.
- **Extensibilidad:** Preparada para módulos venideros (Generador de código, Agentes IA, Enterprise Architect XMI).
