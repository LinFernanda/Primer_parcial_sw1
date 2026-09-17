package com.caseplatform.generator.service.impl;

import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.generator.analyzer.UMLAnalyzerService;
import com.caseplatform.generator.builder.*;
import com.caseplatform.generator.dto.GeneratedFileDTO;
import com.caseplatform.generator.dto.GeneratedProjectPreviewDTO;
import com.caseplatform.generator.dto.GeneratorRequestDTO;
import com.caseplatform.generator.dto.GeneratorResponseDTO;
import com.caseplatform.generator.model.GeneratedEntityModel;
import com.caseplatform.generator.model.GeneratedFile;
import com.caseplatform.generator.model.GeneratedProjectModel;
import com.caseplatform.generator.service.BackendGeneratorService;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.repository.ModeloUMLRepository;
import com.caseplatform.versioning.dto.CreateVersionDTO;
import com.caseplatform.versioning.service.VersionService;
import com.caseplatform.websocket.service.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackendGeneratorServiceImpl implements BackendGeneratorService {

    private final ModeloUMLRepository modeloUMLRepository;
    private final UMLAnalyzerService umlAnalyzerService;
    private final EntityGeneratorService entityGeneratorService;
    private final RepositoryGeneratorService repositoryGeneratorService;
    private final DTOGeneratorService dtoGeneratorService;
    private final ServiceGeneratorService serviceGeneratorService;
    private final ControllerGeneratorService controllerGeneratorService;
    private final ProjectStructureBuilder projectStructureBuilder;

    @Autowired(required = false)
    private VersionService versionService;

    @Autowired(required = false)
    private EventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public GeneratedProjectPreviewDTO previewProject(Long modeloId, GeneratorRequestDTO request) {
        log.info("Generando previsualización de proyecto backend para modelo ID: {}", modeloId);
        ModeloUML modelo = modeloUMLRepository.findById(modeloId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId));

        GeneratedProjectModel projectModel = buildProjectModel(modelo, request);

        List<GeneratedFileDTO> fileDTOs = projectModel.getFiles().stream()
                .map(f -> GeneratedFileDTO.builder()
                        .relativePath(f.getRelativePath())
                        .content(f.getContent())
                        .category(f.getCategory().name())
                        .sizeBytes(f.getSizeBytes())
                        .build())
                .toList();

        return GeneratedProjectPreviewDTO.builder()
                .modeloId(modeloId)
                .projectName(projectModel.getProjectName())
                .packageName(projectModel.getBasePackage())
                .totalFiles(fileDTOs.size())
                .totalEntities(projectModel.getEntities().size())
                .files(fileDTOs)
                .build();
    }

    @Override
    @Transactional
    public byte[] generateProjectZip(Long modeloId, GeneratorRequestDTO request, String usuarioEmail) {
        log.info("Generando paquete ZIP de proyecto backend para modelo ID: {} por usuario: {}", modeloId, usuarioEmail);
        ModeloUML modelo = modeloUMLRepository.findById(modeloId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId));

        GeneratedProjectModel projectModel = buildProjectModel(modelo, request);

        if (projectModel.getEntities().isEmpty()) {
            throw new ValidationException("El modelo UML no contiene clases para generar el backend.");
        }

        // Empaquetar archivos en ZIP
        byte[] zipBytes = packageToZip(projectModel);

        // 15. Integración con Historial y Versiones (Fase 6)
        if (versionService != null) {
            try {
                CreateVersionDTO versionDTO = CreateVersionDTO.builder()
                        .modeloId(modeloId)
                        .nombreVersion("Generación Backend Spring Boot (" + projectModel.getProjectName() + ")")
                        .descripcion("Proyecto Spring Boot generado automáticamente. Entidades: " +
                                projectModel.getEntities().size() + ", Total archivos: " + projectModel.getFiles().size())
                        .build();
                versionService.crearVersion(versionDTO, usuarioEmail);
            } catch (Exception e) {
                log.warn("No se pudo registrar la versión de generación de código: {}", e.getMessage());
            }
        }

        // Difusión colaborativa WebSocket
        if (eventPublisher != null) {
            try {
                com.caseplatform.websocket.model.UMLEvent event = com.caseplatform.websocket.model.UMLEvent.builder()
                        .modeloUMLId(modeloId)
                        .tipoOperacion(com.caseplatform.websocket.model.TipoOperacionUML.UPDATE)
                        .elementoTipo(com.caseplatform.websocket.model.TipoElementoUML.MODELO)
                        .elementoId(modeloId.toString())
                        .usuario(usuarioEmail != null ? usuarioEmail : "generator@caseplatform.com")
                        .fecha(LocalDateTime.now())
                        .datosCambio(java.util.Map.of(
                                "mensaje", "Proyecto backend Spring Boot generado: " + projectModel.getProjectName(),
                                "totalEntidades", projectModel.getEntities().size()
                        ))
                        .build();
                eventPublisher.publishEvent(modeloId, event);
            } catch (Exception e) {
                log.warn("No se pudo difundir el evento de generación de backend: {}", e.getMessage());
            }
        }

        log.info("Paquete ZIP generado exitosamente. Tamaño: {} bytes, {} archivos.", zipBytes.length, projectModel.getFiles().size());
        return zipBytes;
    }

    @Override
    @Transactional
    public GeneratorResponseDTO generateProject(Long modeloId, GeneratorRequestDTO request, String usuarioEmail) {
        GeneratedProjectPreviewDTO preview = previewProject(modeloId, request);
        // Garantizar validación de generación
        generateProjectZip(modeloId, request, usuarioEmail);

        return GeneratorResponseDTO.builder()
                .modeloId(modeloId)
                .projectName(preview.getProjectName())
                .packageName(preview.getPackageName())
                .totalFiles(preview.getTotalFiles())
                .totalEntities(preview.getTotalEntities())
                .zipDownloadUrl("/api/generator/project/" + modeloId + "/zip")
                .fechaGeneracion(LocalDateTime.now())
                .mensaje("Proyecto backend Spring Boot generado exitosamente con " + preview.getTotalEntities() + " entidades.")
                .build();
    }

    private GeneratedProjectModel buildProjectModel(ModeloUML modelo, GeneratorRequestDTO request) {
        GeneratedProjectModel projectModel = umlAnalyzerService.analyze(modelo, request);
        List<GeneratedFile> files = new ArrayList<>();

        String basePackage = projectModel.getBasePackage();

        // 1. Generar código por cada entidad detectada
        for (GeneratedEntityModel entity : projectModel.getEntities()) {
            // Entidad JPA
            files.add(entityGeneratorService.generateEntity(basePackage, entity));

            // Repositorio Spring Data JPA
            files.add(repositoryGeneratorService.generateRepository(basePackage, entity));

            // DTOs (Request y Response)
            files.addAll(dtoGeneratorService.generateDTOs(basePackage, entity));

            // Servicio (Interface e Implementación)
            files.addAll(serviceGeneratorService.generateService(basePackage, entity));

            // Controlador REST
            files.add(controllerGeneratorService.generateController(basePackage, entity));
        }

        // 2. Generar estructura del proyecto (pom.xml, application.yml, Docker, README, etc.)
        files.addAll(projectStructureBuilder.buildProjectStructure(projectModel));

        projectModel.setFiles(files);
        return projectModel;
    }

    private byte[] packageToZip(GeneratedProjectModel projectModel) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {

            String rootDir = projectModel.getArtifactId() + "/";

            for (GeneratedFile file : projectModel.getFiles()) {
                String entryPath = rootDir + file.getRelativePath();
                ZipEntry entry = new ZipEntry(entryPath);
                zos.putNextEntry(entry);
                zos.write(file.getContent().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error al comprimir el proyecto en formato ZIP: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el archivo comprimido del proyecto: " + e.getMessage(), e);
        }
    }
}
