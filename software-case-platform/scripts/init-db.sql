-- ==============================================================================
-- FASE 13: SCRIPT DDL DE INICIALIZACIÓN DE PRODUCCIÓN POSTGRESQL (AWS RDS)
-- ==============================================================================

-- 1. Tabla de Usuarios y Seguridad RBAC
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

-- 2. Tabla de Proyectos UML
CREATE TABLE IF NOT EXISTS proyectos_uml (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(1000),
    usuario_propietario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_proyectos_usuario ON proyectos_uml(usuario_propietario_id);

-- 3. Tabla de Modelos UML
CREATE TABLE IF NOT EXISTS modelos_uml (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    proyecto_id BIGINT NOT NULL REFERENCES proyectos_uml(id) ON DELETE CASCADE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabla de Clases UML
CREATE TABLE IF NOT EXISTS clases_uml (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    visibilidad VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    descripcion VARCHAR(500),
    posicion_x DOUBLE PRECISION DEFAULT 0.0,
    posicion_y DOUBLE PRECISION DEFAULT 0.0,
    modelo_id BIGINT NOT NULL REFERENCES modelos_uml(id) ON DELETE CASCADE,
    CONSTRAINT uk_clase_modelo_nombre UNIQUE (modelo_id, nombre)
);

CREATE INDEX IF NOT EXISTS idx_clases_modelo ON clases_uml(modelo_id);

-- 5. Tabla de Atributos UML
CREATE TABLE IF NOT EXISTS atributos_uml (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo_dato VARCHAR(100) NOT NULL,
    visibilidad VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    es_estatico BOOLEAN DEFAULT FALSE,
    valor_defecto VARCHAR(255),
    clase_id BIGINT NOT NULL REFERENCES clases_uml(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_atributos_clase ON atributos_uml(clase_id);

-- 6. Tabla de Métodos UML
CREATE TABLE IF NOT EXISTS metodos_uml (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo_retorno VARCHAR(100) NOT NULL DEFAULT 'void',
    visibilidad VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    es_estatico BOOLEAN DEFAULT FALSE,
    parametros VARCHAR(500),
    clase_id BIGINT NOT NULL REFERENCES clases_uml(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_metodos_clase ON metodos_uml(clase_id);

-- 7. Tabla de Relaciones UML
CREATE TABLE IF NOT EXISTS relaciones_uml (
    id BIGSERIAL PRIMARY KEY,
    tipo_relacion VARCHAR(30) NOT NULL,
    clase_origen_id BIGINT NOT NULL REFERENCES clases_uml(id) ON DELETE CASCADE,
    clase_destino_id BIGINT NOT NULL REFERENCES clases_uml(id) ON DELETE CASCADE,
    cardinalidad_origen VARCHAR(20),
    cardinalidad_destino VARCHAR(20),
    descripcion VARCHAR(500),
    modelo_id BIGINT NOT NULL REFERENCES modelos_uml(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_relaciones_modelo ON relaciones_uml(modelo_id);

-- 8. Tabla de Versiones e Historial de Cambios
CREATE TABLE IF NOT EXISTS versiones_modelo (
    id BIGSERIAL PRIMARY KEY,
    modelo_id BIGINT NOT NULL REFERENCES modelos_uml(id) ON DELETE CASCADE,
    nombre_version VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    snapshot_json TEXT NOT NULL,
    creado_por VARCHAR(150) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS historial_cambios (
    id BIGSERIAL PRIMARY KEY,
    modelo_id BIGINT NOT NULL REFERENCES modelos_uml(id) ON DELETE CASCADE,
    tipo_operacion VARCHAR(50) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    usuario_email VARCHAR(150) NOT NULL,
    detalles_json TEXT,
    fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Inserción de usuarios semilla para ambiente de producción
INSERT INTO usuarios (nombre_completo, email, password, rol, estado)
VALUES 
  ('Administrador del Sistema', 'admin@caseplatform.com', '$2a$10$w8u5X87OQJzZcZp042gQjO286X9/Jq2UqNsqw9d82Yg6k3HwQdC2S', 'ADMIN', 'ACTIVO'),
  ('Ingeniero de Software', 'ingeniero@caseplatform.com', '$2a$10$w8u5X87OQJzZcZp042gQjO286X9/Jq2UqNsqw9d82Yg6k3HwQdC2S', 'INGENIERO', 'ACTIVO')
ON CONFLICT (email) DO NOTHING;
