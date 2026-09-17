package com.caseplatform.versioning.controller;

import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.versioning.dto.CreateVersionDTO;
import com.caseplatform.versioning.dto.HistorialCambioDTO;
import com.caseplatform.versioning.dto.RestoreVersionDTO;
import com.caseplatform.versioning.dto.VersionDTO;
import com.caseplatform.versioning.service.VersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el módulo de versiones, historial de cambios y trazabilidad UML.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    /**
     * POST /api/versiones - Congelar y crear una nueva versión del modelo UML.
     */
    @PostMapping("/versiones")
    public ResponseEntity<VersionDTO> crearVersion(
            @Valid @RequestBody CreateVersionDTO dto,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : "ingeniero@caseplatform.com";
        log.info("Petición POST /api/versiones por usuario: {}", email);
        VersionDTO creada = versionService.crearVersion(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * GET /api/modelos/{id}/versiones - Obtener todas las versiones de un modelo UML.
     */
    @GetMapping("/modelos/{id}/versiones")
    public ResponseEntity<List<VersionDTO>> listarVersionesPorModelo(@PathVariable Long id) {
        log.info("Petición GET /api/modelos/{}/versiones", id);
        List<VersionDTO> versiones = versionService.listarVersionesPorModelo(id);
        return ResponseEntity.ok(versiones);
    }

    /**
     * GET /api/versiones/{id} - Obtener detalle y snapshot de una versión específica.
     */
    @GetMapping("/versiones/{id}")
    public ResponseEntity<VersionDTO> obtenerVersionPorId(@PathVariable Long id) {
        log.info("Petición GET /api/versiones/{}", id);
        VersionDTO version = versionService.obtenerVersionPorId(id);
        return ResponseEntity.ok(version);
    }

    /**
     * GET /api/modelos/{id}/historial - Obtener el historial completo de cambios de un modelo UML.
     */
    @GetMapping("/modelos/{id}/historial")
    public ResponseEntity<List<HistorialCambioDTO>> obtenerHistorialPorModelo(@PathVariable Long id) {
        log.info("Petición GET /api/modelos/{}/historial", id);
        List<HistorialCambioDTO> historial = versionService.obtenerHistorialPorModelo(id);
        return ResponseEntity.ok(historial);
    }

    /**
     * POST /api/versiones/{id}/restore - Restaurar el modelo UML al snapshot de una versión anterior.
     */
    @PostMapping("/versiones/{id}/restore")
    public ResponseEntity<ModeloUMLDTO> restaurarVersion(
            @PathVariable Long id,
            @RequestBody(required = false) RestoreVersionDTO dto,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : "ingeniero@caseplatform.com";
        log.info("Petición POST /api/versiones/{}/restore por usuario: {}", id, email);
        ModeloUMLDTO restaurado = versionService.restaurarVersion(id, dto, email);
        return ResponseEntity.ok(restaurado);
    }
}
