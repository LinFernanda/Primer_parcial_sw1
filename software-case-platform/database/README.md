# Database Architecture - CASE Platform

Este directorio contiene los scripts de inicialización, migraciones y configuración para la base de datos de la plataforma CASE.

## Requisitos
- **Motor:** PostgreSQL 16+ (compatible con 15+)
- **Base de datos:** `case_platform_db`
- **Extensión obligatoria:** `uuid-ossp` (para identificadores UUID v4)

## Configuración de Entorno (.env)
Las variables de entorno configuradas para el acceso a datos son:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=case_platform_db
DB_USER=postgres
DB_PASSWORD=password
```

## Inicialización Manual
Para ejecutar el script de inicialización mediante `psql`:

```bash
psql -U postgres -h localhost -p 5432 -d case_platform_db -f init.sql
```

## Estructura de Tablas Inicial
- `platform_system_check`: Tabla de diagnóstico para validar la conectividad de la base de datos y la salud del sistema.
