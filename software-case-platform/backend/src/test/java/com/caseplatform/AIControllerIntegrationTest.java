package com.caseplatform;

import com.caseplatform.ai.command.AICommandRequest;
import com.caseplatform.ai.command.ParsedAIAction;
import com.caseplatform.ai.command.VoiceCommandRequest;
import com.caseplatform.ai.model.TipoOperacionAI;
import com.caseplatform.ai.repository.AICommandHistoryRepository;
import com.caseplatform.dto.uml.ProyectoUMLDTO;
import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import com.caseplatform.model.Usuario;
import com.caseplatform.repository.ProyectoUMLRepository;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.security.JwtService;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AIControllerIntegrationTest {

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
    private AICommandHistoryRepository aiCommandHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String token;
    private Long modeloId;

    @BeforeEach
    void setUp() throws Exception {
        aiCommandHistoryRepository.deleteAll();
        historialCambioRepository.deleteAll();
        versionModeloRepository.deleteAll();
        proyectoUMLRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = Usuario.builder()
                .nombreCompleto("Ingeniero AI")
                .email("ingeniero.ai@caseplatform.com")
                .password(passwordEncoder.encode("Password123*"))
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        token = "Bearer " + jwtService.generarToken(usuario);

        // Crear proyecto base que inicializa automáticamente un Modelo UML
        ProyectoUMLDTO proyDto = ProyectoUMLDTO.builder()
                .nombre("Proyecto AI Modeling")
                .descripcion("Proyecto para pruebas de IA")
                .build();

        MvcResult resultProy = mockMvc.perform(post("/api/proyectos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proyDto)))
                .andExpect(status().isCreated())
                .andReturn();

        ProyectoUMLDTO proyCreado = objectMapper.readValue(resultProy.getResponse().getContentAsString(), ProyectoUMLDTO.class);
        modeloId = proyCreado.getModelos().get(0).getId();
    }

    @Test
    @DisplayName("API: POST /api/modelos/{id}/ai/command ejecuta comando en lenguaje natural y actualiza el modelo")
    void testComandoCrearClase() throws Exception {
        AICommandRequest request = AICommandRequest.builder()
                .prompt("crear clase Factura con atributos monto Double y fecha LocalDate")
                .build();

        mockMvc.perform(post("/api/modelos/" + modeloId + "/ai/command")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.requiereConfirmacion").value(false))
                .andExpect(jsonPath("$.accion.tipoOperacion").value("CREATE_CLASS"))
                .andExpect(jsonPath("$.accion.nombreClase").value("Factura"))
                .andExpect(jsonPath("$.modeloActualizado.clases[*].nombre", hasItem("Factura")));
    }

    @Test
    @DisplayName("API: POST /api/modelos/{id}/ai/command detecta ambigüedad y requiere confirmación previa")
    void testComandoAmbiguedad() throws Exception {
        AICommandRequest request = AICommandRequest.builder()
                .prompt("crear Producto")
                .build();

        mockMvc.perform(post("/api/modelos/" + modeloId + "/ai/command")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiereConfirmacion").value(true))
                .andExpect(jsonPath("$.preguntaConfirmacion").exists())
                .andExpect(jsonPath("$.accion.tipoOperacion").value("CONFIRMATION_REQUIRED"));
    }

    @Test
    @DisplayName("API: POST /api/modelos/{id}/ai/command ejecuta tras confirmación explícita del usuario")
    void testComandoConfirmado() throws Exception {
        ParsedAIAction actionConfirmada = ParsedAIAction.builder()
                .tipoOperacion(TipoOperacionAI.CREATE_CLASS)
                .nombreClase("Producto")
                .visibilidad("PUBLIC")
                .build();

        AICommandRequest request = AICommandRequest.builder()
                .prompt("crear Producto")
                .confirmado(true)
                .accionConfirmada(actionConfirmada)
                .build();

        mockMvc.perform(post("/api/modelos/" + modeloId + "/ai/command")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.requiereConfirmacion").value(false))
                .andExpect(jsonPath("$.modeloActualizado.clases[*].nombre", hasItem("Producto")));
    }

    @Test
    @DisplayName("API: POST /api/modelos/{id}/ai/voice procesa comandos dictados por voz")
    void testComandoPorVoz() throws Exception {
        VoiceCommandRequest request = VoiceCommandRequest.builder()
                .textoTranscrito("crear clase Proveedor")
                .build();

        mockMvc.perform(post("/api/modelos/" + modeloId + "/ai/voice")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.modeloActualizado.clases[*].nombre", hasItem("Proveedor")));
    }

    @Test
    @DisplayName("API: GET /api/modelos/{id}/ai/history devuelve historial de auditoría de comandos IA")
    void testHistorialComandos() throws Exception {
        // Ejecutar un comando previo
        AICommandRequest request = AICommandRequest.builder()
                .prompt("crear clase Vehiculo")
                .build();

        mockMvc.perform(post("/api/modelos/" + modeloId + "/ai/command")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Consultar historial
        mockMvc.perform(get("/api/modelos/" + modeloId + "/ai/history")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].prompt").value("crear clase Vehiculo"))
                .andExpect(jsonPath("$[0].ejecutadoConExito").value(true));
    }

    @Test
    @DisplayName("API: Restricción estricta de alcance rechaza generar sistemas completos desde cero")
    void testRechazoGeneracionSistemaCompleto() throws Exception {
        AICommandRequest request = AICommandRequest.builder()
                .prompt("generar sistema completo de facturación electrónica con base de datos")
                .build();

        mockMvc.perform(post("/api/modelos/" + modeloId + "/ai/command")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("No genera sistemas completos desde cero")));
    }
}
