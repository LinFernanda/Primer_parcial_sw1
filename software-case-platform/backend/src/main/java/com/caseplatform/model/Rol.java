package com.caseplatform.model;

/**
 * Roles del sistema para autorización y control de acceso basado en roles (RBAC).
 * - ADMIN: Gestionar usuarios, administrar configuración general, controlar acceso.
 * - INGENIERO: Crear proyectos, diseñar modelos UML, utilizar herramientas colaborativas.
 */
public enum Rol {
    ADMIN,
    ARQUITECTO,
    INGENIERO
}
