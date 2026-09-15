package com.caseplatform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoints protegidos para validar el control de acceso a recursos de proyectos y modelos UML (Fase 2).
 */
@RestController
public class ResourcePlaceholderController {

    @GetMapping("/api/projects")
    public ResponseEntity<Map<String, Object>> listarProyectos(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "Recurso protegido de proyectos accesible para usuario autenticado",
                "usuario", authentication.getName(),
                "autoridades", authentication.getAuthorities().toString()
        ));
    }

    @GetMapping("/api/models")
    public ResponseEntity<Map<String, Object>> listarModelos(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "Recurso protegido de modelos UML accesible para usuario autenticado",
                "usuario", authentication.getName(),
                "autoridades", authentication.getAuthorities().toString()
        ));
    }
}
