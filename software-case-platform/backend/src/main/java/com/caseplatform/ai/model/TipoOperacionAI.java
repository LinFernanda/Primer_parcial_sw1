package com.caseplatform.ai.model;

/**
 * Tipos de operaciones UML estructuradas interpretables y ejecutables por el Agente IA.
 */
public enum TipoOperacionAI {
    CREATE_CLASS,
    UPDATE_CLASS,
    DELETE_CLASS,
    CREATE_ATTRIBUTE,
    UPDATE_ATTRIBUTE,
    DELETE_ATTRIBUTE,
    CREATE_RELATION,
    UPDATE_RELATION,
    DELETE_RELATION,
    CONFIRMATION_REQUIRED,
    UNKNOWN
}
