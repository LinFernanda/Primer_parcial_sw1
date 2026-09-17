package com.caseplatform.generator;

import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.ProyectoUMLDTO;
import com.caseplatform.generator.dto.GeneratorRequestDTO;
import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import com.caseplatform.model.Usuario;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.*;
import com.caseplatform.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas de Integración - GeneratorController (Fase 9)")
class GeneratorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProyectoUMLRepository proyectoUMLRepository;

    @Autowired
    private ClaseUMLRepository claseUMLRepository;

    @Autowired
    private RelacionUMLRepository relacionUMLRepository;

    @Autowired
    private com.caseplatform.imageuml.repository.ImagenUMLRepository imagenUMLRepository;

    @Autowired
    private com.caseplatform.versioning.repository.HistorialCambioRepository historialCambioRepository;

    @Autowired
    private com.caseplatform.versioning.repository.VersionModeloRepository versionModeloRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String token;
    private Long modeloId;

    @BeforeEach
    void setUp() throws Exception {
        limpiarBaseDatos();

        Usuario usuario = Usuario.builder()
                .nombreCompleto("Ingeniero de Software CASE")
                .email("ingeniero.generator@caseplatform.com")
                .password(passwordEncoder.encode("Password123*"))
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        token = "Bearer " + jwtService.generarToken(usuario);

        // 1. Crear proyecto y modelo inicial
        ProyectoUMLDTO proyDto = ProyectoUMLDTO.builder()
                .nombre("Proyecto Tienda Online")
                .descripcion("Pruebas de generación automática de backend Spring Boot")
                .build();

        MvcResult resultProy = mockMvc.perform(post("/api/proyectos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proyDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(resultProy.getResponse().getContentAsString());
        this.modeloId = rootNode.path("modelos").get(0).path("id").asLong();

        // 2. Crear una clase con atributos en el modelo
        ClaseUMLDTO claseDto = ClaseUMLDTO.builder()
                .nombre("Producto")
                .posicionX(100.0)
                .posicionY(100.0)
                .visibilidad(VisibilidadUML.PUBLIC)
                .descripcion("Entidad representativa de inventario")
                .atributos(List.of(
                        AtributoUMLDTO.builder().nombre("nombre").tipoDato("String").visibilidad(VisibilidadUML.PRIVATE).build(),
                        AtributoUMLDTO.builder().nombre("precio").tipoDato("Double").visibilidad(VisibilidadUML.PRIVATE).build()
                ))
                .build();

        mockMvc.perform(post("/api/modelos/" + modeloId + "/clases")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claseDto)))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void tearDown() {
        limpiarBaseDatos();
    }

    private void limpiarBaseDatos() {
        historialCambioRepository.deleteAll();
        versionModeloRepository.deleteAll();
        imagenUMLRepository.deleteAll();
        relacionUMLRepository.deleteAll();
        claseUMLRepository.deleteAll();
        proyectoUMLRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/generator/project/{modeloId}/preview retorna estructura de archivos generada")
    void testPreviewProjectEndpoint() throws Exception {
        mockMvc.perform(get("/api/generator/project/" + modeloId + "/preview")
                        .header("Authorization", token)
                        .param("packageName", "com.tienda.backend")
                        .param("projectName", "TiendaBackend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modeloId").value(modeloId))
                .andExpect(jsonPath("$.projectName").value("TiendaBackend"))
                .andExpect(jsonPath("$.packageName").value("com.tienda.backend"))
                .andExpect(jsonPath("$.totalEntities").value(1))
                .andExpect(jsonPath("$.totalFiles", greaterThanOrEqualTo(9)))
                .andExpect(jsonPath("$.files[*].relativePath", hasItem("pom.xml")))
                .andExpect(jsonPath("$.files[*].relativePath", hasItem(containsString("Producto.java"))))
                .andExpect(jsonPath("$.files[*].relativePath", hasItem(containsString("ProductoRepository.java"))))
                .andExpect(jsonPath("$.files[*].relativePath", hasItem(containsString("ProductoService.java"))))
                .andExpect(jsonPath("$.files[*].relativePath", hasItem(containsString("ProductoController.java"))));
    }

    @Test
    @DisplayName("POST /api/generator/project/{modeloId} genera el proyecto y retorna resumen con enlace zip")
    void testGenerateProjectEndpoint() throws Exception {
        GeneratorRequestDTO request = GeneratorRequestDTO.builder()
                .packageName("com.empresa.ventas")
                .projectName("VentasService")
                .serverPort(8085)
                .databaseName("ventas_db")
                .build();

        mockMvc.perform(post("/api/generator/project/" + modeloId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modeloId").value(modeloId))
                .andExpect(jsonPath("$.projectName").value("VentasService"))
                .andExpect(jsonPath("$.totalEntities").value(1))
                .andExpect(jsonPath("$.zipDownloadUrl").value("/api/generator/project/" + modeloId + "/zip"))
                .andExpect(jsonPath("$.mensaje", containsString("exitosamente")));
    }

    @Test
    @DisplayName("GET /api/generator/project/{modeloId}/zip descarga el paquete binario .ZIP")
    void testDownloadProjectZipEndpoint() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/generator/project/" + modeloId + "/zip")
                        .header("Authorization", token)
                        .param("projectName", "MiBackend"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"MiBackend.zip\"")))
                .andExpect(content().contentType("application/zip"))
                .andReturn();

        byte[] zipBytes = mvcResult.getResponse().getContentAsByteArray();
        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 500); // El ZIP con los archivos Java, XML y YML pesa más de 500 bytes
    }
}
