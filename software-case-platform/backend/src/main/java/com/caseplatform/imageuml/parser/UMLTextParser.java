package com.caseplatform.imageuml.parser;

import com.caseplatform.imageuml.dto.AtributoDetectadoDTO;
import com.caseplatform.imageuml.dto.ClaseDetectadaDTO;
import com.caseplatform.imageuml.dto.MetodoDetectadoDTO;
import com.caseplatform.imageuml.dto.RelacionDetectadaDTO;
import com.caseplatform.model.TipoRelacionUML;
import com.caseplatform.model.VisibilidadUML;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser inteligente de texto y patrones sintácticos de diagramas UML.
 * Extrae y normaliza clases, atributos, visibilidades, tipos de datos,
 * métodos, relaciones y cardinalidades (1, 0..1, *, 1..*, 0..*).
 */
@Slf4j
@Component
public class UMLTextParser {

    // Regex para visibilidad UML (+, -, #, ~)
    private static final Pattern VISIBILITY_PATTERN = Pattern.compile("^\\s*([+\\-#~])?\\s*(.+)$");

    // Regex para atributo: [visibilidad] nombre [: tipo] [= valorInicial]
    private static final Pattern ATTR_PATTERN = Pattern.compile(
            "^\\s*([+\\-#~])?\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?::\\s*([a-zA-Z0-9_<>\\[\\]]+))?\\s*(?:=\\s*(.+))?$"
    );

    // Regex para método: [visibilidad] nombre ( [params] ) [: tipoRetorno]
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "^\\s*([+\\-#~])?\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\((.*?)\\)\\s*(?::\\s*([a-zA-Z0-9_<>\\[\\]]+))?$"
    );

    // Regex para relaciones entre dos entidades con cardinalidades opcionales
    // Ejemplos: "Cliente 1 -- * Factura", "Pedido * -> 1 Cliente", "Animal <|-- Perro", "Servicio ..> Repositorio"
    private static final Pattern RELATION_PATTERN = Pattern.compile(
            "([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?:[\"']?([0-9]+(?:\\.\\.[0-9*]+)?|\\*)[\"']?\\s*)?" +
            "(--|-->|<--|<\\|--|--\\|>|\\*--|--\\*|o--|--o|\\.\\.>|<\\.\\.|->|<-)" +
            "\\s*(?:[\"']?([0-9]+(?:\\.\\.[0-9*]+)?|\\*)[\"']?\\s*)?([a-zA-Z_][a-zA-Z0-9_]*)" +
            "(?:\\s*:\\s*([a-zA-Z0-9_\\s]+))?"
    );

    /**
     * Parsea un bloque de texto que describe clases y relaciones en formato textual o diagrama estructurado.
     */
    public List<ClaseDetectadaDTO> parseClassesFromText(String text) {
        List<ClaseDetectadaDTO> clases = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return clases;
        }

        String[] lines = text.split("\\r?\\n");
        ClaseDetectadaDTO claseActual = null;
        boolean readingAttributes = true;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            // Separadores de cajas de clase UML (líneas con guiones o signos de igual)
            if (line.matches("^[-=_]{3,}$")) {
                if (claseActual != null && readingAttributes) {
                    readingAttributes = false; // Segunda sección de la caja de clase: métodos
                }
                continue;
            }

            // Detección de inicio de clase: "class Nombre" o "[Nombre]" o "Nombre"
            if (line.toLowerCase().startsWith("class ") || line.matches("^\\[[a-zA-Z_][a-zA-Z0-9_]*\\]$") ||
                (claseActual == null && isClassHeader(line))) {

                String className = cleanClassName(line);
                claseActual = ClaseDetectadaDTO.builder()
                        .nombre(className)
                        .visibilidad(VisibilidadUML.PUBLIC)
                        .atributos(new ArrayList<>())
                        .metodos(new ArrayList<>())
                        .build();
                clases.add(claseActual);
                readingAttributes = true;
                continue;
            }

            // Si no hay clase activa y parece nombre de clase
            if (claseActual == null) {
                if (isClassHeader(line)) {
                    claseActual = ClaseDetectadaDTO.builder()
                            .nombre(cleanClassName(line))
                            .visibilidad(VisibilidadUML.PUBLIC)
                            .atributos(new ArrayList<>())
                            .metodos(new ArrayList<>())
                            .build();
                    clases.add(claseActual);
                    readingAttributes = true;
                }
                continue;
            }

            // Descartar si es una línea de relación en medio del bloque
            if (RELATION_PATTERN.matcher(line).find()) {
                claseActual = null; // Termina la clase actual
                continue;
            }

            // Intentar parsear como método si contiene paréntesis
            if (line.contains("(") && line.contains(")")) {
                MetodoDetectadoDTO metodo = parseMethodLine(line);
                if (metodo != null) {
                    claseActual.getMetodos().add(metodo);
                    continue;
                }
            }

            // Si estamos leyendo atributos o tiene dos puntos
            AtributoDetectadoDTO attr = parseAttributeLine(line);
            if (attr != null) {
                claseActual.getAtributos().add(attr);
            } else if (isClassHeader(line) && !line.contains(":") && !line.contains("(")) {
                // Posible inicio de nueva clase
                claseActual = ClaseDetectadaDTO.builder()
                        .nombre(cleanClassName(line))
                        .visibilidad(VisibilidadUML.PUBLIC)
                        .atributos(new ArrayList<>())
                        .metodos(new ArrayList<>())
                        .build();
                clases.add(claseActual);
                readingAttributes = true;
            }
        }

        return clases;
    }

    private static final String[] KNOWN_CONNECTORS = {
            "<|--", "--|>", "*--", "--*", "o--", "--o", "..>", "<..", "-->", "<--", "->", "<-", "--"
    };

    /**
     * Extrae relaciones entre clases y sus cardinalidades a partir de texto estructurado.
     */
    public List<RelacionDetectadaDTO> parseRelationsFromText(String text) {
        List<RelacionDetectadaDTO> relaciones = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return relaciones;
        }

        String[] lines = text.split("\\r?\\n");
        for (String rawLine : lines) {
            RelacionDetectadaDTO rel = parseRelationLine(rawLine);
            if (rel != null) {
                relaciones.add(rel);
            }
        }

        return relaciones;
    }

    /**
     * Parsea una línea individual que contiene una relación UML.
     */
    public RelacionDetectadaDTO parseRelationLine(String rawLine) {
        if (rawLine == null || rawLine.isBlank()) return null;

        String line = rawLine.trim();

        // Extraer descripción opcional tras ':'
        String desc = null;
        if (line.contains(":")) {
            String[] parts = line.split(":", 2);
            desc = parts[1].trim();
            line = parts[0].trim();
        }

        // Buscar el conector adecuado (los más largos primero para evitar prefijos)
        String foundConnector = null;
        int connectorIdx = -1;
        for (String conn : KNOWN_CONNECTORS) {
            int idx = line.indexOf(conn);
            if (idx != -1) {
                foundConnector = conn;
                connectorIdx = idx;
                break;
            }
        }

        if (foundConnector == null || connectorIdx <= 0) {
            return null;
        }

        String left = line.substring(0, connectorIdx).trim();
        String right = line.substring(connectorIdx + foundConnector.length()).trim();

        if (left.isEmpty() || right.isEmpty()) {
            return null;
        }

        // Parsear lado izquierdo: "ClaseOrigen [cardinalidad]"
        String origen = left;
        String cardOrigen = "1";
        String[] leftTokens = left.split("\\s+");
        if (leftTokens.length >= 2) {
            origen = leftTokens[0];
            cardOrigen = normalizeCardinality(leftTokens[1]);
        }

        // Parsear lado derecho: "[cardinalidad] ClaseDestino"
        String destino = right;
        String cardDestino = "1";
        String[] rightTokens = right.split("\\s+");
        if (rightTokens.length >= 2) {
            cardDestino = normalizeCardinality(rightTokens[0]);
            destino = rightTokens[1];
        }

        origen = cleanClassName(origen);
        destino = cleanClassName(destino);

        if (!origen.matches("^[a-zA-Z_][a-zA-Z0-9_]*$") || !destino.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return null;
        }

        TipoRelacionUML tipo = resolveRelationType(foundConnector);

        return RelacionDetectadaDTO.builder()
                .claseOrigen(origen)
                .claseDestino(destino)
                .tipoRelacion(tipo)
                .cardinalidadOrigen(cardOrigen != null ? cardOrigen : "1")
                .cardinalidadDestino(cardDestino != null ? cardDestino : "1")
                .descripcion(desc)
                .build();
    }

    /**
     * Parsea una línea de atributo UML individual: "+ nombre: String = 'val'"
     */
    public AtributoDetectadoDTO parseAttributeLine(String line) {
        if (line == null || line.isBlank()) return null;

        Matcher matcher = ATTR_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            String visChar = matcher.group(1);
            String name = matcher.group(2);
            String rawType = matcher.group(3);
            String initialVal = matcher.group(4);

            VisibilidadUML vis = parseVisibility(visChar, VisibilidadUML.PRIVATE);
            String type = normalizeDataType(rawType != null ? rawType : "String");

            return AtributoDetectadoDTO.builder()
                    .nombre(name)
                    .tipoDato(type)
                    .visibilidad(vis)
                    .valorInicial(initialVal != null ? initialVal.trim() : null)
                    .build();
        }

        // Intento heurístico para formato simple "nombre:tipo" o "tipo nombre"
        String trimmed = line.trim().replaceAll("^[+\\-#~]\\s*", "");
        if (trimmed.contains(":")) {
            String[] parts = trimmed.split(":", 2);
            String name = parts[0].trim();
            String type = normalizeDataType(parts[1].trim());
            if (name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                VisibilidadUML vis = parseVisibility(line.trim().substring(0, 1), VisibilidadUML.PRIVATE);
                return AtributoDetectadoDTO.builder()
                        .nombre(name)
                        .tipoDato(type)
                        .visibilidad(vis)
                        .build();
            }
        }

        return null;
    }

    /**
     * Parsea una línea de método UML individual: "+ calcularTotal(descuento: Double): Double"
     */
    public MetodoDetectadoDTO parseMethodLine(String line) {
        if (line == null || line.isBlank()) return null;

        Matcher matcher = METHOD_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            String visChar = matcher.group(1);
            String name = matcher.group(2);
            String params = matcher.group(3);
            String returnType = matcher.group(4);

            VisibilidadUML vis = parseVisibility(visChar, VisibilidadUML.PUBLIC);
            String type = normalizeDataType(returnType != null ? returnType : "void");

            return MetodoDetectadoDTO.builder()
                    .nombre(name)
                    .tipoRetorno(type)
                    .visibilidad(vis)
                    .parametros(params != null && !params.isBlank() ? params.trim() : null)
                    .build();
        }

        return null;
    }

    /**
     * Normaliza los tipos de datos comunes a estándares Java / UML.
     */
    public String normalizeDataType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return "String";
        }
        String clean = rawType.trim();
        String lower = clean.toLowerCase();

        return switch (lower) {
            case "int", "integer", "number", "entero" -> "Integer";
            case "long", "bigint" -> "Long";
            case "string", "str", "varchar", "text", "texto" -> "String";
            case "double", "float", "decimal", "real" -> "Double";
            case "bool", "boolean", "booleano" -> "Boolean";
            case "date", "fecha", "datetime", "timestamp" -> "Date";
            case "void" -> "void";
            default -> clean;
        };
    }

    /**
     * Normaliza cardinalidades UML soportadas: 1, 0..1, *, 1..*, 0..*
     */
    public String normalizeCardinality(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String clean = raw.trim().replace("\"", "").replace("'", "");
        return switch (clean) {
            case "1", "uno", "one" -> "1";
            case "0..1", "0..*" -> clean;
            case "*", "n", "m", "many", "muchos" -> "*";
            case "1..*", "1..n" -> "1..*";
            default -> clean;
        };
    }

    /**
     * Determina la visibilidad UML a partir del símbolo prefijo (+, -, #, ~).
     */
    public VisibilidadUML parseVisibility(String symbol, VisibilidadUML defaultVis) {
        if (symbol == null || symbol.isBlank()) return defaultVis;
        return switch (symbol.trim()) {
            case "+" -> VisibilidadUML.PUBLIC;
            case "-" -> VisibilidadUML.PRIVATE;
            case "#" -> VisibilidadUML.PROTECTED;
            case "~" -> VisibilidadUML.PACKAGE;
            default -> defaultVis;
        };
    }

    private boolean isClassHeader(String line) {
        String clean = cleanClassName(line);
        // Debe comenzar con mayúscula y ser un identificador válido sin espacios ni símbolos
        return clean.matches("^[A-Z][a-zA-Z0-9_]*$") && !clean.equalsIgnoreCase("UML");
    }

    private String cleanClassName(String line) {
        return line.replace("class ", "")
                .replace("[", "")
                .replace("]", "")
                .replaceAll("<<.*?>>", "")
                .trim();
    }

    private TipoRelacionUML resolveRelationType(String connector) {
        if (connector == null) return TipoRelacionUML.ASOCIACION;
        return switch (connector) {
            case "<|--", "--|>" -> TipoRelacionUML.HERENCIA;
            case "*--", "--*" -> TipoRelacionUML.COMPOSICION;
            case "o--", "--o" -> TipoRelacionUML.AGREGACION;
            case "..>", "<.." -> TipoRelacionUML.DEPENDENCIA;
            default -> TipoRelacionUML.ASOCIACION;
        };
    }
}
