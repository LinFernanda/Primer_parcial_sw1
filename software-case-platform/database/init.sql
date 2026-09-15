-- ====================================================================
-- CASE Platform - Database Initialization Script
-- Target Database: case_platform_db
-- Target RDBMS: PostgreSQL 15+ / 16+
-- ====================================================================

-- Enable UUID extension for unique identifier generation across models
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Schema creation (public is default, setting search_path)
SET search_path TO public;

-- Verification table for database connectivity check
CREATE TABLE IF NOT EXISTS platform_system_check (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    check_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO platform_system_check (check_name, status)
VALUES ('SYSTEM_INIT_PHASE_1', 'SUCCESS')
ON CONFLICT DO NOTHING;
