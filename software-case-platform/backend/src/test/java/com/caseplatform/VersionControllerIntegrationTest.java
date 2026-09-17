package com.caseplatform;

import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.dto.uml.ProyectoUMLDTO;
import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import com.caseplatform.model.Usuario;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.ProyectoUMLRepository;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.security.JwtService;
import com.caseplatform.versioning.dto.CreateVersionDTO;
import com.caseplatform.versioning.dto.RestoreVersionDTO;
import com.caseplatform.versioning.repository.HistorialCambioRepository;
import com.caseplatform.versioning.repository.VersionModeloRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VersionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProyectoUMLRepository proyectoUMLRepository;

    @Autowired
    private VersionModeloRepository versionModeloRepository;

    @Autowired
    private HistorialCambioRepository historialCambioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String token;
    private Long modeloId;

    @BeforeEach
    void setUp() throws Exception {
        historialCambioRepository.deleteAll();
        versionModeloRepository.deleteAll();
        proyectoUMLRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = Usuario.builder()
                .nombreCompleto("Ingeniero Software")
                .email("ingeniero.versiones@caseplatform.com")
                .password(passwordEncoder.encode("Password123*"))
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        token = "Bearer " + jwtService.generarToken(usuario);

        // Crear proyecto base (que inicializa un modelo UML automáticamente)
        ProyectoUMLDTO proyDto = ProyectoUMLDTO.builder()
                .nombre("Proyecto Versionable")
                .descripcion("Proyecto de prueba para sistema de versiones")
                .build();

        MvcResult resultProy = mockMvc.perform(post("/api/proyectos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proyDto)))
                .andExpect(status().isCreated())
                .andReturn();

        ProyectoUMLDTO proyCreado = objectMapper.readValue(resultProy.getResponse().getContentAsString(), ProyectoUMLDTO.class);
        modeloId = proyCreado.getModelos().get(0).getId();

        // Agregar una clase inicial al modelo
        ClaseUMLDTO claseDto = ClaseUMLDTO.builder()
                .nombre("Cliente")
                .visibilidad(VisibilidadUML.PUBLIC)
                .posicionX(150.0)
                .posicionY(200.0)
                .build();

        mockMvc.perform(post("/api/modelos/" + modeloId + "/clases")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claseDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("API: POST /api/versiones crea snapshot y versión correctamente")
    void testCrearVersionEndpoint() throws Exception {
        CreateVersionDTO versionReq = CreateVersionDTO.builder()
                .modeloId(modeloId)
                .nombreVersion("Versión 1.0 Inicial")
                .numeroVersion("v1.0")
                .descripcion("Primer snapshot con clase Cliente")
                .build();

        mockMvc.perform(post("/api/versiones")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(versionReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreVersion").value("Versión 1.0 Inicial"))
                .andExpect(jsonPath("$.numeroVersion").value("v1.0"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.modeloId").value(modeloId))
                .andExpect(jsonPath("$.usuarioCreadorEmail").value("ingeniero.versiones@caseplatform.com"))
                .andExpect(jsonPath("$.snapshot.clases", hasSize(1)));
    }

    @Test
    @DisplayName("API: GET /api/modelos/{id}/versiones lista las versiones existentes")
    void testListarVersionesEndpoint() throws Exception {
        // Crear versión
        CreateVersionDTO versionReq = CreateVersionDTO.builder()
                .modeloId(modeloId)
                .nombreVersion("Versión 1.0")
                .build();

        mockMvc.perform(post("/api/versiones")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(versionReq)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/modelos/" + modeloId + "/versiones")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreVersion").value("Versión 1.0"));
    }

    @Test
    @DisplayName("API: GET /api/modelos/{id}/historial registra y devuelve cambios atómicos")
    void testObtenerHistorialEndpoint() throws Exception {
        // Crear una segunda clase para generar evento de historial
        ClaseUMLDTO clase2 = ClaseUMLDTO.builder()
                .nombre("Factura")
                .visibilidad(VisibilidadUML.PUBLIC)
                .build();

        mockMvc.perform(post("/api/modelos/" + modeloId + "/clases")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clase2)))
                .andExpect(status().isCreated());

        // Consultar historial
        mockMvc.perform(get("/api/modelos/" + modeloId + "/historial")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("API: POST /api/versiones/{id}/restore restaura el modelo al estado snapshot")
    void testRestaurarVersionEndpoint() throws Exception {
        // 1. Crear Versión 1.0 (solo tiene la clase 'Cliente')
        CreateVersionDTO v1 = CreateVersionDTO.builder()
                .modeloId(modeloId)
                .nombreVersion("Versión 1.0")
                .numeroVersion("v1.0")
                .build();

        MvcResult v1Res = mockMvc.perform(post("/api/versiones")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v1)))
                .andExpect(status().isCreated())
                .andReturn();

        Long versionId = objectMapper.readTree(v1Res.getResponse().getContentAsString()).get("id").asLong();

        // 2. Agregar clase 'Producto' que no estaba en la versión 1.0
        ClaseUMLDTO claseProducto = ClaseUMLDTO.builder()
                .nombre("Producto")
                .visibilidad(VisibilidadUML.PUBLIC)
                .build();

        mockMvc.perform(post("/api/modelos/" + modeloId + "/clases")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claseProducto)))
                .andExpect(status().isCreated());

        // Verificar que el modelo tiene 2 clases
        mockMvc.perform(get("/api/modelos/" + modeloId + "/clases")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 3. Restaurar Versión 1.0
        RestoreVersionDTO restoreReq = RestoreVersionDTO.builder()
                .comentario("Revertir clase Producto")
                .build();

        mockMvc.perform(post("/api/versiones/" + versionId + "/restore")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restoreReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clases", hasSize(1)))
                .andExpect(jsonPath("$.clases[0].nombre").value("Cliente"));

        // 4. Comprobar que en base de datos el modelo tiene solo 1 clase restaurada
        mockMvc.perform(get("/api/modelos/" + modeloId + "/clases")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Cliente"));
    }
}
