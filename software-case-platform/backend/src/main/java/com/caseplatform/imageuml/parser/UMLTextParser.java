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
 * Parser inteligente y tolerante a fallos de texto y patrones sintácticos de diagramas UML.
 * Diseñado especialmente para interpretar resultados de OCR (Tesseract / Visión Computacional),
 * código PlantUML, Mermaid o esquemas textuales libres de clases y relaciones.
 */
@Slf4j
@Component
public class UMLTextParser {

    // Regex para visibilidad UML (+, -, #, ~)
    private static final Pattern VISIBILITY_PATTERN = Pattern.compile("^\\s*([+\\-#~])?\\s*(.+)$");

    // Regex para atributo estándar: [visibilidad] nombre [: tipo] [= valorInicial]
    private static final Pattern ATTR_PATTERN = Pattern.compile(
            "^\\s*([+\\-#~])?\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?::\\s*([a-zA-Z0-9_<>\\[\\]]+))?\\s*(?:=\\s*(.+))?$"
    );

    // Regex para atributo estilo Java / Mermaid: [visibilidad] tipo nombre [= valorInicial]
    private static final Pattern ATTR_JAVA_MERMAID_PATTERN = Pattern.compile(
            "^\\s*([+\\-#~])?\\s*([a-zA-Z0-9_<>\\[\\]]+)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?:=\\s*(.+))?$"
    );

    // Regex para método estándar: [visibilidad] nombre ( [params] ) [: tipoRetorno]
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "^\\s*([+\\-#~])?\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\((.*?)\\)\\s*(?::\\s*([a-zA-Z0-9_<>\\[\\]]+))?$"
    );

    // Regex para método estilo Java / C++: [visibilidad] [tipoRetorno] nombre ( [params] )
    private static final Pattern METHOD_JAVA_PATTERN = Pattern.compile(
            "^\\s*([+\\-#~])?\\s*([a-zA-Z0-9_<>\\[\\]]+)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\((.*?)\\)\\s*$"
    );

    // Conectores simbólicos ordenados por longitud decreciente
    private static final String[] KNOWN_CONNECTORS = {
            "<|--", "--|>", "*--", "--*", "o--", "--o", "..>", "<..", "-->", "<--", "->", "<-", "--"
    };

    // Palabras clave textuales de relaciones en español e inglés
    private static final Map<String, TipoRelacionUML> TEXTUAL_RELATIONS = Map.ofEntries(
            Map.entry("hereda de", TipoRelacionUML.HERENCIA),
            Map.entry("hereda", TipoRelacionUML.HERENCIA),
            Map.entry("extends", TipoRelacionUML.HERENCIA),
            Map.entry("implements", TipoRelacionUML.HERENCIA),
            Map.entry("es un", TipoRelacionUML.HERENCIA),
            Map.entry("es una", TipoRelacionUML.HERENCIA),
            Map.entry("contiene", TipoRelacionUML.COMPOSICION),
            Map.entry("compone", TipoRelacionUML.COMPOSICION),
            Map.entry("tiene", TipoRelacionUML.AGREGACION),
            Map.entry("agrega", TipoRelacionUML.AGREGACION),
            Map.entry("depende de", TipoRelacionUML.DEPENDENCIA),
            Map.entry("usa", TipoRelacionUML.DEPENDENCIA),
            Map.entry("asociado a", TipoRelacionUML.ASOCIACION),
            Map.entry("asociado con", TipoRelacionUML.ASOCIACION),
            Map.entry("relacionado con", TipoRelacionUML.ASOCIACION)
    );

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "uml", "diagram", "diagrama", "class", "clase", "interface", "entity", "entidad",
            "model", "modelo", "package", "paquete", "attributes", "atributos", "methods", "metodos",
            "operaciones", "operations", "start", "end", "note", "title", "skinparam", "public", "private",
            "protected", "void", "string", "int", "integer", "boolean", "double", "float", "long", "date"
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
            String line = cleanOcrLine(rawLine);
            if (line.isEmpty()) {
                continue;
            }

            // Separadores de cajas de clase UML (ej: +---+, |----|, -------, =====, etc.)
            if (isBoxDivider(line)) {
                if (claseActual != null && readingAttributes) {
                    readingAttributes = false; // Segunda sección de la caja de clase: métodos
                }
                continue;
            }

            // Llaves de cierre o palabras de fin de bloque
            if (line.equals("}") || line.equalsIgnoreCase("end") || line.equalsIgnoreCase("@enduml")) {
                claseActual = null;
                readingAttributes = true;
                continue;
            }

            // Detección explícita de clase con palabras clave: "class Nombre", "clase Nombre", "interface Nombre", etc.
            String lower = line.toLowerCase();
            if (lower.startsWith("class ") || lower.startsWith("clase ") || lower.startsWith("interface ")
                    || lower.startsWith("entity ") || lower.startsWith("abstract class ")) {
                String className = cleanClassName(line);
                if (!className.isEmpty()) {
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
            }

            // Detección de inicio de clase con corchetes: "[Nombre]"
            if (line.matches("^\\[[a-zA-Z_][a-zA-Z0-9_]*\\]$")) {
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

            // Si no hay clase activa y la línea parece cabecera de clase (ej. PascalCase: "Cliente", "Pedido")
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
            if (parseRelationLine(line) != null) {
                claseActual = null; // Termina la clase actual
                continue;
            }

            // Si la línea indica explícitamente inicio de sección de métodos
            if (lower.equals("metodos") || lower.equals("métodos") || lower.equals("methods") || lower.equals("operaciones")) {
                readingAttributes = false;
                continue;
            }

            // Si la línea indica explícitamente inicio de sección de atributos
            if (lower.equals("atributos") || lower.equals("attributes") || lower.equals("propiedades")) {
                readingAttributes = true;
                continue;
            }

            // Intentar parsear como método si contiene paréntesis ()
            if (line.contains("(") && line.contains(")")) {
                MetodoDetectadoDTO metodo = parseMethodLine(line);
                if (metodo != null) {
                    claseActual.getMetodos().add(metodo);
                    continue;
                }
            }

            // Intentar parsear como atributo
            AtributoDetectadoDTO attr = parseAttributeLine(line);
            if (attr != null) {
                claseActual.getAtributos().add(attr);
            } else if (isClassHeader(line) && !line.contains(":") && !line.contains("(") && !line.contains("=") && !line.contains(" ")) {
                // Posible inicio de una nueva clase sin separador previo
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

        // Deduplicar clases por nombre
        Map<String, ClaseDetectadaDTO> clasesMap = new LinkedHashMap<>();
        for (ClaseDetectadaDTO c : clases) {
            if (c.getNombre() != null && !c.getNombre().isBlank()) {
                if (!clasesMap.containsKey(c.getNombre())) {
                    clasesMap.put(c.getNombre(), c);
                } else {
                    // Combinar atributos y métodos si aparecía repetida
                    ClaseDetectadaDTO existing = clasesMap.get(c.getNombre());
                    if (c.getAtributos() != null) existing.getAtributos().addAll(c.getAtributos());
                    if (c.getMetodos() != null) existing.getMetodos().addAll(c.getMetodos());
                }
            }
        }

        return new ArrayList<>(clasesMap.values());
    }

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
            String line = cleanOcrLine(rawLine);
            RelacionDetectadaDTO rel = parseRelationLine(line);
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

        String line = cleanOcrLine(rawLine);

        // Extraer descripción opcional tras ':' si no es un tipo de atributo
        String desc = null;
        if (line.contains(":") && !line.matches("^[+\\-#~]?\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*:.*")) {
            String[] parts = line.split(":", 2);
            desc = parts[1].trim();
            line = parts[0].trim();
        }

        // 1. Buscar conectores simbólicos primero
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

        if (foundConnector != null && connectorIdx > 0) {
            String left = line.substring(0, connectorIdx).trim();
            String right = line.substring(connectorIdx + foundConnector.length()).trim();

            if (!left.isEmpty() && !right.isEmpty()) {
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

                if (isValidIdentifier(origen) && isValidIdentifier(destino)) {
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
            }
        }

        // 2. Buscar conectores textuales (ej: "Cliente hereda de Persona", "Pedido contiene Detalle")
        for (Map.Entry<String, TipoRelacionUML> entry : TEXTUAL_RELATIONS.entrySet()) {
            String patternStr = "\\b" + Pattern.quote(entry.getKey()) + "\\b";
            Pattern p = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(line);
            if (m.find()) {
                String left = line.substring(0, m.start()).trim();
                String right = line.substring(m.end()).trim();

                String origen = cleanClassName(left);
                String destino = cleanClassName(right);

                if (isValidIdentifier(origen) && isValidIdentifier(destino)) {
                    return RelacionDetectadaDTO.builder()
                            .claseOrigen(origen)
                            .claseDestino(destino)
                            .tipoRelacion(entry.getValue())
                            .cardinalidadOrigen("1")
                            .cardinalidadDestino("1")
                            .descripcion(desc)
                            .build();
                }
            }
        }

        return null;
    }

    /**
     * Parsea una línea de atributo UML individual: "+ nombre: String = 'val'" o "- id: Long"
     */
    public AtributoDetectadoDTO parseAttributeLine(String rawLine) {
        if (rawLine == null || rawLine.isBlank()) return null;

        String line = cleanOcrLine(rawLine);
        // Quitar punto y coma al final
        if (line.endsWith(";")) {
            line = line.substring(0, line.length() - 1).trim();
        }

        // 1. Formato estándar UML: [vis] nombre [: tipo] [= valor]
        Matcher matcher = ATTR_PATTERN.matcher(line);
        if (matcher.matches()) {
            String visChar = matcher.group(1);
            String name = matcher.group(2);
            String rawType = matcher.group(3);
            String initialVal = matcher.group(4);

            // Evitar confundir clases con atributos si no tienen dos puntos ni visibilidad
            if (visChar == null && rawType == null && isClassHeader(name)) {
                return null;
            }

            VisibilidadUML vis = parseVisibility(visChar, VisibilidadUML.PRIVATE);
            String type = normalizeDataType(rawType != null ? rawType : "String");

            return AtributoDetectadoDTO.builder()
                    .nombre(name)
                    .tipoDato(type)
                    .visibilidad(vis)
                    .valorInicial(initialVal != null ? initialVal.trim() : null)
                    .build();
        }

        // 2. Formato estilo Java / Mermaid: [vis] tipo nombre [= valor]
        Matcher javaMatcher = ATTR_JAVA_MERMAID_PATTERN.matcher(line);
        if (javaMatcher.matches()) {
            String visChar = javaMatcher.group(1);
            String rawType = javaMatcher.group(2);
            String name = javaMatcher.group(3);
            String initialVal = javaMatcher.group(4);

            if (isValidIdentifier(name) && isLikelyType(rawType)) {
                VisibilidadUML vis = parseVisibility(visChar, VisibilidadUML.PRIVATE);
                String type = normalizeDataType(rawType);
                return AtributoDetectadoDTO.builder()
                        .nombre(name)
                        .tipoDato(type)
                        .visibilidad(vis)
                        .valorInicial(initialVal != null ? initialVal.trim() : null)
                        .build();
            }
        }

        // 3. Intento heurístico para formato simple "nombre:tipo" o "nombre : tipo"
        String trimmed = line.replaceAll("^[+\\-#~]\\s*", "");
        if (trimmed.contains(":")) {
            String[] parts = trimmed.split(":", 2);
            String name = parts[0].trim();
            String type = normalizeDataType(parts[1].trim());
            if (isValidIdentifier(name)) {
                VisibilidadUML vis = parseVisibility(line.substring(0, 1), VisibilidadUML.PRIVATE);
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
    public MetodoDetectadoDTO parseMethodLine(String rawLine) {
        if (rawLine == null || rawLine.isBlank()) return null;

        String line = cleanOcrLine(rawLine);
        if (line.endsWith(";")) {
            line = line.substring(0, line.length() - 1).trim();
        }

        // 1. Formato estándar UML: [vis] nombre(params): returnType
        Matcher matcher = METHOD_PATTERN.matcher(line);
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

        // 2. Formato estilo Java / C++: [vis] returnType nombre(params)
        Matcher javaMatcher = METHOD_JAVA_PATTERN.matcher(line);
        if (javaMatcher.matches()) {
            String visChar = javaMatcher.group(1);
            String rawType = javaMatcher.group(2);
            String name = javaMatcher.group(3);
            String params = javaMatcher.group(4);

            VisibilidadUML vis = parseVisibility(visChar, VisibilidadUML.PUBLIC);
            String type = normalizeDataType(rawType != null ? rawType : "void");

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
        String clean = rawType.trim().replaceAll("[,;]$", "");
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
        if (raw == null || raw.isBlank()) return "1";
        String clean = raw.trim().replace("\"", "").replace("'", "");
        return switch (clean.toLowerCase()) {
            case "1", "uno", "one" -> "1";
            case "0..1", "0..*" -> clean;
            case "*", "n", "m", "many", "muchos" -> "*";
            case "1..*", "1..n" -> "1..*";
            default -> clean;
        };
    }

    /**
     * Determina la visibilidad UML a partir del símbolo prefijo (+, -, #, ~) o palabra clave.
     */
    public VisibilidadUML parseVisibility(String symbol, VisibilidadUML defaultVis) {
        if (symbol == null || symbol.isBlank()) return defaultVis;
        String s = symbol.trim().toLowerCase();
        return switch (s) {
            case "+", "public", "publico", "público" -> VisibilidadUML.PUBLIC;
            case "-", "private", "privado" -> VisibilidadUML.PRIVATE;
            case "#", "protected", "protegido" -> VisibilidadUML.PROTECTED;
            case "~", "package", "paquete" -> VisibilidadUML.PACKAGE;
            default -> defaultVis;
        };
    }

    /**
     * Limpia ruido común producido por motores OCR al escanear cajas UML:
     * bordes verticales |, │, esquinas +, corchetes, comillas y viñetas.
     */
    public String cleanOcrLine(String rawLine) {
        if (rawLine == null) return "";
        String line = rawLine.trim();

        // Eliminar bordes verticales típicos de ASCII/Unicode en los extremos: | texto |
        line = line.replaceAll("^[|│!\\-]+\\s*", "");
        line = line.replaceAll("\\s*[|│!\\-]+$", "");

        // Eliminar viñetas comunes de OCR
        line = line.replaceAll("^[•*·~]\\s*", "");

        // Eliminar estereotipos como <<entity>>, «table»
        line = line.replaceAll("<<.*?>>", "").replaceAll("«.*?»", "");

        return line.trim();
    }

    /**
     * Determina si la línea corresponde a una división de caja UML (líneas horizontales).
     */
    public boolean isBoxDivider(String line) {
        if (line == null || line.isBlank()) return false;
        String clean = line.replaceAll("[+\\-=_|│┌┐└┘├┤┼~]", "").trim();
        return clean.isEmpty() && line.length() >= 2;
    }

    private boolean isClassHeader(String line) {
        String clean = cleanClassName(line);
        if (clean.isEmpty() || RESERVED_KEYWORDS.contains(clean.toLowerCase())) {
            return false;
        }
        // Debe comenzar con mayúscula o letra válida y ser un identificador
        return clean.matches("^[A-Z][a-zA-Z0-9_]*$");
    }

    private String cleanClassName(String line) {
        if (line == null) return "";
        String clean = line.replace("class ", "")
                .replace("clase ", "")
                .replace("interface ", "")
                .replace("entity ", "")
                .replace("abstract class ", "")
                .replace("[", "")
                .replace("]", "")
                .replace("{", "")
                .replace("}", "")
                .replaceAll("<<.*?>>", "")
                .replaceAll("«.*?»", "")
                .trim();

        // Si tiene espacios (ej: "class Cliente Vip"), tomar el nombre principal o camelCase
        if (clean.contains(" ")) {
            String[] parts = clean.split("\\s+");
            if (parts.length > 0 && isValidIdentifier(parts[0])) {
                clean = parts[0];
            }
        }
        return clean;
    }

    private boolean isValidIdentifier(String s) {
        return s != null && s.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    private boolean isLikelyType(String s) {
        if (s == null) return false;
        String lower = s.toLowerCase();
        return lower.matches("^(string|int|integer|long|double|float|boolean|bool|date|datetime|void|list|set|map|\\[\\]|[a-z0-9_<>]+)$");
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
