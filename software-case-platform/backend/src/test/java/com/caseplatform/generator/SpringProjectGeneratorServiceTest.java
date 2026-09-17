package com.caseplatform.generator;

import com.caseplatform.generator.analyzer.UMLAnalyzerServiceImpl;
import com.caseplatform.generator.builder.*;
import com.caseplatform.generator.dto.GeneratedProjectPreviewDTO;
import com.caseplatform.generator.dto.GeneratorRequestDTO;
import com.caseplatform.generator.service.impl.BackendGeneratorServiceImpl;
import com.caseplatform.model.AtributoUML;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.ModeloUMLRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias: BackendGeneratorService y Empaquetado ZIP (Fase 9)")
class SpringProjectGeneratorServiceTest {

    @Mock
    private ModeloUMLRepository modeloUMLRepository;

    private BackendGeneratorServiceImpl generatorService;

    @BeforeEach
    void setUp() {
        generatorService = new BackendGeneratorServiceImpl(
                modeloUMLRepository,
                new UMLAnalyzerServiceImpl(),
                new EntityGeneratorServiceImpl(),
                new RepositoryGeneratorServiceImpl(),
                new DTOGeneratorServiceImpl(),
                new ServiceGeneratorServiceImpl(),
                new ControllerGeneratorServiceImpl(),
                new ProjectStructureBuilderImpl()
        );
    }

    @Test
    @DisplayName("Debe previsualizar el árbol completo de archivos de la arquitectura en capas")
    void testPreviewProject() {
        ModeloUML modelo = createSampleModel();
        when(modeloUMLRepository.findById(1L)).thenReturn(Optional.of(modelo));

        GeneratorRequestDTO request = GeneratorRequestDTO.builder()
                .packageName("com.caseplatform.demo")
                .projectName("DemoSystem")
                .build();

        GeneratedProjectPreviewDTO preview = generatorService.previewProject(1L, request);

        assertNotNull(preview);
        assertEquals(1, preview.getTotalEntities());
        assertTrue(preview.getTotalFiles() >= 10); // Entity, Repo, 2 DTOs, Service, Impl, Controller, Pom, Yml, App, Docker...

        // Verificar que existan archivos en todas las capas requeridas
        assertTrue(preview.getFiles().stream().anyMatch(f -> f.getRelativePath().endsWith("entity/Factura.java")));
        assertTrue(preview.getFiles().stream().anyMatch(f -> f.getRelativePath().endsWith("repository/FacturaRepository.java")));
        assertTrue(preview.getFiles().stream().anyMatch(f -> f.getRelativePath().endsWith("dto/FacturaRequestDTO.java")));
        assertTrue(preview.getFiles().stream().anyMatch(f -> f.getRelativePath().endsWith("dto/FacturaResponseDTO.java")));
        assertTrue(preview.getFiles().stream().anyMatch(f -> f.getRelativePath().endsWith("service/FacturaService.java")));
        assertTrue(preview.getFiles().stream().anyMatch(f -> f.getRelativePath().endsWith("service/impl/FacturaServiceImpl.java")));
        assertTrue(preview.getFiles().stream().anyMatch(f -> f.getRelativePath().endsWith("controller/FacturaController.java")));
        assertTrue(preview.getFiles().stream().anyMatch(f -> f.getRelativePath().equals("pom.xml")));
        assertTrue(preview.getFiles().stream().anyMatch(f -> f.getRelativePath().equals("src/main/resources/application.yml")));
    }

    @Test
    @DisplayName("Debe generar archivo ZIP válido y descargable con todos los archivos")
    void testGenerateProjectZip() throws IOException {
        ModeloUML modelo = createSampleModel();
        when(modeloUMLRepository.findById(1L)).thenReturn(Optional.of(modelo));

        byte[] zipBytes = generatorService.generateProjectZip(1L, null, "ingeniero@caseplatform.com");

        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);

        // Descomprimir en memoria y validar contenido
        Set<String> entryNames = new HashSet<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryNames.add(entry.getName());
                zis.closeEntry();
            }
        }

        assertTrue(entryNames.stream().anyMatch(n -> n.endsWith("pom.xml")));
        assertTrue(entryNames.stream().anyMatch(n -> n.endsWith("Factura.java")));
        assertTrue(entryNames.stream().anyMatch(n -> n.endsWith("FacturaController.java")));
        assertTrue(entryNames.stream().anyMatch(n -> n.endsWith("application.yml")));
    }

    private ModeloUML createSampleModel() {
        ClaseUML factura = ClaseUML.builder()
                .id(1L)
                .nombre("Factura")
                .atributos(new ArrayList<>(List.of(
                        AtributoUML.builder().id(10L).nombre("numero").tipoDato("String").visibilidad(VisibilidadUML.PRIVATE).build(),
                        AtributoUML.builder().id(11L).nombre("total").tipoDato("Double").visibilidad(VisibilidadUML.PRIVATE).build()
                )))
                .build();

        return ModeloUML.builder()
                .id(1L)
                .nombre("Facturación")
                .clases(List.of(factura))
                .relaciones(new ArrayList<>())
                .build();
    }
}
