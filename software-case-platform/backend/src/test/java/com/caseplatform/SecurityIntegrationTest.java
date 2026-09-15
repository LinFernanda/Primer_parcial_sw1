package com.caseplatform;

import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import com.caseplatform.model.Usuario;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Usuario adminUser;
    private Usuario ingenieroUser;
    private String adminToken;
    private String ingenieroToken;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        adminUser = usuarioRepository.save(Usuario.builder()
                .nombreCompleto("Admin General")
                .email("admin.test@caseplatform.com")
                .password(passwordEncoder.encode("AdminPass123*"))
                .rol(Rol.ADMIN)
                .estado(EstadoUsuario.ACTIVO)
                .build());

        ingenieroUser = usuarioRepository.save(Usuario.builder()
                .nombreCompleto("Ingeniero Dev")
                .email("dev.test@caseplatform.com")
                .password(passwordEncoder.encode("DevPass123*"))
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .build());

        adminToken = jwtService.generarToken(adminUser);
        ingenieroToken = jwtService.generarToken(ingenieroUser);
    }

    @Test
    @DisplayName("Seguridad: Rechazo de acceso a endpoint protegido sin token (HTTP 401)")
    void testAccesoSinToken_Devuelve401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Seguridad: Acceso exitoso a /api/users/me con token JWT válido (HTTP 200)")
    void testAccesoConTokenValido_Devuelve200() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + ingenieroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("dev.test@caseplatform.com"))
                .andExpect(jsonPath("$.rol").value("INGENIERO"));
    }

    @Test
    @DisplayName("Seguridad: Acceso exitoso a /api/projects con token JWT válido (HTTP 200)")
    void testAccesoProyectosConTokenValido_Devuelve200() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + ingenieroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("dev.test@caseplatform.com"));
    }

    @Test
    @DisplayName("Seguridad: Rechazo por rol insuficiente en endpoint de administrador (HTTP 403)")
    void testAccesoAdminPorIngeniero_Devuelve403() throws Exception {
        // INGENIERO intentando listar todos los usuarios (requiere ADMIN)
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + ingenieroToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Seguridad: Acceso exitoso a endpoint de administrador con rol ADMIN (HTTP 200)")
    void testAccesoAdminPorAdmin_Devuelve200() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
