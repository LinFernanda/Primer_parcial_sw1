package com.caseplatform.integration.enterprisearchitect;

import com.caseplatform.integration.enterprisearchitect.exporter.XMIExporterImpl;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportClass;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportModel;
import com.caseplatform.integration.enterprisearchitect.parser.XMIParserImpl;
import com.caseplatform.model.AtributoUML;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.MetodoUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.RelacionUML;
import com.caseplatform.model.TipoRelacionUML;
import com.caseplatform.model.VisibilidadUML;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XMIExporterTest {

    private XMIExporterImpl exporter;
    private XMIParserImpl parser;

    @BeforeEach
    void setUp() {
        exporter = new XMIExporterImpl();
        parser = new XMIParserImpl();
    }

    @Test
    @DisplayName("Debe exportar un modelo UML a XMI 2.1 y permitir re-importación coherente (Round-Trip)")
    void testExportAndRoundTripImport() throws Exception {
        ModeloUML modelo = ModeloUML.builder()
                .id(10L)
                .nombre("SistemaVentas")
                .build();

        ClaseUML producto = ClaseUML.builder()
                .id(101L)
                .nombre("Producto")
                .visibilidad(VisibilidadUML.PUBLIC)
                .posicionX(120.0)
                .posicionY(80.0)
                .modeloUML(modelo)
                .atributos(new ArrayList<>())
                .metodos(new ArrayList<>())
                .build();

        AtributoUML attrNombre = AtributoUML.builder()
                .id(201L)
                .nombre("nombre")
                .tipoDato("String")
                .visibilidad(VisibilidadUML.PRIVATE)
                .claseUML(producto)
                .build();

        AtributoUML attrPrecio = AtributoUML.builder()
                .id(202L)
                .nombre("precio")
                .tipoDato("Double")
                .visibilidad(VisibilidadUML.PRIVATE)
                .claseUML(producto)
                .build();

        producto.getAtributos().add(attrNombre);
        producto.getAtributos().add(attrPrecio);

        MetodoUML metodoActualizar = MetodoUML.builder()
                .id(301L)
                .nombre("actualizarPrecio")
                .tipoRetorno("Boolean")
                .visibilidad(VisibilidadUML.PUBLIC)
                .parametros("nuevoPrecio: Double")
                .claseUML(producto)
                .build();
        producto.getMetodos().add(metodoActualizar);

        ClaseUML categoria = ClaseUML.builder()
                .id(102L)
                .nombre("Categoria")
                .visibilidad(VisibilidadUML.PUBLIC)
                .posicionX(450.0)
                .posicionY(80.0)
                .modeloUML(modelo)
                .atributos(new ArrayList<>())
                .metodos(new ArrayList<>())
                .build();

        RelacionUML relacion = RelacionUML.builder()
                .id(401L)
                .tipoRelacion(TipoRelacionUML.AGREGACION)
                .claseOrigen(producto)
                .claseDestino(categoria)
                .cardinalidadOrigen("1")
                .cardinalidadDestino("0..*")
                .descripcion("perteneceA")
                .modeloUML(modelo)
                .build();

        modelo.setClases(List.of(producto, categoria));
        modelo.setRelaciones(List.of(relacion));

        // 1. Exportar a XMI
        String xml = exporter.exportToXMI(modelo);
        assertNotNull(xml);
        assertTrue(xml.contains("xmi:version=\"2.1\""));
        assertTrue(xml.contains("SistemaVentas"));
        assertTrue(xml.contains("name=\"Producto\""));
        assertTrue(xml.contains("name=\"Categoria\""));
        assertTrue(xml.contains("name=\"precio\""));
        assertTrue(xml.contains("name=\"actualizarPrecio\""));
        assertTrue(xml.contains("extender=\"Enterprise Architect\""));
        assertTrue(xml.contains("left=120"));

        // 2. Round-trip: Analizar el XML exportado con XMIParser
        UMLImportModel reImported = parser.parse(xml);
        assertNotNull(reImported);
        assertEquals(2, reImported.getClases().size());
        assertEquals(1, reImported.getRelaciones().size());

        UMLImportClass prodImportado = reImported.getClases().stream()
                .filter(c -> c.getNombre().equals("Producto"))
                .findFirst()
                .orElse(null);
        assertNotNull(prodImportado);
        assertEquals(2, prodImportado.getAtributos().size());
        assertEquals(1, prodImportado.getMetodos().size());
        assertEquals(120.0, prodImportado.getPosicionX());
    }
}
