package com.caseplatform.ai.parser;

import com.caseplatform.ai.command.ParsedAIAction;
import com.caseplatform.ai.config.AIConfig;
import com.caseplatform.ai.model.TipoOperacionAI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intérprete semántico de lenguaje natural a acciones UML estructuradas.
 * Admite orquestación con modelos LLM externos (OpenAI / compatibles)
 * y motor de Procesamiento de Lenguaje Natural (NLP) determinístico local.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AICommandParser {

    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Interpreta la instrucción en lenguaje natural y la traduce a una acción estructurada.
     */
    public ParsedAIAction parse(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.UNKNOWN)
                    .explicacion("Instrucción vacía o no válida.")
                    .build();
        }

        String promptLimpio = prompt.trim();

        // 1. Si la API Key de OpenAI / LLM está configurada, intentar orquestación con modelo LLM
        if (aiConfig.getApiKey() != null && !aiConfig.getApiKey().isBlank()) {
            try {
                ParsedAIAction llmResult = parseWithLLM(promptLimpio);
                if (llmResult != null && llmResult.getTipoOperacion() != TipoOperacionAI.UNKNOWN) {
                    return llmResult;
                }
            } catch (Exception e) {
                log.warn("Fallo la llamada al modelo LLM ({}), utilizando motor de reglas NLP local: {}",
                        aiConfig.getModel(), e.getMessage());
            }
        }

        // 2. Motor NLP semántico determinístico local (español e inglés)
        return parseWithRuleEngine(promptLimpio);
    }

    /**
     * Interpretación asistida por modelo LLM (OpenAI API / endpoint compatible con chat completions).
     */
    private ParsedAIAction parseWithLLM(String prompt) {
        String systemPrompt = """
                Eres un asistente experto en ingeniería de software y modelado conceptual UML 2.5.
                Tu función exclusiva es interpretar comandos del usuario en lenguaje natural y convertirlos
                en una acción JSON estructurada para manipular el diagrama UML existente.
                
                IMPORTANTE: NO debes generar sistemas completos desde cero. Solo traduce la orden directa del usuario.
                
                Responde ÚNICAMENTE con un JSON válido con la siguiente estructura:
                {
                  "tipoOperacion": "CREATE_CLASS | UPDATE_CLASS | DELETE_CLASS | CREATE_ATTRIBUTE | UPDATE_ATTRIBUTE | DELETE_ATTRIBUTE | CREATE_RELATION | UPDATE_RELATION | DELETE_RELATION | CONFIRMATION_REQUIRED | UNKNOWN",
                  "nombreClase": "Nombre de clase o null",
                  "nuevoNombreClase": "Nuevo nombre si es rename o null",
                  "nombreAtributo": "Nombre de atributo o null",
                  "nuevoNombreAtributo": "Nuevo nombre si es rename o null",
                  "tipoDatoAtributo": "String, Integer, Double, Boolean, Long, etc.",
                  "visibilidad": "PUBLIC, PRIVATE, PROTECTED, PACKAGE",
                  "claseOrigen": "Clase origen de relación o null",
                  "claseDestino": "Clase destino de relación o null",
                  "tipoRelacion": "ASOCIACION, HERENCIA, AGREGACION, COMPOSICION, DEPENDENCIA",
                  "cardinalidadOrigen": "1, 0..1, *, 1..*",
                  "cardinalidadDestino": "1, 0..1, *, 1..*",
                  "descripcion": "Descripción o null",
                  "requiereConfirmacion": false,
                  "preguntaConfirmacion": null,
                  "atributos": [
                    {"nombre": "campo", "tipo": "String", "visibilidad": "PRIVATE"}
                  ],
                  "explicacion": "Breve confirmación de la acción interpretada"
                }
                
                Si la orden es ambigua (ej. 'Crear cliente' sin especificar si es clase), establece tipoOperacion en 'CONFIRMATION_REQUIRED', requiereConfirmacion en true y formula preguntaConfirmacion.
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiConfig.getApiKey().trim());

        Map<String, Object> body = Map.of(
                "model", aiConfig.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.0,
                "response_format", Map.of("type", "json_object")
        );

        String url = aiConfig.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                return objectMapper.readValue(content, ParsedAIAction.class);
            } catch (Exception e) {
                log.error("Error al parsear respuesta JSON de OpenAI: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Motor de reglas semánticas y expresiones regulares para interpretación local de comandos UML.
     */
    public ParsedAIAction parseWithRuleEngine(String prompt) {
        String p = prompt.trim();
        String lower = p.toLowerCase();

        // ---------------------------------------------------------------------
        // RESTRICCIÓN ESTRICTA: No generar sistemas completos desde cero (Fase 7)
        // ---------------------------------------------------------------------
        if (lower.contains("sistema completo") || lower.contains("todo el sistema")
                || lower.contains("generar sistema") || lower.contains("crear sistema")) {
            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.UNKNOWN)
                    .explicacion("El Agente IA está diseñado exclusivamente para asistir en la edición y manipulación del modelo UML existente. No genera sistemas completos desde cero.")
                    .build();
        }

        // ---------------------------------------------------------------------
        // CASO 1: Ambigüedad evidente que requiere confirmación (Sección 9)
        // Ej: "crear cliente", "cliente", "factura", "crear venta" (sin palabra 'clase' ni tipo)
        // ---------------------------------------------------------------------
        Pattern ambiguedadPattern = Pattern.compile("^(?:crear|añadir|agregar|nueva)\\s+([a-záéíóúñ0-9_]+)$", Pattern.CASE_INSENSITIVE);
        Matcher ambiguedadMatcher = ambiguedadPattern.matcher(p);
        if (ambiguedadMatcher.matches()) {
            String palabra = ambiguedadMatcher.group(1).trim();
            if (!palabra.equalsIgnoreCase("clase") && !palabra.equalsIgnoreCase("relacion") && !palabra.equalsIgnoreCase("atributo")) {
                String nombrePropuesto = capitalize(palabra);
                return ParsedAIAction.builder()
                        .tipoOperacion(TipoOperacionAI.CONFIRMATION_REQUIRED)
                        .nombreClase(nombrePropuesto)
                        .requiereConfirmacion(true)
                        .preguntaConfirmacion("¿Desea crear una clase llamada '" + nombrePropuesto + "'?")
                        .explicacion("El comando es ambiguo. Se sugiere crear la clase " + nombrePropuesto)
                        .build();
            }
        }

        // ---------------------------------------------------------------------
        // CASO 2: Crear clase con posibles atributos
        // Ej: "Crear clase Cliente", "Crear una clase llamada Cliente",
        //     "Crear clase Cliente con atributo nombre String y edad Integer"
        // ---------------------------------------------------------------------
        Pattern crearClasePattern = Pattern.compile(
                "(?:crear|añadir|agregar|nueva)\\s+(?:una\\s+)?clase\\s+(?:llamada\\s+)?([A-Za-z0-9_]+)(?:\\s+con\\s+(?:el\\s+)?(?:atributo|atributos|campos?)\\s+(.+))?",
                Pattern.CASE_INSENSITIVE
        );
        Matcher crearClaseMatcher = crearClasePattern.matcher(p);
        if (crearClaseMatcher.find()) {
            String nombreClase = capitalize(crearClaseMatcher.group(1));
            String atributosRaw = crearClaseMatcher.group(2);

            List<ParsedAIAction.AtributoSimple> atributos = new ArrayList<>();
            if (atributosRaw != null && !atributosRaw.isBlank()) {
                atributos = parseListaAtributos(atributosRaw);
            }

            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.CREATE_CLASS)
                    .nombreClase(nombreClase)
                    .visibilidad("PUBLIC")
                    .atributos(atributos)
                    .explicacion("Crear clase UML '" + nombreClase + "'" +
                            (!atributos.isEmpty() ? " con " + atributos.size() + " atributo(s)" : ""))
                    .build();
        }

        // ---------------------------------------------------------------------
        // CASO 3: Modificar o renombrar clase
        // Ej: "Cambiar el nombre de Cliente a Usuario", "Renombrar clase Cliente por Usuario"
        // ---------------------------------------------------------------------
        Pattern renameClasePattern = Pattern.compile(
                "(?:cambiar|modificar|renombrar)\\s+(?:el\\s+nombre\\s+de\\s+(?:la\\s+clase\\s+)?|clase\\s+)?([A-Za-z0-9_]+)\\s+(?:a|por)\\s+([A-Za-z0-9_]+)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher renameClaseMatcher = renameClasePattern.matcher(p);
        if (renameClaseMatcher.find()) {
            String origen = capitalize(renameClaseMatcher.group(1));
            String destino = capitalize(renameClaseMatcher.group(2));
            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.UPDATE_CLASS)
                    .nombreClase(origen)
                    .nuevoNombreClase(destino)
                    .explicacion("Renombrar clase '" + origen + "' a '" + destino + "'")
                    .build();
        }

        // ---------------------------------------------------------------------
        // CASO 4: Eliminar clase
        // Ej: "Eliminar la clase Producto", "Borrar clase Factura"
        // ---------------------------------------------------------------------
        Pattern deleteClasePattern = Pattern.compile(
                "(?:eliminar|borrar|quitar)\\s+(?:la\\s+)?clase\\s+([A-Za-z0-9_]+)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher deleteClaseMatcher = deleteClasePattern.matcher(p);
        if (deleteClaseMatcher.find()) {
            String nombreClase = capitalize(deleteClaseMatcher.group(1));
            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.DELETE_CLASS)
                    .nombreClase(nombreClase)
                    .explicacion("Eliminar clase UML '" + nombreClase + "'")
                    .build();
        }

        // ---------------------------------------------------------------------
        // CASO 5: Agregar atributo a clase
        // Ej: "Agregar atributo nombre de tipo String a Cliente",
        //     "Añadir atributo email String en Usuario",
        //     "Agregar atributo precio tipo Double a Producto"
        // ---------------------------------------------------------------------
        Pattern agregarAttrPattern = Pattern.compile(
                "(?:agregar|añadir|crear|nuevo)\\s+atributo\\s+([A-Za-z0-9_]+)(?:\\s+(?:de\\s+tipo|tipo)\\s+|\\s+)([A-Za-z0-9_<>]+)(?:\\s+con\\s+visibilidad\\s+([A-Za-z]+))?\\s+(?:a|en)\\s+(?:la\\s+clase\\s+)?([A-Za-z0-9_]+)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher agregarAttrMatcher = agregarAttrPattern.matcher(p);
        if (agregarAttrMatcher.find()) {
            String nombreAttr = agregarAttrMatcher.group(1);
            String tipoDato = normalizarTipoDato(agregarAttrMatcher.group(2));
            String visibilidad = agregarAttrMatcher.group(3) != null
                    ? agregarAttrMatcher.group(3).toUpperCase()
                    : "PRIVATE";
            String nombreClase = capitalize(agregarAttrMatcher.group(4));

            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.CREATE_ATTRIBUTE)
                    .nombreClase(nombreClase)
                    .nombreAtributo(nombreAttr)
                    .tipoDatoAtributo(tipoDato)
                    .visibilidad(visibilidad)
                    .explicacion("Agregar atributo '" + nombreAttr + ": " + tipoDato + "' a clase '" + nombreClase + "'")
                    .build();
        }

        // ---------------------------------------------------------------------
        // CASO 6: Modificar o renombrar atributo
        // Ej: "Modificar atributo nombre en Cliente a nombreCompleto",
        //     "Cambiar atributo precio en Producto por valor Double"
        // ---------------------------------------------------------------------
        Pattern updateAttrPattern = Pattern.compile(
                "(?:cambiar|modificar|renombrar)\\s+atributo\\s+([A-Za-z0-9_]+)(?:\\s+(?:en|de)\\s+(?:la\\s+clase\\s+)?([A-Za-z0-9_]+))?\\s+(?:a|por)\\s+([A-Za-z0-9_]+)(?:\\s+(?:de\\s+tipo|tipo)\\s+([A-Za-z0-9_<>]+))?",
                Pattern.CASE_INSENSITIVE
        );
        Matcher updateAttrMatcher = updateAttrPattern.matcher(p);
        if (updateAttrMatcher.find()) {
            String attrViejo = updateAttrMatcher.group(1);
            String claseObj = updateAttrMatcher.group(2) != null ? capitalize(updateAttrMatcher.group(2)) : null;
            String attrNuevo = updateAttrMatcher.group(3);
            String nuevoTipo = updateAttrMatcher.group(4) != null ? normalizarTipoDato(updateAttrMatcher.group(4)) : null;

            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.UPDATE_ATTRIBUTE)
                    .nombreClase(claseObj)
                    .nombreAtributo(attrViejo)
                    .nuevoNombreAtributo(attrNuevo)
                    .tipoDatoAtributo(nuevoTipo)
                    .explicacion("Modificar atributo '" + attrViejo + "' por '" + attrNuevo + "'" +
                            (claseObj != null ? " en clase '" + claseObj + "'" : ""))
                    .build();
        }

        // ---------------------------------------------------------------------
        // CASO 7: Eliminar atributo
        // Ej: "Eliminar el atributo precio de Producto", "Borrar atributo email en Cliente"
        // ---------------------------------------------------------------------
        Pattern deleteAttrPattern = Pattern.compile(
                "(?:eliminar|borrar|quitar)\\s+(?:el\\s+)?atributo\\s+([A-Za-z0-9_]+)(?:\\s+(?:de|en)\\s+(?:la\\s+clase\\s+)?([A-Za-z0-9_]+))?",
                Pattern.CASE_INSENSITIVE
        );
        Matcher deleteAttrMatcher = deleteAttrPattern.matcher(p);
        if (deleteAttrMatcher.find()) {
            String nombreAttr = deleteAttrMatcher.group(1);
            String nombreClase = deleteAttrMatcher.group(2) != null ? capitalize(deleteAttrMatcher.group(2)) : null;

            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.DELETE_ATTRIBUTE)
                    .nombreClase(nombreClase)
                    .nombreAtributo(nombreAttr)
                    .explicacion("Eliminar atributo '" + nombreAttr + "'" +
                            (nombreClase != null ? " de clase '" + nombreClase + "'" : ""))
                    .build();
        }

        // ---------------------------------------------------------------------
        // CASO 8: Crear relación entre clases
        // Ej: "Relacionar Cliente con Venta con cardinalidad uno a muchos",
        //     "Conectar Cliente y Pedido con asociación 1 a *",
        //     "Crear herencia entre Persona y Empleado"
        // ---------------------------------------------------------------------
        Pattern crearRelacionPattern = Pattern.compile(
                "(?:relacionar|conectar|unir|crear\\s+relacion\\s+entre)\\s+([A-Za-z0-9_]+)\\s+(?:con|y)\\s+([A-Za-z0-9_]+)(?:\\s+(?:de\\s+tipo|con)\\s+([A-Za-z]+))?(?:\\s+(?:con\\s+cardinalidad|cardinalidad)\\s+([^,]+))?",
                Pattern.CASE_INSENSITIVE
        );
        Matcher crearRelacionMatcher = crearRelacionPattern.matcher(p);
        if (crearRelacionMatcher.find()) {
            String origen = capitalize(crearRelacionMatcher.group(1));
            String destino = capitalize(crearRelacionMatcher.group(2));
            String tipoRelRaw = crearRelacionMatcher.group(3);
            String cardRaw = crearRelacionMatcher.group(4);

            String tipoRel = inferirTipoRelacion(tipoRelRaw != null ? tipoRelRaw : lower);
            String[] cards = parseCardinalidad(cardRaw != null ? cardRaw : lower);

            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.CREATE_RELATION)
                    .claseOrigen(origen)
                    .claseDestino(destino)
                    .tipoRelacion(tipoRel)
                    .cardinalidadOrigen(cards[0])
                    .cardinalidadDestino(cards[1])
                    .descripcion("Relación " + tipoRel + " entre " + origen + " y " + destino)
                    .explicacion("Crear relación " + tipoRel + " de '" + origen + "' (" + cards[0] + ") a '" + destino + "' (" + cards[1] + ")")
                    .build();
        }

        // Patrón alternativo para herencia directa: "Hacer que Empleado herede de Persona"
        Pattern herenciaPattern = Pattern.compile(
                "(?:hacer\\s+que\\s+)?([A-Za-z0-9_]+)\\s+hered[ae]?\\s+de\\s+([A-Za-z0-9_]+)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher herenciaMatcher = herenciaPattern.matcher(p);
        if (herenciaMatcher.find()) {
            String hijo = capitalize(herenciaMatcher.group(1));
            String padre = capitalize(herenciaMatcher.group(2));
            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.CREATE_RELATION)
                    .claseOrigen(hijo)
                    .claseDestino(padre)
                    .tipoRelacion("HERENCIA")
                    .cardinalidadOrigen("1")
                    .cardinalidadDestino("1")
                    .descripcion("Herencia: " + hijo + " hereda de " + padre)
                    .explicacion("Crear relación de HERENCIA: '" + hijo + "' hereda de '" + padre + "'")
                    .build();
        }

        // ---------------------------------------------------------------------
        // CASO 9: Modificar relación / cardinalidad
        // Ej: "Cambiar relación Cliente-Venta a 1:N",
        //     "Cambiar cardinalidad de Cliente con Venta a 1..*"
        // ---------------------------------------------------------------------
        Pattern updateRelPattern = Pattern.compile(
                "(?:cambiar|modificar)\\s+(?:la\\s+)?(?:relacion|cardinalidad)\\s+(?:de\\s+)?([A-Za-z0-9_]+)[\\s\\-_]+([A-Za-z0-9_]+)\\s+a\\s+(.+)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher updateRelMatcher = updateRelPattern.matcher(p);
        if (updateRelMatcher.find()) {
            String origen = capitalize(updateRelMatcher.group(1));
            String destino = capitalize(updateRelMatcher.group(2));
            String cardNueva = updateRelMatcher.group(3);
            String[] cards = parseCardinalidad(cardNueva);

            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.UPDATE_RELATION)
                    .claseOrigen(origen)
                    .claseDestino(destino)
                    .cardinalidadOrigen(cards[0])
                    .cardinalidadDestino(cards[1])
                    .explicacion("Modificar cardinalidad de relación '" + origen + "' - '" + destino + "' a " + cards[0] + " : " + cards[1])
                    .build();
        }

        // ---------------------------------------------------------------------
        // CASO 10: Eliminar relación
        // Ej: "Eliminar relación entre Cliente y Venta", "Borrar relación Cliente-Venta"
        // ---------------------------------------------------------------------
        Pattern deleteRelPattern = Pattern.compile(
                "(?:eliminar|borrar|quitar)\\s+(?:la\\s+)?relacion\\s+(?:entre\\s+)?([A-Za-z0-9_]+)(?:\\s+(?:con|y)\\s+|[\\-_]+)([A-Za-z0-9_]+)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher deleteRelMatcher = deleteRelPattern.matcher(p);
        if (deleteRelMatcher.find()) {
            String origen = capitalize(deleteRelMatcher.group(1));
            String destino = capitalize(deleteRelMatcher.group(2));
            return ParsedAIAction.builder()
                    .tipoOperacion(TipoOperacionAI.DELETE_RELATION)
                    .claseOrigen(origen)
                    .claseDestino(destino)
                    .explicacion("Eliminar relación entre '" + origen + "' y '" + destino + "'")
                    .build();
        }

        // ---------------------------------------------------------------------
        // CASO INCIERTO / DESCONOCIDO
        // ---------------------------------------------------------------------
        return ParsedAIAction.builder()
                .tipoOperacion(TipoOperacionAI.UNKNOWN)
                .explicacion("No se reconoció una instrucción válida de manipulación UML para: \"" + prompt + "\"")
                .build();
    }

    private List<ParsedAIAction.AtributoSimple> parseListaAtributos(String text) {
        List<ParsedAIAction.AtributoSimple> list = new ArrayList<>();
        // Divide por comas o conjunciones 'y'
        String[] parts = text.split(",|\\s+y\\s+");
        for (String part : parts) {
            String clean = part.trim();
            if (clean.startsWith("atributo")) {
                clean = clean.replaceFirst("^atributo\\s+", "").trim();
            }
            String[] tokens = clean.split("\\s+");
            if (tokens.length >= 2) {
                String nombre = tokens[0];
                String tipo = normalizarTipoDato(tokens[1]);
                list.add(ParsedAIAction.AtributoSimple.builder()
                        .nombre(nombre)
                        .tipo(tipo)
                        .visibilidad("PRIVATE")
                        .build());
            } else if (tokens.length == 1 && !tokens[0].isBlank()) {
                list.add(ParsedAIAction.AtributoSimple.builder()
                        .nombre(tokens[0])
                        .tipo("String")
                        .visibilidad("PRIVATE")
                        .build());
            }
        }
        return list;
    }

    private String inferirTipoRelacion(String text) {
        String l = text.toLowerCase();
        if (l.contains("herencia") || l.contains("hereda") || l.contains("subclase")) {
            return "HERENCIA";
        }
        if (l.contains("agregacion") || l.contains("agrega")) {
            return "AGREGACION";
        }
        if (l.contains("composicion") || l.contains("compone")) {
            return "COMPOSICION";
        }
        if (l.contains("dependencia") || l.contains("depende")) {
            return "DEPENDENCIA";
        }
        return "ASOCIACION";
    }

    private String[] parseCardinalidad(String text) {
        String l = text.toLowerCase();
        if (l.contains("uno a muchos") || l.contains("1 a *") || l.contains("1:*") || l.contains("1:n") || l.contains("1..*")) {
            return new String[]{"1", "*"};
        }
        if (l.contains("muchos a muchos") || l.contains("* a *") || l.contains("*:*") || l.contains("n:n") || l.contains("n:m") || l.contains("*..*")) {
            return new String[]{"*", "*"};
        }
        if (l.contains("uno a uno") || l.contains("1 a 1") || l.contains("1:1") || l.contains("1..1")) {
            return new String[]{"1", "1"};
        }
        if (l.contains("cero a uno") || l.contains("0 a 1") || l.contains("0:1") || l.contains("0..1")) {
            return new String[]{"0..1", "1"};
        }
        if (l.contains("cero a muchos") || l.contains("0 a *") || l.contains("0:*") || l.contains("0..*")) {
            return new String[]{"0..1", "*"};
        }
        return new String[]{"1", "*"};
    }

    private String normalizarTipoDato(String raw) {
        if (raw == null || raw.isBlank()) return "String";
        String l = raw.trim().toLowerCase();
        switch (l) {
            case "string":
            case "texto":
            case "cadena":
            case "varchar":
                return "String";
            case "int":
            case "integer":
            case "entero":
                return "Integer";
            case "long":
                return "Long";
            case "double":
            case "decimal":
            case "float":
            case "real":
                return "Double";
            case "boolean":
            case "bool":
            case "booleano":
                return "Boolean";
            case "date":
            case "fecha":
                return "LocalDate";
            case "datetime":
            case "timestamp":
                return "LocalDateTime";
            default:
                return capitalize(raw.trim());
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isBlank()) return str;
        String s = str.trim();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
