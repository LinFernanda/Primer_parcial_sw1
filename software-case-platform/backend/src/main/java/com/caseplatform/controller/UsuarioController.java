package com.caseplatform.controller;

import com.caseplatform.dto.UsuarioResponseDTO;
import com.caseplatform.dto.UsuarioUpdateDTO;
import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST protegido para la administración y consulta de usuarios.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene los datos del perfil del usuario actualmente autenticado mediante su token JWT.
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerPerfilActual(Authentication authentication) {
        String email = authentication.getName();
        UsuarioResponseDTO usuario = usuarioService.obtenerPorEmail(email);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Lista todos los usuarios registrados. Exclusivo para administradores.
     * GET /api/users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene el detalle de un usuario por su identificador único.
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Actualiza los datos de un usuario. Exclusivo para administradores.
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO request
    ) {
        UsuarioResponseDTO usuario = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Modifica el estado de un usuario (ACTIVO, INACTIVO, BLOQUEADO).
     * PATCH /api/users/{id}/estado?estado=INACTIVO
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoUsuario estado
    ) {
        UsuarioResponseDTO usuario = usuarioService.cambiarEstado(id, estado);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Elimina un usuario del sistema.
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
