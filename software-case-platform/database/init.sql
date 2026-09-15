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
-- Hash BCrypt para 'Admin123*': $2a$10$7EqJtq98hPqEX7fNZaFWoOhiMv4vK1Vd06N76Fz02o4MhyJzom3f2
INSERT INTO usuarios (nombre_completo, email, password, rol, estado, fecha_creacion, fecha_actualizacion)
VALUES (
    'Administrador Plataforma CASE',
    'admin@caseplatform.com',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiMv4vK1Vd06N76Fz02o4MhyJzom3f2',
    'ADMIN',
    'ACTIVO',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
