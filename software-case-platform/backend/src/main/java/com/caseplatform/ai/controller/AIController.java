package com.caseplatform.ai.controller;

import com.caseplatform.ai.command.AICommandRequest;
import com.caseplatform.ai.command.AICommandResponse;
import com.caseplatform.ai.command.VoiceCommandRequest;
import com.caseplatform.ai.model.AICommandHistory;
import com.caseplatform.ai.service.AIAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Controlador REST para el Agente Inteligente de edición UML por texto y voz.
 */
@Slf4j
@RestController
@RequestMapping("/api/modelos/{id}/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIAgentService aiAgentService;

    /**
     * POST /api/modelos/{id}/ai/command - Interpretar y ejecutar un comando en lenguaje natural.
     */
    @PostMapping("/command")
    public ResponseEntity<AICommandResponse> procesarComando(
            @PathVariable Long id,
            @Valid @RequestBody AICommandRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : "ingeniero@caseplatform.com";
        log.info("Petición POST /api/modelos/{}/ai/command de usuario: {}", id, email);
        AICommandResponse response = aiAgentService.procesarComando(id, request, email);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/modelos/{id}/ai/voice - Procesar comando por voz transcrito a texto.
     */
    @PostMapping("/voice")
    public ResponseEntity<AICommandResponse> procesarVoz(
            @PathVariable Long id,
            @RequestBody VoiceCommandRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : "ingeniero@caseplatform.com";
        log.info("Petición POST /api/modelos/{}/ai/voice de usuario: {}", id, email);
        AICommandResponse response = aiAgentService.procesarVoz(id, request, email);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/modelos/{id}/ai/history - Obtener historial de auditoría de comandos IA.
     */
    @GetMapping("/history")
    public ResponseEntity<List<AICommandHistory>> obtenerHistorialComandos(@PathVariable Long id) {
        log.info("Petición GET /api/modelos/{}/ai/history", id);
        List<AICommandHistory> history = aiAgentService.obtenerHistorialComandos(id);
        return ResponseEntity.ok(history);
    }
}
