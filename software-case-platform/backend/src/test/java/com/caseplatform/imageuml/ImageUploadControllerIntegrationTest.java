package com.caseplatform.imageuml;

import com.caseplatform.dto.uml.ProyectoUMLDTO;
import com.caseplatform.imageuml.dto.ApplyImageUMLRequestDTO;
import com.caseplatform.imageuml.dto.ClaseDetectadaDTO;
import com.caseplatform.imageuml.dto.ImageUMLDetectedDTO;
import com.caseplatform.imageuml.dto.RelacionDetectadaDTO;
import com.caseplatform.imageuml.repository.ImagenUMLRepository;
import com.caseplatform.model.*;
import com.caseplatform.repository.ClaseUMLRepository;
import com.caseplatform.repository.ProyectoUMLRepository;
import com.caseplatform.repository.RelacionUMLRepository;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas de Integración - ImageUploadController (Fase 8)")
class ImageUploadControllerIntegrationTest {

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
    private ImagenUMLRepository imagenUMLRepository;

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
    private byte[] samplePngBytes;

    @BeforeEach
    void setUp() throws Exception {
        limpiarBaseDatos();

        Usuario usuario = Usuario.builder()
                .nombreCompleto("Ingeniero Visión Artificial")
                .email("ingeniero.vision@caseplatform.com")
                .password(passwordEncoder.encode("Password123*"))
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        token = "Bearer " + jwtService.generarToken(usuario);

        // Crear proyecto base con su modelo inicial
        ProyectoUMLDTO proyDto = ProyectoUMLDTO.builder()
                .nombre("Proyecto Image To UML")
                .descripcion("Pruebas de visión artificial para UML")
                .build();

        MvcResult resultProy = mockMvc.perform(post("/api/proyectos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proyDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(resultProy.getResponse().getContentAsString());
        this.modeloId = rootNode.path("modelos").get(0).path("id").asLong();

        // Generar imagen sintética válida PNG
        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        try {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 300, 200);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(20, 20, 100, 80);
            g2d.drawRect(160, 20, 100, 80);
            g2d.drawLine(120, 60, 160, 60);
        } finally {
            g2d.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        this.samplePngBytes = baos.toByteArray();
    }

    @Test
    @DisplayName("POST /api/imageuml/upload debe subir imagen válida (PNG) y retornar elementos detectados")
    void testUploadImage_FormatoValido_Retorna201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagrama_ventas.png",
                "image/png",
                samplePngBytes
        );

        mockMvc.perform(multipart("/api/imageuml/upload")
                        .file(file)
                        .param("modeloId", modeloId.toString())
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idImagen").isNumber())
                .andExpect(jsonPath("$.nombreArchivo").value("diagrama_ventas.png"))
                .andExpect(jsonPath("$.estadoProcesamiento").value("PROCESADA"))
                .andExpect(jsonPath("$.resultadoUML").exists())
                .andExpect(jsonPath("$.resultadoUML.clases.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("POST /api/imageuml/upload debe rechazar formatos no permitidos (ej. .txt o .exe)")
    void testUploadImage_FormatoInvalido_Retorna400() throws Exception {
        MockMultipartFile badFile = new MockMultipartFile(
                "file",
                "malicioso.exe",
                "application/octet-stream",
                new byte[]{1, 2, 3, 4}
        );

        mockMvc.perform(multipart("/api/imageuml/upload")
                        .file(badFile)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/imageuml/upload debe rechazar archivos vacíos")
    void testUploadImage_ArchivoVacio_Retorna400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "vacio.png",
                "image/png",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/imageuml/upload")
                        .file(emptyFile)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/imageuml/models/{modeloId}/apply debe aplicar modelo detectado a la pizarra UML")
    void testApplyDetectedModel_Exitoso() throws Exception {
        // 1. Subir imagen previa
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagrama_ecommerce.png",
                "image/png",
                samplePngBytes
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/imageuml/upload")
                        .file(file)
                        .param("modeloId", modeloId.toString())
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode uploadNode = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        long imagenId = uploadNode.path("idImagen").asLong();

        // 2. Aplicar modelo detectado
        ImageUMLDetectedDTO modeloAjustado = ImageUMLDetectedDTO.builder()
                .clases(List.of(
                        ClaseDetectadaDTO.builder().nombre("Cliente").atributos(new ArrayList<>()).metodos(new ArrayList<>()).build(),
                        ClaseDetectadaDTO.builder().nombre("Pedido").atributos(new ArrayList<>()).metodos(new ArrayList<>()).build()
                ))
                .relaciones(List.of(
                        RelacionDetectadaDTO.builder()
                                .claseOrigen("Cliente")
                                .claseDestino("Pedido")
                                .tipoRelacion(TipoRelacionUML.ASOCIACION)
                                .cardinalidadOrigen("1")
                                .cardinalidadDestino("*")
                                .build()
                ))
                .build();

        ApplyImageUMLRequestDTO applyReq = ApplyImageUMLRequestDTO.builder()
                .idImagen(imagenId)
                .modeloAjustado(modeloAjustado)
                .limpiarModeloExistente(false)
                .build();

        mockMvc.perform(post("/api/imageuml/models/" + modeloId + "/apply")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(modeloId))
                .andExpect(jsonPath("$.clases.length()", greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("POST /api/imageuml/models/{modeloId}/convert debe subir y convertir imagen en un solo paso")
    void testConvertAndApplyDirectly_Exitoso() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagrama_universidad.png",
                "image/png",
                samplePngBytes
        );

        mockMvc.perform(multipart("/api/imageuml/models/" + modeloId + "/convert")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(modeloId))
                .andExpect(jsonPath("$.clases.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/imageuml/{imagenId} debe retornar metadata y estado de la imagen")
    void testGetImageDetail_Exitoso() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagrama_test.png",
                "image/png",
                samplePngBytes
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/imageuml/upload")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andReturn();

        long idImagen = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).path("idImagen").asLong();

        mockMvc.perform(get("/api/imageuml/" + idImagen)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idImagen").value(idImagen))
                .andExpect(jsonPath("$.estadoProcesamiento").value("PROCESADA"));
    }

    @Test
    @DisplayName("GET /api/imageuml/{imagenId}/file debe retornar los bytes originales de la imagen")
    void testGetImageFile_Exitoso() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagrama_visual.png",
                "image/png",
                samplePngBytes
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/imageuml/upload")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andReturn();

        long idImagen = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).path("idImagen").asLong();

        MvcResult fileResult = mockMvc.perform(get("/api/imageuml/" + idImagen + "/file")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_PNG_VALUE))
                .andReturn();

        assertNotNull(fileResult.getResponse().getContentAsByteArray());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        limpiarBaseDatos();
    }

    private void limpiarBaseDatos() {
        imagenUMLRepository.deleteAll();
        historialCambioRepository.deleteAll();
        versionModeloRepository.deleteAll();
        relacionUMLRepository.deleteAll();
        claseUMLRepository.deleteAll();
        proyectoUMLRepository.deleteAll();
        usuarioRepository.deleteAll();
    }
}
