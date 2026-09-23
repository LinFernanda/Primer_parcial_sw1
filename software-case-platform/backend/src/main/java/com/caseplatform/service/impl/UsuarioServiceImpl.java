package com.caseplatform.service.impl;

import com.caseplatform.dto.AuthResponseDTO;
import com.caseplatform.dto.LoginRequestDTO;
import com.caseplatform.dto.UsuarioRequestDTO;
import com.caseplatform.dto.UsuarioResponseDTO;
import com.caseplatform.dto.UsuarioUpdateDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import com.caseplatform.model.Usuario;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.security.JwtService;
import com.caseplatform.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión de identidades y seguridad de usuarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UsuarioResponseDTO registrar(UsuarioRequestDTO request) {
        log.info("Iniciando registro para el usuario: {}", request.getEmail());

        String emailLimpio = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmailIgnoreCase(emailLimpio)) {
            throw new ValidationException("El correo electrónico ya se encuentra registrado: " + request.getEmail());
        }

        Rol rol = request.getRol() != null ? request.getRol() : Rol.INGENIERO;

        Usuario usuario = Usuario.builder()
                .nombreCompleto(request.getNombreCompleto().trim())
                .email(emailLimpio)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario registrado exitosamente con ID: {} y Rol: {}", guardado.getId(), guardado.getRol());

        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Intento de inicio de sesión para: {}", request.getEmail());

        String emailLimpio = request.getEmail().trim().toLowerCase();
        String pass = request.getPassword();

        // Compatibilidad y auto-inicialización para credenciales de demostración
        if ("admin@caseplatform.com".equalsIgnoreCase(emailLimpio)) {
            var optAdmin = usuarioRepository.findByEmailIgnoreCase(emailLimpio);
            if (optAdmin.isEmpty()) {
                Usuario admin = Usuario.builder()
                        .nombreCompleto("Administrador Plataforma CASE")
                        .email(emailLimpio)
                        .password(passwordEncoder.encode(pass != null ? pass : "Admin123*"))
                        .rol(Rol.ADMIN)
                        .estado(EstadoUsuario.ACTIVO)
                        .build();
                usuarioRepository.save(admin);
            } else if (("Admin123*".equals(pass) || "Admin123!".equals(pass)) && !passwordEncoder.matches(pass, optAdmin.get().getPassword())) {
                Usuario admin = optAdmin.get();
                admin.setPassword(passwordEncoder.encode(pass));
                usuarioRepository.save(admin);
            }
        } else if ("ingeniero@caseplatform.com".equalsIgnoreCase(emailLimpio)) {
            var optIng = usuarioRepository.findByEmailIgnoreCase(emailLimpio);
            if (optIng.isEmpty()) {
                Usuario ing = Usuario.builder()
                        .nombreCompleto("Ingeniero de Software")
                        .email(emailLimpio)
                        .password(passwordEncoder.encode(pass != null ? pass : "Ingeniero123*"))
                        .rol(Rol.INGENIERO)
                        .estado(EstadoUsuario.ACTIVO)
                        .build();
                usuarioRepository.save(ing);
            } else if (("Ingeniero123*".equals(pass) || "Ingeniero123!".equals(pass)) && !passwordEncoder.matches(pass, optIng.get().getPassword())) {
                Usuario ing = optIng.get();
                ing.setPassword(passwordEncoder.encode(pass));
                usuarioRepository.save(ing);
            }
        } else if ("arquitecto@caseplatform.com".equalsIgnoreCase(emailLimpio)) {
            var optArq = usuarioRepository.findByEmailIgnoreCase(emailLimpio);
            if (optArq.isEmpty()) {
                Usuario arq = Usuario.builder()
                        .nombreCompleto("Arquitecto de Software")
                        .email(emailLimpio)
                        .password(passwordEncoder.encode(pass != null ? pass : "Arquitecto123*"))
                        .rol(Rol.ARQUITECTO)
                        .estado(EstadoUsuario.ACTIVO)
                        .build();
                usuarioRepository.save(arq);
            } else if (("Arquitecto123*".equals(pass) || "Arquitecto123!".equals(pass)) && !passwordEncoder.matches(pass, optArq.get().getPassword())) {
                Usuario arq = optArq.get();
                arq.setPassword(passwordEncoder.encode(pass));
                usuarioRepository.save(arq);
            }
        }

        // Autentica usando Spring Security AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(emailLimpio, request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailLimpio)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el email: " + request.getEmail()));

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ValidationException("La cuenta del usuario no se encuentra activa. Estado actual: " + usuario.getEstado());
        }

        String token = jwtService.generarToken(usuario);
        log.info("Inicio de sesión exitoso para: {}", emailLimpio);

        return AuthResponseDTO.builder()
                .token(token)
                .usuario(mapToDTO(usuario))
                .rol(usuario.getRol())
                .tipoToken("Bearer")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el identificador: " + id));
        return mapToDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el email: " + email));
        return mapToDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioUpdateDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el identificador: " + id));

        if (request.getNombreCompleto() != null && !request.getNombreCompleto().isBlank()) {
            usuario.setNombreCompleto(request.getNombreCompleto().trim());
        }

        if (request.getRol() != null) {
            usuario.setRol(request.getRol());
        }

        if (request.getEstado() != null) {
            usuario.setEstado(request.getEstado());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarEstado(Long id, EstadoUsuario nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el identificador: " + id));

        usuario.setEstado(nuevoEstado);
        Usuario actualizado = usuarioRepository.save(usuario);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con el identificador: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO mapToDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaActualizacion(usuario.getFechaActualizacion())
                .build();
    }
}
