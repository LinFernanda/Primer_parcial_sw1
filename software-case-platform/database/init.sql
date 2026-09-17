-- ====================================================================
-- CASE Platform - Database Initialization Script
-- Target Database: case_platform_db
-- Target RDBMS: PostgreSQL 15+ / 16+
-- ====================================================================

-- Enable UUID extension for unique identifier generation across models
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Schema creation (public is default, setting search_path)
SET search_path TO public;

-- Verification table for database connectivity check (Phase 1)
CREATE TABLE IF NOT EXISTS platform_system_check (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    check_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO platform_system_check (check_name, status)
VALUES ('SYSTEM_INIT_PHASE_1', 'SUCCESS')
ON CONFLICT DO NOTHING;

-- ====================================================================
-- FASE 2: Sistema de Usuarios, Autenticación y Seguridad
-- ====================================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('ADMIN', 'INGENIERO')),
    CONSTRAINT chk_usuarios_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO'))
);

CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON usuarios(rol);
CREATE INDEX IF NOT EXISTS idx_usuarios_estado ON usuarios(estado);

-- Usuario administrador inicial para pruebas (Password: Admin123*)
INSERT INTO usuarios (nombre_completo, email, password, rol, estado, fecha_creacion, fecha_actualizacion)
VALUES (
    'Administrador Plataforma CASE',
    'admin@caseplatform.com',
    '$2a$10$oVIGQ8JvNqcpGFFYquFlT./5QTk29TbcqsxY8RJqqBEr0ancx5IKO',
    'ADMIN',
    'ACTIVO',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- ====================================================================
-- FASE 3: Núcleo UML 2.5 y Modelo Conceptual del Sistema
-- ====================================================================

-- 1. Tabla Proyectos UML
CREATE TABLE IF NOT EXISTS proyectos_uml (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(1000),
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE,
    usuario_propietario_id BIGINT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_proyecto_usuario FOREIGN KEY (usuario_propietario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_proyectos_usuario ON proyectos_uml(usuario_propietario_id);
CREATE INDEX IF NOT EXISTS idx_proyectos_estado ON proyectos_uml(estado);

-- 2. Tabla Modelos UML
CREATE TABLE IF NOT EXISTS modelos_uml (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    version VARCHAR(30) NOT NULL DEFAULT '1.0',
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE,
    proyecto_id BIGINT NOT NULL,
    CONSTRAINT fk_modelo_proyecto FOREIGN KEY (proyecto_id)
        REFERENCES proyectos_uml(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_modelos_proyecto ON modelos_uml(proyecto_id);

-- 3. Tabla Clases UML
CREATE TABLE IF NOT EXISTS clases_uml (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    visibilidad VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    descripcion VARCHAR(500),
    posicion_x DOUBLE PRECISION DEFAULT 0.0,
    posicion_y DOUBLE PRECISION DEFAULT 0.0,
    modelo_id BIGINT NOT NULL,
    CONSTRAINT fk_clase_modelo FOREIGN KEY (modelo_id)
        REFERENCES modelos_uml(id) ON DELETE CASCADE,
    CONSTRAINT uk_clase_modelo_nombre UNIQUE (modelo_id, nombre)
);

CREATE INDEX IF NOT EXISTS idx_clases_modelo ON clases_uml(modelo_id);

-- 4. Tabla Atributos UML
CREATE TABLE IF NOT EXISTS atributos_uml (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo_dato VARCHAR(50) NOT NULL,
    visibilidad VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    valor_inicial VARCHAR(100),
    clase_id BIGINT NOT NULL,
    CONSTRAINT fk_atributo_clase FOREIGN KEY (clase_id)
        REFERENCES clases_uml(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_atributos_clase ON atributos_uml(clase_id);

-- 5. Tabla Métodos UML
CREATE TABLE IF NOT EXISTS metodos_uml (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo_retorno VARCHAR(50) NOT NULL DEFAULT 'void',
    visibilidad VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    parametros VARCHAR(255),
    clase_id BIGINT NOT NULL,
    CONSTRAINT fk_metodo_clase FOREIGN KEY (clase_id)
        REFERENCES clases_uml(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_metodos_clase ON metodos_uml(clase_id);

-- 6. Tabla Relaciones UML
CREATE TABLE IF NOT EXISTS relaciones_uml (
    id BIGSERIAL PRIMARY KEY,
    tipo_relacion VARCHAR(30) NOT NULL,
    clase_origen_id BIGINT NOT NULL,
    clase_destino_id BIGINT NOT NULL,
    cardinalidad_origen VARCHAR(20) DEFAULT '1',
    cardinalidad_destino VARCHAR(20) DEFAULT '1',
    descripcion VARCHAR(500),
    modelo_id BIGINT NOT NULL,
    CONSTRAINT fk_relacion_origen FOREIGN KEY (clase_origen_id)
        REFERENCES clases_uml(id) ON DELETE CASCADE,
    CONSTRAINT fk_relacion_destino FOREIGN KEY (clase_destino_id)
        REFERENCES clases_uml(id) ON DELETE CASCADE,
    CONSTRAINT fk_relacion_modelo FOREIGN KEY (modelo_id)
        REFERENCES modelos_uml(id) ON DELETE CASCADE,
    CONSTRAINT chk_relacion_tipo CHECK (tipo_relacion IN ('ASOCIACION', 'HERENCIA', 'DEPENDENCIA', 'AGREGACION', 'COMPOSICION'))
);

CREATE INDEX IF NOT EXISTS idx_relaciones_modelo ON relaciones_uml(modelo_id);
CREATE INDEX IF NOT EXISTS idx_relaciones_origen ON relaciones_uml(clase_origen_id);
CREATE INDEX IF NOT EXISTS idx_relaciones_destino ON relaciones_uml(clase_destino_id);

-- ====================================================================
-- FASE 6: Sistema de Versiones, Historial y Control de Cambios UML
-- ====================================================================

-- 1. Tabla Versiones de Modelo UML (Snapshots)
CREATE TABLE IF NOT EXISTS versiones_modelo (
    id BIGSERIAL PRIMARY KEY,
    numero_version VARCHAR(50) NOT NULL,
    nombre_version VARCHAR(150) NOT NULL,
    descripcion VARCHAR(1000),
    snapshot_json TEXT NOT NULL,
    modelo_id BIGINT NOT NULL,
    usuario_creador_id BIGINT,
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT fk_version_modelo FOREIGN KEY (modelo_id)
        REFERENCES modelos_uml(id) ON DELETE CASCADE,
    CONSTRAINT fk_version_usuario FOREIGN KEY (usuario_creador_id)
        REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_versiones_modelo ON versiones_modelo(modelo_id);
CREATE INDEX IF NOT EXISTS idx_versiones_fecha ON versiones_modelo(fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_versiones_usuario ON versiones_modelo(usuario_creador_id);

-- 2. Tabla Historial de Cambios Atómicos
CREATE TABLE IF NOT EXISTS historial_cambios (
    id BIGSERIAL PRIMARY KEY,
    tipo_operacion VARCHAR(50) NOT NULL,
    elemento_modificado VARCHAR(100) NOT NULL,
    id_elemento VARCHAR(100),
    datos_anteriores TEXT,
    datos_nuevos TEXT,
    usuario_id BIGINT,
    usuario_email VARCHAR(150) NOT NULL,
    fecha_cambio TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version_modelo_id BIGINT,
    modelo_id BIGINT NOT NULL,
    CONSTRAINT fk_historial_modelo FOREIGN KEY (modelo_id)
        REFERENCES modelos_uml(id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_version FOREIGN KEY (version_modelo_id)
        REFERENCES versiones_modelo(id) ON DELETE SET NULL,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_historial_modelo ON historial_cambios(modelo_id);
CREATE INDEX IF NOT EXISTS idx_historial_fecha ON historial_cambios(fecha_cambio);
CREATE INDEX IF NOT EXISTS idx_historial_version ON historial_cambios(version_modelo_id);

-- ====================================================================
-- FASE 7: Agente de Inteligencia Artificial para Edición UML
-- ====================================================================

CREATE TABLE IF NOT EXISTS ai_comandos_historial (
    id BIGSERIAL PRIMARY KEY,
    tipo_operacion VARCHAR(50) NOT NULL,
    elemento_objetivo VARCHAR(100),
    parametros TEXT,
    prompt_original TEXT NOT NULL,
    respuesta_generada TEXT,
    exitoso BOOLEAN NOT NULL DEFAULT TRUE,
    requiere_confirmacion BOOLEAN NOT NULL DEFAULT FALSE,
    usuario_id BIGINT,
    usuario_email VARCHAR(150) NOT NULL,
    fecha TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modelo_id BIGINT NOT NULL,
    CONSTRAINT fk_ai_comando_modelo FOREIGN KEY (modelo_id)
        REFERENCES modelos_uml(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_comando_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_ai_comandos_modelo ON ai_comandos_historial(modelo_id);
CREATE INDEX IF NOT EXISTS idx_ai_comandos_fecha ON ai_comandos_historial(fecha);
CREATE INDEX IF NOT EXISTS idx_ai_comandos_usuario ON ai_comandos_historial(usuario_id);


