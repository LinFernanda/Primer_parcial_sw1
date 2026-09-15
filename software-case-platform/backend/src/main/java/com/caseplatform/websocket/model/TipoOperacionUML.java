package com.caseplatform.websocket.model;

/**
 * Operaciones colaborativas soportadas en el sistema de eventos en tiempo real.
 */
public enum TipoOperacionUML {
    CREATE,
    UPDATE,
    DELETE,
    LOCK,
    UNLOCK,
    JOIN,
    LEAVE,
    PRESENCE_SYNC,
    LOCKS_SYNC,
    ERROR
}
