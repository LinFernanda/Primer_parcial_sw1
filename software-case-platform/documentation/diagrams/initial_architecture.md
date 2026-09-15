# Diagrama de Arquitectura Inicial - CASE Platform

```mermaid
graph TD
    subgraph Clientes
        WEB["Frontend Web<br/>(React + TypeScript + Vite)"]
        MOB["Aplicación Móvil<br/>(Flutter + Dart)"]
    end

    subgraph Backend_SpringBoot["Backend (Spring Boot 3 / Java 21)"]
        CTRL["Capa Controller<br/>REST APIs & STOMP"]
        SEC["Capa Security<br/>JWT & CORS Filter"]
        SRV["Capa Service<br/>Lógica de Negocio"]
        REPO["Capa Repository<br/>Spring Data JPA"]
        DTO["DTOs & Mappers"]
        EXC["Global Exception Handler"]

        CTRL --> SEC
        CTRL --> SRV
        SRV --> REPO
        SRV --> DTO
        CTRL -.-> EXC
    end

    subgraph Persistencia["Persistencia de Datos"]
        DB[("PostgreSQL 16<br/>case_platform_db")]
        REPO --> DB
    end

    WEB -->|"HTTP / REST<br/>Port 8080"| CTRL
    MOB -->|"HTTP / REST<br/>Port 8080"| CTRL
```

### Flujo de Peticiones
1. Los clientes (Web o Móvil) despachan solicitudes HTTP seguras al API Gateway/Backend en `http://localhost:8080/api/v1/...`.
2. La capa **Security** valida credenciales, cabeceras CORS y tokens JWT.
3. La capa **Controller** procesa y mapea la solicitud delegando en la interfaz **Service**.
4. La capa **Service** aplica lógica de negocio y se comunica con la capa **Repository**.
5. La capa **Repository** interactúa con **PostgreSQL** mediante Hibernate / JDBC HikariCP.
6. La respuesta se retorna empaquetada en un `ApiResponse<T>` estandarizado. Si ocurre una excepción, `GlobalExceptionHandler` captura y unifica el formato del error.
