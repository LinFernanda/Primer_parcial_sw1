package com.caseplatform;

import com.caseplatform.ai.command.ParsedAIAction;
import com.caseplatform.ai.config.AIConfig;
import com.caseplatform.ai.model.TipoOperacionAI;
import com.caseplatform.ai.parser.AICommandParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AICommandParserTest {

    private AICommandParser parser;

    @BeforeEach
    void setUp() {
        AIConfig aiConfig = new AIConfig();
        // Sin API Key para evaluar el motor determinístico local de NLP
        aiConfig.setApiKey("");
        ObjectMapper objectMapper = new ObjectMapper();
        parser = new AICommandParser(aiConfig, objectMapper);
    }

    @Test
    @DisplayName("Debe parsear 'crear clase Factura' a CREATE_CLASS")
    void testCrearClaseSimple() {
        ParsedAIAction action = parser.parse("crear clase Factura");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.CREATE_CLASS, action.getTipoOperacion());
        assertEquals("Factura", action.getNombreClase());
        assertFalse(action.isRequiereConfirmacion());
    }

    @Test
    @DisplayName("Debe parsear clase con múltiples atributos y tipos de datos")
    void testCrearClaseConAtributos() {
        ParsedAIAction action = parser.parse("crear clase Cliente con atributos nombre String y edad Integer");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.CREATE_CLASS, action.getTipoOperacion());
        assertEquals("Cliente", action.getNombreClase());
        assertNotNull(action.getAtributos());
        assertEquals(2, action.getAtributos().size());
        assertEquals("nombre", action.getAtributos().get(0).getNombre());
        assertEquals("String", action.getAtributos().get(0).getTipo());
        assertEquals("edad", action.getAtributos().get(1).getNombre());
        assertEquals("Integer", action.getAtributos().get(1).getTipo());
    }

    @Test
    @DisplayName("Debe parsear 'renombrar clase Cliente a ClienteVip' a UPDATE_CLASS")
    void testRenombrarClase() {
        ParsedAIAction action = parser.parse("renombrar clase Cliente a ClienteVip");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.UPDATE_CLASS, action.getTipoOperacion());
        assertEquals("Cliente", action.getNombreClase());
        assertEquals("ClienteVip", action.getNuevoNombreClase());
    }

    @Test
    @DisplayName("Debe parsear 'eliminar clase Factura' a DELETE_CLASS")
    void testEliminarClase() {
        ParsedAIAction action = parser.parse("eliminar clase Factura");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.DELETE_CLASS, action.getTipoOperacion());
        assertEquals("Factura", action.getNombreClase());
    }

    @Test
    @DisplayName("Debe parsear 'agregar atributo email de tipo String a la clase Cliente' a CREATE_ATTRIBUTE")
    void testAgregarAtributo() {
        ParsedAIAction action = parser.parse("agregar atributo email de tipo String a la clase Cliente");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.CREATE_ATTRIBUTE, action.getTipoOperacion());
        assertEquals("Cliente", action.getNombreClase());
        assertEquals("email", action.getNombreAtributo());
        assertEquals("String", action.getTipoDatoAtributo());
    }

    @Test
    @DisplayName("Debe parsear 'eliminar atributo telefono en Cliente' a DELETE_ATTRIBUTE")
    void testEliminarAtributo() {
        ParsedAIAction action = parser.parse("eliminar atributo telefono en Cliente");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.DELETE_ATTRIBUTE, action.getTipoOperacion());
        assertEquals("Cliente", action.getNombreClase());
        assertEquals("telefono", action.getNombreAtributo());
    }

    @Test
    @DisplayName("Debe parsear relación de asociación con cardinalidad 1 a *")
    void testCrearRelacionAsociacion() {
        ParsedAIAction action = parser.parse("crear relacion entre Cliente y Pedido con cardinalidad 1 a *");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.CREATE_RELATION, action.getTipoOperacion());
        assertEquals("Cliente", action.getClaseOrigen());
        assertEquals("Pedido", action.getClaseDestino());
        assertEquals("ASOCIACION", action.getTipoRelacion());
        assertEquals("1", action.getCardinalidadOrigen());
        assertEquals("*", action.getCardinalidadDestino());
    }

    @Test
    @DisplayName("Debe parsear herencia directa 'Empleado hereda de Persona'")
    void testCrearRelacionHerencia() {
        ParsedAIAction action = parser.parse("Empleado hereda de Persona");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.CREATE_RELATION, action.getTipoOperacion());
        assertEquals("Empleado", action.getClaseOrigen());
        assertEquals("Persona", action.getClaseDestino());
        assertEquals("HERENCIA", action.getTipoRelacion());
    }

    @Test
    @DisplayName("Debe detectar comandos ambiguos como 'Crear factura' y solicitar confirmación")
    void testAmbiguedadRequiereConfirmacion() {
        ParsedAIAction action = parser.parse("Crear factura");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.CONFIRMATION_REQUIRED, action.getTipoOperacion());
        assertTrue(action.isRequiereConfirmacion());
        assertNotNull(action.getPreguntaConfirmacion());
        assertTrue(action.getPreguntaConfirmacion().contains("Factura"));
    }

    @Test
    @DisplayName("Debe rechazar solicitudes de generación completa de sistemas (restricción estricta Fase 7)")
    void testRechazoGeneracionSistemaCompleto() {
        ParsedAIAction action = parser.parse("generar sistema completo de facturación y contabilidad");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.UNKNOWN, action.getTipoOperacion());
        assertTrue(action.getExplicacion().contains("No genera sistemas completos desde cero"));
    }

    @Test
    @DisplayName("Debe orquestar con Groq AI cuando la API Key está configurada")
    void testParseWithGroqAI() {
        AIConfig groqConfig = new AIConfig();
        groqConfig.setGroqApiKey("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");
        groqConfig.setGroqModel("openai/gpt-oss-120b");
        groqConfig.setGroqBaseUrl("https://api.groq.com/openai/v1");

        AICommandParser groqParser = new AICommandParser(groqConfig, new ObjectMapper());
        ParsedAIAction action = groqParser.parse("crear clase Producto con atributo precio Double");

        assertNotNull(action);
        assertEquals(TipoOperacionAI.CREATE_CLASS, action.getTipoOperacion());
        assertEquals("Producto", action.getNombreClase());
    }
}
