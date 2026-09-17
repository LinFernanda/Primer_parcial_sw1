package com.caseplatform.integration.enterprisearchitect.controller;

import com.caseplatform.integration.enterprisearchitect.dto.XMIExportResponseDTO;
import com.caseplatform.integration.enterprisearchitect.dto.XMIImportResponseDTO;
import com.caseplatform.integration.enterprisearchitect.dto.XMIValidationResponseDTO;
import com.caseplatform.integration.enterprisearchitect.service.EnterpriseArchitectExportService;
import com.caseplatform.integration.enterprisearchitect.service.EnterpriseArchitectImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controlador REST para la interoperabilidad con Enterprise Architect mediante XMI.
 */
@Slf4j
@RestController
@RequestMapping("/api/integration/ea")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class EnterpriseArchitectController {

    private final EnterpriseArchitectImportService importService;
    private final EnterpriseArchitectExportService exportService;

    /**
     * Valida la estructura XMI de un archivo XML sin persistir cambios.
     * POST /api/integration/ea/validate
     */
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<XMIValidationResponseDTO> validateXMI(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        log.info("Petición POST /api/integration/ea/validate para archivo: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo XMI subido no puede estar vacío");
        }
        XMIValidationResponseDTO response = importService.validateXMI(file.getInputStream());
        return ResponseEntity.ok(response);
    }

    /**
     * Importa un archivo XMI sobre un modelo UML existente.
     * POST /api/integration/ea/models/{modeloId}/import
     */
    @PostMapping(value = "/models/{modeloId}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<XMIImportResponseDTO> importToExistingModel(
            @PathVariable Long modeloId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "limpiarExistente", defaultValue = "false") boolean limpiarExistente
    ) throws IOException {
        String userEmail = getAuthenticatedUserEmail();
        log.info("Petición POST /api/integration/ea/models/{}/import por usuario: {}, archivo: {}",
                modeloId, userEmail, file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo XMI subido no puede estar vacío");
        }

        XMIImportResponseDTO response = importService.importToExistingModel(
                modeloId,
                file.getInputStream(),
                file.getOriginalFilename(),
                limpiarExistente,
                userEmail
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Importa un archivo XMI creando un nuevo modelo UML dentro de un proyecto.
     * POST /api/integration/ea/projects/{proyectoId}/import
     */
    @PostMapping(value = "/projects/{proyectoId}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<XMIImportResponseDTO> importAsNewModel(
            @PathVariable Long proyectoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "nombreModelo", required = false) String nombreModelo
    ) throws IOException {
        String userEmail = getAuthenticatedUserEmail();
        log.info("Petición POST /api/integration/ea/projects/{}/import por usuario: {}, archivo: {}",
                proyectoId, userEmail, file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo XMI subido no puede estar vacío");
        }

        XMIImportResponseDTO response = importService.importAsNewModel(
                proyectoId,
                file.getInputStream(),
                file.getOriginalFilename(),
                nombreModelo,
                userEmail
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Exporta el modelo UML a formato XMI y lo descarga como archivo XML/XMI.
     * GET /api/integration/ea/models/{modeloId}/export
     */
    @GetMapping(value = "/models/{modeloId}/export", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> exportModelToXMI(
            @PathVariable Long modeloId
    ) {
        String userEmail = getAuthenticatedUserEmail();
        log.info("Petición GET /api/integration/ea/models/{}/export por usuario: {}", modeloId, userEmail);

        XMIExportResponseDTO metadata = exportService.exportModel(modeloId, userEmail);
        byte[] bytes = exportService.exportModelAsBytes(modeloId, userEmail);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getNombreArchivo() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .body(bytes);
    }

    /**
     * Previsualiza los metadatos y contenido XML de exportación XMI.
     * GET /api/integration/ea/models/{modeloId}/export/preview
     */
    @GetMapping("/models/{modeloId}/export/preview")
    public ResponseEntity<XMIExportResponseDTO> previewExportXMI(
            @PathVariable Long modeloId
    ) {
        String userEmail = getAuthenticatedUserEmail();
        log.info("Petición GET /api/integration/ea/models/{}/export/preview por usuario: {}", modeloId, userEmail);

        XMIExportResponseDTO response = exportService.exportModel(modeloId, userEmail);
        return ResponseEntity.ok(response);
    }

    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            return authentication.getName();
        }
        return "usuario@caseplatform.com";
    }
}
