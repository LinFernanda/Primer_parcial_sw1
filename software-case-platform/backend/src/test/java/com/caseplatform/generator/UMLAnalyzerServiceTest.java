package com.caseplatform.generator;

import com.caseplatform.generator.analyzer.UMLAnalyzerService;
import com.caseplatform.generator.analyzer.UMLAnalyzerServiceImpl;
import com.caseplatform.generator.dto.GeneratorRequestDTO;
import com.caseplatform.generator.model.*;
import com.caseplatform.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias: UMLAnalyzerService (Fase 9)")
class UMLAnalyzerServiceTest {

    private UMLAnalyzerService umlAnalyzerService;

    @BeforeEach
    void setUp() {
        umlAnalyzerService = new UMLAnalyzerServiceImpl();
    }

    @Test
    @DisplayName("Debe analizar clases, normalizar nombres y mapear tipos de datos primitivos a Java")
    void testAnalyzeClasesAndDataTypes() {
        ModeloUML modelo = ModeloUML.builder()
                .id(1L)
                .nombre("Sistema E-Commerce")
                .clases(new ArrayList<>())
                .relaciones(new ArrayList<>())
                .build();

        ClaseUML cliente = ClaseUML.builder()
                .id(10L)
                .nombre("Cliente")
                .posicionX(100.0)
                .posicionY(100.0)
                .atributos(new ArrayList<>(List.of(
                        AtributoUML.builder().id(1L).nombre("nombre").tipoDato("String").visibilidad(VisibilidadUML.PRIVATE).build(),
                        AtributoUML.builder().id(2L).nombre("edad").tipoDato("int").visibilidad(VisibilidadUML.PRIVATE).build(),
                        AtributoUML.builder().id(3L).nombre("activo").tipoDato("boolean").visibilidad(VisibilidadUML.PUBLIC).build(),
                        AtributoUML.builder().id(4L).nombre("saldo").tipoDato("BigDecimal").visibilidad(VisibilidadUML.PRIVATE).build(),
                        AtributoUML.builder().id(5L).nombre("fechaRegistro").tipoDato("date").visibilidad(VisibilidadUML.PRIVATE).build()
                )))
                .build();

        modelo.getClases().add(cliente);

        GeneratedProjectModel project = umlAnalyzerService.analyze(modelo, GeneratorRequestDTO.builder().build());

        assertNotNull(project);
        assertEquals("SistemaECommerce", project.getProjectName());
        assertEquals(1, project.getEntities().size());

        GeneratedEntityModel entity = project.getEntities().get(0);
        assertEquals("Cliente", entity.getName());
        assertEquals("clientes", entity.getTableName());

        // Debe haber inyectado automáticamente el campo id si no existía
        assertTrue(entity.getFields().stream().anyMatch(f -> f.isId() && "id".equals(f.getName())));

        // Verificar tipos Java mapeados
        assertEquals("String", findField(entity, "nombre").getJavaType());
        assertEquals("Integer", findField(entity, "edad").getJavaType());
        assertEquals("Boolean", findField(entity, "activo").getJavaType());
        assertEquals("java.math.BigDecimal", findField(entity, "saldo").getJavaType());
        assertEquals("java.time.LocalDate", findField(entity, "fechaRegistro").getJavaType());
    }

    @Test
    @DisplayName("Debe interpretar cardinalidad 1 a * como @OneToMany y @ManyToOne")
    void testAnalyzeOneToManyRelationship() {
        ClaseUML cliente = ClaseUML.builder().id(1L).nombre("Cliente").atributos(new ArrayList<>()).build();
        ClaseUML pedido = ClaseUML.builder().id(2L).nombre("Pedido").atributos(new ArrayList<>()).build();

        RelacionUML relacion = RelacionUML.builder()
                .id(100L)
                .claseOrigen(cliente)
                .claseDestino(pedido)
                .tipoRelacion(TipoRelacionUML.ASOCIACION)
                .cardinalidadOrigen("1")
                .cardinalidadDestino("*")
                .descripcion("cliente realiza pedidos")
                .build();

        ModeloUML modelo = ModeloUML.builder()
                .id(1L)
                .nombre("Ventas")
                .clases(List.of(cliente, pedido))
                .relaciones(List.of(relacion))
                .build();

        GeneratedProjectModel project = umlAnalyzerService.analyze(modelo, null);

        GeneratedEntityModel clienteEntity = project.getEntities().stream()
                .filter(e -> "Cliente".equals(e.getName())).findFirst().orElseThrow();
        GeneratedEntityModel pedidoEntity = project.getEntities().stream()
                .filter(e -> "Pedido".equals(e.getName())).findFirst().orElseThrow();

        // En Cliente debe existir relación ONE_TO_MANY apuntando a Pedido
        GeneratedRelationModel oneToMany = clienteEntity.getRelations().stream()
                .filter(r -> r.getTipoRelacionJPA() == TipoRelacionJPA.ONE_TO_MANY).findFirst().orElseThrow();
        assertEquals("Pedido", oneToMany.getTargetEntity());
        assertEquals("cliente", oneToMany.getMappedBy());

        // En Pedido debe existir relación MANY_TO_ONE apuntando a Cliente
        GeneratedRelationModel manyToOne = pedidoEntity.getRelations().stream()
                .filter(r -> r.getTipoRelacionJPA() == TipoRelacionJPA.MANY_TO_ONE).findFirst().orElseThrow();
        assertEquals("Cliente", manyToOne.getTargetEntity());
        assertEquals("cliente_id", manyToOne.getJoinColumnName());
    }

    @Test
    @DisplayName("Debe interpretar cardinalidad * a * como @ManyToMany")
    void testAnalyzeManyToManyRelationship() {
        ClaseUML estudiante = ClaseUML.builder().id(1L).nombre("Estudiante").atributos(new ArrayList<>()).build();
        ClaseUML curso = ClaseUML.builder().id(2L).nombre("Curso").atributos(new ArrayList<>()).build();

        RelacionUML relacion = RelacionUML.builder()
                .id(101L)
                .claseOrigen(estudiante)
                .claseDestino(curso)
                .tipoRelacion(TipoRelacionUML.ASOCIACION)
                .cardinalidadOrigen("*")
                .cardinalidadDestino("*")
                .build();

        ModeloUML modelo = ModeloUML.builder()
                .id(2L)
                .nombre("Universidad")
                .clases(List.of(estudiante, curso))
                .relaciones(List.of(relacion))
                .build();

        GeneratedProjectModel project = umlAnalyzerService.analyze(modelo, null);

        GeneratedEntityModel estEntity = project.getEntities().stream()
                .filter(e -> "Estudiante".equals(e.getName())).findFirst().orElseThrow();

        assertTrue(estEntity.getRelations().stream()
                .anyMatch(r -> r.getTipoRelacionJPA() == TipoRelacionJPA.MANY_TO_MANY && "Curso".equals(r.getTargetEntity())));
    }

    private GeneratedFieldModel findField(GeneratedEntityModel entity, String name) {
        return entity.getFields().stream()
                .filter(f -> name.equalsIgnoreCase(f.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Campo no encontrado: " + name));
    }
}
