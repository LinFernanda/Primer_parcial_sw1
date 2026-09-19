package com.caseplatform.imageuml;

import com.caseplatform.imageuml.dto.AtributoDetectadoDTO;
import com.caseplatform.imageuml.dto.ClaseDetectadaDTO;
import com.caseplatform.imageuml.dto.MetodoDetectadoDTO;
import com.caseplatform.imageuml.dto.RelacionDetectadaDTO;
import com.caseplatform.imageuml.parser.UMLTextParser;
import com.caseplatform.model.TipoRelacionUML;
import com.caseplatform.model.VisibilidadUML;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - UMLTextParser (OCR y Sintaxis UML)")
class UMLTextParserTest {

    private UMLTextParser parser;

    @BeforeEach
    void setUp() {
        parser = new UMLTextParser();
    }

    @Test
    @DisplayName("Debe parsear clases, atributos y métodos desde texto estructurado de diagrama")
    void testParseClassesFromText_Exitoso() {
        String diagramaTexto = """
                ----------------
                Cliente
                ----------------
                - id: Long
                + nombre: String
                # edad: int = 18
                ----------------
                + calcularTotal(): Double
                + pagar(monto: Double): Boolean
                ----------------
                """;

        List<ClaseDetectadaDTO> clases = parser.parseClassesFromText(diagramaTexto);

        assertNotNull(clases);
        assertEquals(1, clases.size());

        ClaseDetectadaDTO cliente = clases.get(0);
        assertEquals("Cliente", cliente.getNombre());
        assertEquals(3, cliente.getAtributos().size());

        AtributoDetectadoDTO idAttr = cliente.getAtributos().get(0);
        assertEquals("id", idAttr.getNombre());
        assertEquals("Long", idAttr.getTipoDato());
        assertEquals(VisibilidadUML.PRIVATE, idAttr.getVisibilidad());

        AtributoDetectadoDTO edadAttr = cliente.getAtributos().get(2);
        assertEquals("edad", edadAttr.getNombre());
        assertEquals("Integer", edadAttr.getTipoDato()); // normalizado de int
        assertEquals(VisibilidadUML.PROTECTED, edadAttr.getVisibilidad());
        assertEquals("18", edadAttr.getValorInicial());

        assertEquals(2, cliente.getMetodos().size());
        MetodoDetectadoDTO calcMetodo = cliente.getMetodos().get(0);
        assertEquals("calcularTotal", calcMetodo.getNombre());
        assertEquals("Double", calcMetodo.getTipoRetorno());
        assertEquals(VisibilidadUML.PUBLIC, calcMetodo.getVisibilidad());
    }

    @Test
    @DisplayName("Debe detectar y extraer relaciones UML y cardinalidades (1, 0..1, *, 1..*)")
    void testParseRelationsFromText_ConCardinalidades() {
        String diagramaTexto = """
                Cliente 1 -- * Venta : realiza
                Empresa 1 *-- 1..* Departamento : contiene
                Usuario <|-- Administrador
                Servicio ..> Repositorio
                """;

        List<RelacionDetectadaDTO> relaciones = parser.parseRelationsFromText(diagramaTexto);

        assertNotNull(relaciones);
        assertEquals(4, relaciones.size());

        // Relación 1: Asociación Cliente 1 -- * Venta
        RelacionDetectadaDTO rel1 = relaciones.get(0);
        assertEquals("Cliente", rel1.getClaseOrigen());
        assertEquals("Venta", rel1.getClaseDestino());
        assertEquals(TipoRelacionUML.ASOCIACION, rel1.getTipoRelacion());
        assertEquals("1", rel1.getCardinalidadOrigen());
        assertEquals("*", rel1.getCardinalidadDestino());

        // Relación 2: Composición Empresa 1 *-- 1..* Departamento
        RelacionDetectadaDTO rel2 = relaciones.get(1);
        assertEquals("Empresa", rel2.getClaseOrigen());
        assertEquals("Departamento", rel2.getClaseDestino());
        assertEquals(TipoRelacionUML.COMPOSICION, rel2.getTipoRelacion());
        assertEquals("1", rel2.getCardinalidadOrigen());
        assertEquals("1..*", rel2.getCardinalidadDestino());

        // Relación 3: Herencia Usuario <|-- Administrador
        RelacionDetectadaDTO rel3 = relaciones.get(2);
        assertEquals("Usuario", rel3.getClaseOrigen());
        assertEquals("Administrador", rel3.getClaseDestino());
        assertEquals(TipoRelacionUML.HERENCIA, rel3.getTipoRelacion());

        // Relación 4: Dependencia Servicio ..> Repositorio
        RelacionDetectadaDTO rel4 = relaciones.get(3);
        assertEquals("Servicio", rel4.getClaseOrigen());
        assertEquals("Repositorio", rel4.getClaseDestino());
        assertEquals(TipoRelacionUML.DEPENDENCIA, rel4.getTipoRelacion());
    }

    @Test
    @DisplayName("Debe normalizar tipos de datos estándar")
    void testNormalizeDataType() {
        assertEquals("String", parser.normalizeDataType("text"));
        assertEquals("Integer", parser.normalizeDataType("int"));
        assertEquals("Long", parser.normalizeDataType("bigint"));
        assertEquals("Double", parser.normalizeDataType("float"));
        assertEquals("Boolean", parser.normalizeDataType("bool"));
        assertEquals("Date", parser.normalizeDataType("datetime"));
    }

    @Test
    @DisplayName("Debe normalizar cardinalidades UML")
    void testNormalizeCardinality() {
        assertEquals("1", parser.normalizeCardinality("1"));
        assertEquals("0..1", parser.normalizeCardinality("0..1"));
        assertEquals("*", parser.normalizeCardinality("many"));
        assertEquals("*", parser.normalizeCardinality("n"));
        assertEquals("1..*", parser.normalizeCardinality("1..n"));
    }

    @Test
    @DisplayName("Debe parsear clases con ruido de bordes OCR (| y +----+)")
    void testParseClassesWithOcrBoxBorders() {
        String diagramaTexto = """
                +------------------------+
                |      Factura           |
                +------------------------+
                | - numero: String       |
                | - total: Double        |
                | - pagada: boolean      |
                +------------------------+
                | + pagar(): void        |
                +------------------------+
                """;

        List<ClaseDetectadaDTO> clases = parser.parseClassesFromText(diagramaTexto);
        assertNotNull(clases);
        assertEquals(1, clases.size());

        ClaseDetectadaDTO factura = clases.get(0);
        assertEquals("Factura", factura.getNombre());
        assertEquals(3, factura.getAtributos().size());
        assertEquals("numero", factura.getAtributos().get(0).getNombre());
        assertEquals("total", factura.getAtributos().get(1).getNombre());
        assertEquals("pagada", factura.getAtributos().get(2).getNombre());
        assertEquals("Boolean", factura.getAtributos().get(2).getTipoDato());
        assertEquals(1, factura.getMetodos().size());
        assertEquals("pagar", factura.getMetodos().get(0).getNombre());
    }

    @Test
    @DisplayName("Debe parsear clases en formato PlantUML / bloques con llaves")
    void testParseClassesPlantUML() {
        String plantUml = """
                class Usuario {
                  - id: Long
                  - email: String
                  + login(): Boolean
                }

                class Rol {
                  - codigo: String
                  - descripcion: String
                }

                Usuario *-- 1..* Rol : posee
                """;

        List<ClaseDetectadaDTO> clases = parser.parseClassesFromText(plantUml);
        assertEquals(2, clases.size());
        assertEquals("Usuario", clases.get(0).getNombre());
        assertEquals("Rol", clases.get(1).getNombre());

        List<RelacionDetectadaDTO> relaciones = parser.parseRelationsFromText(plantUml);
        assertEquals(1, relaciones.size());
        assertEquals("Usuario", relaciones.get(0).getClaseOrigen());
        assertEquals("Rol", relaciones.get(0).getClaseDestino());
        assertEquals(TipoRelacionUML.COMPOSICION, relaciones.get(0).getTipoRelacion());
    }

    @Test
    @DisplayName("Debe detectar relaciones expresadas textualmente en lenguaje natural")
    void testParseTextualRelations() {
        String texto = """
                Administrador hereda de Persona
                Pedido contiene Item
                Auto tiene Motor
                """;

        List<RelacionDetectadaDTO> relaciones = parser.parseRelationsFromText(texto);
        assertEquals(3, relaciones.size());
        assertEquals(TipoRelacionUML.HERENCIA, relaciones.get(0).getTipoRelacion());
        assertEquals(TipoRelacionUML.COMPOSICION, relaciones.get(1).getTipoRelacion());
        assertEquals(TipoRelacionUML.AGREGACION, relaciones.get(2).getTipoRelacion());
    }
}
