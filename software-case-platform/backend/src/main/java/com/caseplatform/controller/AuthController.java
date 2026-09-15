package com.caseplatform.controller;

import com.caseplatform.dto.AuthResponseDTO;
import com.caseplatform.dto.LoginRequestDTO;
import com.caseplatform.dto.UsuarioRequestDTO;
import com.caseplatform.dto.UsuarioResponseDTO;
import com.caseplatform.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones públicas de autenticación y registro.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    /**
     * Endpoint para registro de nuevos usuarios en la plataforma.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO request) {
        log.info("Petición de registro recibida para: {}", request.getEmail());
        UsuarioResponseDTO response = usuarioService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para autenticación e inicio de sesión de usuarios.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Petición de login recibida para: {}", request.getEmail());
        AuthResponseDTO response = usuarioService.login(request);
        return ResponseEntity.ok(response);
    }
}
