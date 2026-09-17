package com.caseplatform.integration.enterprisearchitect;

import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.ProyectoUMLDTO;
import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import com.caseplatform.model.Usuario;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.ClaseUMLRepository;
import com.caseplatform.repository.ProyectoUMLRepository;
import com.caseplatform.repository.RelacionUMLRepository;
import com.caseplatform.repository.UsuarioRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas de Integración - EnterpriseArchitectController (Fase 10)")
class EnterpriseArchitectControllerIntegrationTest {

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
    private Long proyectoId;

    @BeforeEach
    void setUp() throws Exception {
        limpiarBaseDatos();

        Usuario usuario = Usuario.builder()
                .nombreCompleto("Ingeniero Enterprise Architect")
                .email("ingeniero.ea@caseplatform.com")
                .password(passwordEncoder.encode("Password123*"))
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        token = "Bearer " + jwtService.generarToken(usuario);

        // 1. Crear proyecto y modelo inicial
        ProyectoUMLDTO proyDto = ProyectoUMLDTO.builder()
                .nombre("Proyecto Interoperable EA")
                .descripcion("Pruebas de integración XMI Enterprise Architect")
                .build();

        MvcResult resultProy = mockMvc.perform(post("/api/proyectos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proyDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(resultProy.getResponse().getContentAsString());
        this.proyectoId = rootNode.path("id").asLong();
        this.modeloId = rootNode.path("modelos").get(0).path("id").asLong();

        // 2. Crear clase inicial "Empleado"
        ClaseUMLDTO claseDto = ClaseUMLDTO.builder()
                .nombre("Empleado")
                .posicionX(150.0)
                .posicionY(150.0)
                .visibilidad(VisibilidadUML.PUBLIC)
                .descripcion("Clase inicial de prueba")
                .atributos(List.of(
                        AtributoUMLDTO.builder().nombre("legajo").tipoDato("String").visibilidad(VisibilidadUML.PRIVATE).build()
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
        try {
            imagenUMLRepository.deleteAll();
            historialCambioRepository.deleteAll();
            versionModeloRepository.deleteAll();
            relacionUMLRepository.deleteAll();
            claseUMLRepository.deleteAll();
            proyectoUMLRepository.deleteAll();
            usuarioRepository.deleteAll();
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("POST /api/integration/ea/validate - Debe validar exitosamente un archivo XMI válido")
    void testValidateXMI() throws Exception {
        String xmiContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xmi:XMI xmi:version="2.1" xmlns:uml="http://schema.omg.org/spec/UML/2.1" xmlns:xmi="http://schema.omg.org/spec/XMI/2.1">
              <xmi:Documentation exporter="Enterprise Architect" exporterVersion="6.5"/>
              <uml:Model xmi:type="uml:Model" name="ModeloFacturacion" visibility="public">
                <packagedElement xmi:type="uml:Package" xmi:id="EAPK_1" name="Facturacion" visibility="public">
                  <packagedElement xmi:type="uml:Class" xmi:id="EAID_C1" name="Factura" visibility="public">
                    <ownedAttribute xmi:type="uml:Property" xmi:id="EAID_A1" name="numero" visibility="private">
                      <type xmi:type="uml:PrimitiveType" href="http://schema.omg.org/spec/UML/2.1/uml.xml#String"/>
                    </ownedAttribute>
                  </packagedElement>
                </packagedElement>
              </uml:Model>
            </xmi:XMI>
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "facturacion.xml",
                "application/xml",
                xmiContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/integration/ea/validate")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido", is(true)))
                .andExpect(jsonPath("$.validacion.totalClases", is(1)))
                .andExpect(jsonPath("$.validacion.totalAtributos", is(1)))
                .andExpect(jsonPath("$.preview.nombre", is("ModeloFacturacion")));
    }

    @Test
    @DisplayName("POST /api/integration/ea/models/{id}/import - Debe importar clases y relaciones desde XMI")
    void testImportToExistingModel() throws Exception {
        String xmiContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xmi:XMI xmi:version="2.1" xmlns:uml="http://schema.omg.org/spec/UML/2.1" xmlns:xmi="http://schema.omg.org/spec/XMI/2.1">
              <uml:Model xmi:type="uml:Model" name="ImportadoEA" visibility="public">
                <packagedElement xmi:type="uml:Package" xmi:id="EAPK_1" name="Default" visibility="public">
                  <packagedElement xmi:type="uml:Class" xmi:id="EAID_C10" name="Cliente" visibility="public">
                    <ownedAttribute xmi:type="uml:Property" xmi:id="EAID_A10" name="email" visibility="private"/>
                  </packagedElement>
                  <packagedElement xmi:type="uml:Class" xmi:id="EAID_C20" name="Orden" visibility="public">
                    <ownedAttribute xmi:type="uml:Property" xmi:id="EAID_A20" name="total" visibility="private"/>
                  </packagedElement>
                  <packagedElement xmi:type="uml:Association" xmi:id="EAID_ASSOC_1" name="ClienteOrden">
                    <memberEnd xmi:idref="EAID_END_2"/>
                    <memberEnd xmi:idref="EAID_END_1"/>
                    <ownedEnd xmi:type="uml:Property" xmi:id="EAID_END_1" type="EAID_C10" association="EAID_ASSOC_1"/>
                    <ownedEnd xmi:type="uml:Property" xmi:id="EAID_END_2" type="EAID_C20" association="EAID_ASSOC_1"/>
                  </packagedElement>
                </packagedElement>
              </uml:Model>
            </xmi:XMI>
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagrama_ea.xmi",
                "application/xml",
                xmiContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/integration/ea/models/" + modeloId + "/import")
                        .file(file)
                        .param("limpiarExistente", "false")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito", is(true)))
                .andExpect(jsonPath("$.clasesImportadas", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.relacionesImportadas", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.versionNumero", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/integration/ea/models/{id}/export - Debe exportar el modelo a XML/XMI descargable")
    void testExportModelToXMI() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/integration/ea/models/" + modeloId + "/export")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")))
                .andReturn();

        String xmlResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(xmlResponse.contains("<xmi:XMI"));
        assertTrue(xmlResponse.contains("<uml:Model"));
        assertTrue(xmlResponse.contains("Empleado"));
    }
}
