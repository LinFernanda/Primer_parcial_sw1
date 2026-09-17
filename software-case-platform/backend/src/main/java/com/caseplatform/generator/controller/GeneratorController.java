package com.caseplatform.generator.controller;

import com.caseplatform.generator.dto.GeneratedProjectPreviewDTO;
import com.caseplatform.generator.dto.GeneratorRequestDTO;
import com.caseplatform.generator.dto.GeneratorResponseDTO;
import com.caseplatform.generator.service.BackendGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el motor de generación automática de Backend Spring Boot
 * desde modelos conceptuales UML.
 */
@Slf4j
@RestController
@RequestMapping("/api/generator")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GeneratorController {

    private final BackendGeneratorService backendGeneratorService;

    /**
     * Genera el backend Spring Boot y retorna el resumen de generación.
     * POST /api/generator/project/{modeloId}
     */
    @PostMapping("/project/{modeloId}")
    public ResponseEntity<GeneratorResponseDTO> generateProject(
            @PathVariable Long modeloId,
            @RequestBody(required = false) GeneratorRequestDTO request
    ) {
        String userEmail = getAuthenticatedUserEmail();
        log.info("Petición POST /api/generator/project/{} recibida por usuario: {}", modeloId, userEmail);

        GeneratorResponseDTO response = backendGeneratorService.generateProject(modeloId, request, userEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * Previsualiza los archivos y el código fuente a generar sin descargar.
     * GET /api/generator/project/{modeloId}/preview
     */
    @GetMapping("/project/{modeloId}/preview")
    public ResponseEntity<GeneratedProjectPreviewDTO> previewProject(
            @PathVariable Long modeloId,
            @RequestParam(required = false) String packageName,
            @RequestParam(required = false) String projectName
    ) {
        log.info("Petición GET /api/generator/project/{}/preview", modeloId);
        GeneratorRequestDTO request = GeneratorRequestDTO.builder()
                .packageName(packageName != null ? packageName : "com.caseplatform.generated")
                .projectName(projectName != null ? projectName : "SpringBootBackend")
                .build();

        GeneratedProjectPreviewDTO preview = backendGeneratorService.previewProject(modeloId, request);
        return ResponseEntity.ok(preview);
    }

    /**
     * Descarga directa del archivo .ZIP con el proyecto Spring Boot listo para ejecutar.
     * GET /api/generator/project/{modeloId}/zip
     */
    @GetMapping(value = "/project/{modeloId}/zip", produces = "application/zip")
    public ResponseEntity<byte[]> downloadProjectZip(
            @PathVariable Long modeloId,
            @RequestParam(required = false) String packageName,
            @RequestParam(required = false) String projectName
    ) {
        String userEmail = getAuthenticatedUserEmail();
        log.info("Petición GET /api/generator/project/{}/zip por usuario: {}", modeloId, userEmail);

        GeneratorRequestDTO request = GeneratorRequestDTO.builder()
                .packageName(packageName != null ? packageName : "com.caseplatform.generated")
                .projectName(projectName != null ? projectName : "SpringBootBackend")
                .build();

        byte[] zipBytes = backendGeneratorService.generateProjectZip(modeloId, request, userEmail);

        String fileName = (projectName != null && !projectName.isBlank() ? projectName : "spring-boot-backend") + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipBytes);
    }

    private String getAuthenticatedUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
            return auth.getName();
        }
        return "ingeniero.uml@caseplatform.com";
    }
}
