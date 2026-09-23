package com.caseplatform.imageuml;

import com.caseplatform.imageuml.detector.UMLDetectorServiceImpl;
import com.caseplatform.imageuml.dto.ClaseDetectadaDTO;
import com.caseplatform.imageuml.dto.ImageUMLDetectedDTO;
import com.caseplatform.imageuml.dto.RelacionDetectadaDTO;
import com.caseplatform.model.TipoRelacionUML;
import com.caseplatform.imageuml.parser.UMLTextParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Resolución Automática de Relaciones N:M a Entidades Asociativas")
class UMLDetectorManyToManyTest {

    private UMLDetectorServiceImpl detectorService;

    @BeforeEach
    void setUp() {
        detectorService = new UMLDetectorServiceImpl(null, new UMLTextParser(), new ObjectMapper());
    }

    @Test
    @DisplayName("Debe descomponer N:M entre Estudiante y Curso en entidad asociativa Inscripcion con FKs y atributos contextuales")
    void testResolveManyToManyEstudianteCurso() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageUMLDetectedDTO result = detectorService.detectUMLFromImage(img, new byte[0], "diagrama_estudiantes_cursos.png");

        assertNotNull(result);
        assertNotNull(result.getClases());
        assertEquals(3, result.getClases().size(), "Deben existir 3 clases: Estudiante, Curso e Inscripcion");

        ClaseDetectadaDTO asociativa = result.getClases().stream()
                .filter(c -> "Inscripcion".equalsIgnoreCase(c.getNombre()))
                .findFirst()
                .orElse(null);

        assertNotNull(asociativa, "La entidad intermedia Inscripcion debe haber sido creada automáticamente");

        // Validar Primary Key y Foreign Keys
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "id".equalsIgnoreCase(a.getNombre())));
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "id_estudiante".equalsIgnoreCase(a.getNombre())));
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "id_curso".equalsIgnoreCase(a.getNombre())));

        // Validar atributos contextuales
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "fecha_inscripcion".equalsIgnoreCase(a.getNombre())));
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "nota_final".equalsIgnoreCase(a.getNombre())));
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "estado".equalsIgnoreCase(a.getNombre())));

        // Validar relaciones 1:N resultantes
        assertEquals(2, result.getRelaciones().size(), "Deben reemplazarse por dos relaciones 1:N");
        for (RelacionDetectadaDTO rel : result.getRelaciones()) {
            assertEquals("Inscripcion", rel.getClaseDestino());
            assertEquals("1", rel.getCardinalidadOrigen());
            assertEquals("*", rel.getCardinalidadDestino());
        }
    }

    @Test
    @DisplayName("Debe descomponer N:M entre Venta y Producto desde texto OCR en Detalle_Venta con FKs y atributos como cantidad y precio")
    void testResolveManyToManyVentaProductoWithOcr() {
        String ocr = """
                ----------------
                Venta
                ----------------
                - id: Long
                ----------------
                Producto
                ----------------
                - id: Long
                - nombre: String
                ----------------
                Venta * -- * Producto
                """;

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageUMLDetectedDTO result = detectorService.detectUMLFromImage(img, new byte[0], "diagrama.png", ocr);

        assertNotNull(result);
        assertNotNull(result.getClases());

        ClaseDetectadaDTO asociativa = result.getClases().stream()
                .filter(c -> "Detalle_Venta".equalsIgnoreCase(c.getNombre()))
                .findFirst()
                .orElse(null);

        assertNotNull(asociativa, "La entidad intermedia Detalle_Venta debe haber sido creada");

        // Claves foráneas
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "id_venta".equalsIgnoreCase(a.getNombre())));
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "id_producto".equalsIgnoreCase(a.getNombre())));

        // Atributos contextuales de venta/producto
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "cantidad".equalsIgnoreCase(a.getNombre())));
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "precio_unitario".equalsIgnoreCase(a.getNombre())));
        assertTrue(asociativa.getAtributos().stream().anyMatch(a -> "subtotal".equalsIgnoreCase(a.getNombre())));

        // Relaciones 1:N
        assertEquals(2, result.getRelaciones().size());
        for (RelacionDetectadaDTO r : result.getRelaciones()) {
            assertEquals("Detalle_Venta", r.getClaseDestino());
            assertEquals("1", r.getCardinalidadOrigen());
            assertEquals("*", r.getCardinalidadDestino());
        }
    }
}
