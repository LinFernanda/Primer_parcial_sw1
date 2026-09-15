package com.caseplatform.service;

import com.caseplatform.dto.AuthResponseDTO;
import com.caseplatform.dto.LoginRequestDTO;
import com.caseplatform.dto.UsuarioRequestDTO;
import com.caseplatform.dto.UsuarioResponseDTO;
import com.caseplatform.dto.UsuarioUpdateDTO;
import com.caseplatform.model.EstadoUsuario;

import java.util.List;

/**
 * Contrato de servicio para la gestión de usuarios, registro y autenticación.
 */
public interface UsuarioService {

    /**
     * Registra un nuevo usuario con contraseña cifrada y validación de unicidad de email.
     */
    UsuarioResponseDTO registrar(UsuarioRequestDTO request);

    /**
     * Autentica un usuario con email y contraseña, retornando un token JWT y datos de sesión.
     */
    AuthResponseDTO login(LoginRequestDTO request);

    /**
     * Obtiene el perfil de un usuario por su identificador único.
     */
    UsuarioResponseDTO obtenerPorId(Long id);

    /**
     * Obtiene el perfil de un usuario por su dirección de email.
     */
    UsuarioResponseDTO obtenerPorEmail(String email);

    /**
     * Lista todos los usuarios registrados en la plataforma (uso administrativo).
     */
    List<UsuarioResponseDTO> listarTodos();

    /**
     * Actualiza los datos de perfil de un usuario existente.
     */
    UsuarioResponseDTO actualizar(Long id, UsuarioUpdateDTO request);

    /**
     * Modifica el estado de un usuario (ACTIVO, INACTIVO, BLOQUEADO).
     */
    UsuarioResponseDTO cambiarEstado(Long id, EstadoUsuario nuevoEstado);

    /**
     * Elimina un usuario por su ID.
     */
    void eliminar(Long id);
}
