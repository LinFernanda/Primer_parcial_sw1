package com.caseplatform.model;

/**
 * Visibilidad UML 2.5 para miembros de una clase (atributos y métodos).
 */
public enum VisibilidadUML {
    PUBLIC("+"),
    PRIVATE("-"),
    PROTECTED("#"),
    PACKAGE("~");

    private final String simbolo;

    VisibilidadUML(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }
}
