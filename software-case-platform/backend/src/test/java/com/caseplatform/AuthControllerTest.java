package com.caseplatform;

import com.caseplatform.dto.AuthResponseDTO;
import com.caseplatform.dto.LoginRequestDTO;
import com.caseplatform.dto.UsuarioRequestDTO;
import com.caseplatform.dto.UsuarioResponseDTO;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import com.caseplatform.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("POST /api/auth/register - Registro exitoso devuelve HTTP 201")
    void testRegister_Exitoso() throws Exception {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nombreCompleto("Ana Perez")
                .email("ana@caseplatform.com")
                .password("Password123*")
                .rol(Rol.INGENIERO)
                .build();

        UsuarioResponseDTO response = UsuarioResponseDTO.builder()
                .id(2L)
                .nombreCompleto("Ana Perez")
                .email("ana@caseplatform.com")
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(usuarioService.registrar(any(UsuarioRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("ana@caseplatform.com"))
                .andExpect(jsonPath("$.rol").value("INGENIERO"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Email duplicado devuelve HTTP 400 Bad Request")
    void testRegister_EmailDuplicado_Devuelve400() throws Exception {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nombreCompleto("Ana Perez")
                .email("ana@caseplatform.com")
                .password("Password123*")
                .rol(Rol.INGENIERO)
                .build();

        when(usuarioService.registrar(any(UsuarioRequestDTO.class)))
                .thenThrow(new ValidationException("El correo electrónico ya se encuentra registrado: ana@caseplatform.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El correo electrónico ya se encuentra registrado: ana@caseplatform.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Credenciales válidas devuelve HTTP 200 con Token JWT")
    void testLogin_Exitoso() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("ana@caseplatform.com")
                .password("Password123*")
                .build();

        UsuarioResponseDTO userDto = UsuarioResponseDTO.builder()
                .id(2L)
                .nombreCompleto("Ana Perez")
                .email("ana@caseplatform.com")
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt.token.valid")
                .usuario(userDto)
                .rol(Rol.INGENIERO)
                .tipoToken("Bearer")
                .build();

        when(usuarioService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.valid"))
                .andExpect(jsonPath("$.rol").value("INGENIERO"))
                .andExpect(jsonPath("$.usuario.email").value("ana@caseplatform.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Credenciales incorrectas devuelve HTTP 401 Unauthorized")
    void testLogin_CredencialesIncorrectas_Devuelve401() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("ana@caseplatform.com")
                .password("WrongPassword")
                .build();

        when(usuarioService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales inválidas. Verifica tu email y contraseña."));
    }
}
