package com.caseplatform.generator.model;

/**
 * Tipos de relaciones JPA generadas a partir de cardinalidades UML.
 */
public enum TipoRelacionJPA {
    ONE_TO_MANY,
    MANY_TO_ONE,
    ONE_TO_ONE,
    MANY_TO_MANY,
    INHERITANCE
}
