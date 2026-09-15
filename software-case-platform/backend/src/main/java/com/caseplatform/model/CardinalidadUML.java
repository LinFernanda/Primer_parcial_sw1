package com.caseplatform.model;

/**
 * Cardinalidades UML 2.5 obligatorias para extremos de relaciones.
 */
public enum CardinalidadUML {
    UNO("1"),
    CERO_UNO("0..1"),
    MUCHOS("*"),
    UNO_MUCHOS("1..*"),
    CERO_MUCHOS("0..*");

    private final String valor;

    CardinalidadUML(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static CardinalidadUML fromValor(String texto) {
        if (texto == null) return null;
        String limpio = texto.trim();
        for (CardinalidadUML c : values()) {
            if (c.valor.equals(limpio) || c.name().equalsIgnoreCase(limpio)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Cardinalidad no soportada: " + texto);
    }
}
