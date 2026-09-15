package com.caseplatform.model;

/**
 * Tipos de datos soportados para atributos en el núcleo UML 2.5.
 */
public enum TipoDatoUML {
    STRING("String"),
    INTEGER("Integer"),
    LONG("Long"),
    DOUBLE("Double"),
    BOOLEAN("Boolean"),
    DATE("Date"),
    CUSTOM("Custom");

    private final String nombre;

    TipoDatoUML(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public static TipoDatoUML fromNombre(String texto) {
        if (texto == null) return null;
        String limpio = texto.trim();
        for (TipoDatoUML t : values()) {
            if (t.nombre.equalsIgnoreCase(limpio) || t.name().equalsIgnoreCase(limpio)) {
                return t;
            }
        }
        return CUSTOM;
    }
}
