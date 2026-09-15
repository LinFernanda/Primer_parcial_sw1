package com.caseplatform;

import com.caseplatform.dto.AuthResponseDTO;
import com.caseplatform.dto.LoginRequestDTO;
import com.caseplatform.dto.UsuarioRequestDTO;
import com.caseplatform.dto.UsuarioResponseDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import com.caseplatform.model.Usuario;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.security.JwtService;
import com.caseplatform.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioPrueba;
    private UsuarioRequestDTO requestDTO;
    private LoginRequestDTO loginDTO;

    @BeforeEach
    void setUp() {
        usuarioPrueba = Usuario.builder()
                .id(1L)
                .nombreCompleto("Carlos Ingeniero")
                .email("carlos@caseplatform.com")
                .password("encoded_secret_hash")
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        requestDTO = UsuarioRequestDTO.builder()
                .nombreCompleto("Carlos Ingeniero")
                .email("carlos@caseplatform.com")
                .password("ClaveSegura123*")
                .rol(Rol.INGENIERO)
                .build();

        loginDTO = LoginRequestDTO.builder()
                .email("carlos@caseplatform.com")
                .password("ClaveSegura123*")
                .build();
    }

    @Test
    @DisplayName("Registro exitoso: crea usuario con contraseña hasheada y retorna DTO")
    void testRegistrarUsuario_Exitoso() {
        when(usuarioRepository.existsByEmailIgnoreCase("carlos@caseplatform.com")).thenReturn(false);
        when(passwordEncoder.encode("ClaveSegura123*")).thenReturn("encoded_secret_hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioPrueba);

        UsuarioResponseDTO resultado = usuarioService.registrar(requestDTO);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("carlos@caseplatform.com", resultado.getEmail());
        assertEquals(Rol.INGENIERO, resultado.getRol());
        assertEquals(EstadoUsuario.ACTIVO, resultado.getEstado());

        verify(usuarioRepository).save(any(Usuario.class));
        verify(passwordEncoder).encode("ClaveSegura123*");
    }

    @Test
    @DisplayName("Registro fallido: rechaza email duplicado y lanza ValidationException")
    void testRegistrarUsuario_EmailDuplicado_LanzaExcepcion() {
        when(usuarioRepository.existsByEmailIgnoreCase("carlos@caseplatform.com")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> usuarioService.registrar(requestDTO));
        assertNotNull(ex.getMessage());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Login exitoso: valida credenciales, genera JWT y retorna AuthResponseDTO")
    void testLogin_Exitoso() {
        when(usuarioRepository.findByEmailIgnoreCase("carlos@caseplatform.com")).thenReturn(Optional.of(usuarioPrueba));
        when(jwtService.generarToken(usuarioPrueba)).thenReturn("mocked.jwt.token.case");

        AuthResponseDTO response = usuarioService.login(loginDTO);

        assertNotNull(response);
        assertEquals("mocked.jwt.token.case", response.getToken());
        assertEquals(Rol.INGENIERO, response.getRol());
        assertEquals("Bearer", response.getTipoToken());
        assertEquals("carlos@caseplatform.com", response.getUsuario().getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generarToken(usuarioPrueba);
    }

    @Test
    @DisplayName("Login fallido: credenciales incorrectas lanza BadCredentialsException")
    void testLogin_CredencialesIncorrectas_LanzaExcepcion() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> usuarioService.login(loginDTO));
        verify(jwtService, never()).generarToken(any(Usuario.class));
    }

    @Test
    @DisplayName("Login fallido: cuenta inactiva o bloqueada lanza ValidationException")
    void testLogin_CuentaInactiva_LanzaExcepcion() {
        usuarioPrueba.setEstado(EstadoUsuario.INACTIVO);
        when(usuarioRepository.findByEmailIgnoreCase("carlos@caseplatform.com")).thenReturn(Optional.of(usuarioPrueba));

        assertThrows(ValidationException.class, () -> usuarioService.login(loginDTO));
        verify(jwtService, never()).generarToken(any(Usuario.class));
    }

    @Test
    @DisplayName("Obtener por ID: lanza ResourceNotFoundException cuando no existe")
    void testObtenerPorId_NoExiste_LanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.obtenerPorId(99L));
    }
}
