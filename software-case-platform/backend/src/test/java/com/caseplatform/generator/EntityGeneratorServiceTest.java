package com.caseplatform.generator;

import com.caseplatform.generator.builder.EntityGeneratorService;
import com.caseplatform.generator.builder.EntityGeneratorServiceImpl;
import com.caseplatform.generator.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias: EntityGeneratorService (Fase 9)")
class EntityGeneratorServiceTest {

    private EntityGeneratorService entityGeneratorService;

    @BeforeEach
    void setUp() {
        entityGeneratorService = new EntityGeneratorServiceImpl();
    }

    @Test
    @DisplayName("Debe generar código Java con anotaciones JPA y Lombok para la entidad")
    void testGenerateEntityWithFieldsAndRelations() {
        GeneratedEntityModel entityModel = GeneratedEntityModel.builder()
                .name("Producto")
                .tableName("productos")
                .description("Catálogo de productos")
                .primaryKeyName("id")
                .primaryKeyType("Long")
                .fields(List.of(
                        GeneratedFieldModel.builder().name("id").javaType("Long").columnName("id").isId(true).isGenerated(true).build(),
                        GeneratedFieldModel.builder().name("nombre").javaType("String").columnName("nombre").nullable(false).build(),
                        GeneratedFieldModel.builder().name("precio").javaType("Double").columnName("precio").nullable(false).build()
                ))
                .relations(List.of(
                        GeneratedRelationModel.builder()
                                .tipoRelacionJPA(TipoRelacionJPA.MANY_TO_ONE)
                                .fieldName("categoria")
                                .targetEntity("Categoria")
                                .joinColumnName("categoria_id")
                                .build()
                ))
                .build();

        GeneratedFile file = entityGeneratorService.generateEntity("com.empresa.tienda", entityModel);

        assertNotNull(file);
        assertEquals("src/main/java/com/empresa/tienda/entity/Producto.java", file.getRelativePath());
        assertEquals(CategoryFileType.ENTITY, file.getCategory());

        String code = file.getContent();
        assertTrue(code.contains("package com.empresa.tienda.entity;"));
        assertTrue(code.contains("@Entity"));
        assertTrue(code.contains("@Table(name = \"productos\")"));
        assertTrue(code.contains("@Data"));
        assertTrue(code.contains("@Id"));
        assertTrue(code.contains("@GeneratedValue(strategy = GenerationType.IDENTITY)"));
        assertTrue(code.contains("private Long id;"));
        assertTrue(code.contains("private String nombre;"));
        assertTrue(code.contains("private Double precio;"));
        assertTrue(code.contains("@ManyToOne"));
        assertTrue(code.contains("@JoinColumn(name = \"categoria_id\")"));
        assertTrue(code.contains("private Categoria categoria;"));
    }
}
