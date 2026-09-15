# Guía de Configuración y Ejecución del Backend

## 1. Requisitos Previos
- **Java Development Kit (JDK):** Versión 21 LTS (Eclipse Adoptium / OpenJDK).
- **Apache Maven:** 3.9+ (o utilizar el wrapper `mvnw`).
- **PostgreSQL:** Versión 15+ o 16+ en ejecución.

## 2. Variables de Entorno (.env)
Asegurar que el archivo `.env` o `backend/.env` contenga los parámetros correctos:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=case_platform_db
DB_USER=postgres
DB_PASSWORD=password
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

## 3. Base de Datos
Crear la base de datos y ejecutar el script inicial:

```bash
psql -U postgres -d postgres -c "CREATE DATABASE case_platform_db;"
psql -U postgres -d case_platform_db -f ../database/init.sql
```

## 4. Compilación y Pruebas
```bash
cd backend
mvn clean compile
mvn test
```

## 5. Ejecución del Servidor
```bash
mvn spring-boot:run
```

El servidor quedará disponible en `http://localhost:8080`.

## 6. Endpoints de Verificación
- `GET http://localhost:8080/api/v1/ping` -> Retorna mensaje "pong".
- `GET http://localhost:8080/api/v1/health` -> Retorna estado UP, conexión a base de datos y memoria.
- `GET http://localhost:8080/api/v1/info` -> Retorna metadatos de la plataforma.
