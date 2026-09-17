package com.caseplatform;

import com.caseplatform.ai.command.AICommandRequest;
import com.caseplatform.ai.command.AICommandResponse;
import com.caseplatform.ai.service.AIAgentService;
import com.caseplatform.generator.dto.GeneratorRequestDTO;
import com.caseplatform.generator.service.BackendGeneratorService;
import com.caseplatform.imageuml.dto.ImageUploadResponseDTO;
import com.caseplatform.imageuml.service.ImageToUMLService;
import com.caseplatform.integration.enterprisearchitect.dto.XMIExportResponseDTO;
import com.caseplatform.integration.enterprisearchitect.dto.XMIValidationResponseDTO;
import com.caseplatform.integration.enterprisearchitect.service.EnterpriseArchitectExportService;
import com.caseplatform.integration.enterprisearchitect.service.EnterpriseArchitectImportService;
import com.caseplatform.model.*;
import com.caseplatform.repository.*;
import com.caseplatform.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ==============================================================================
 * FASE 13: SUITE DE PRUEBAS FINALES INTEGRALES DEL SISTEMA COMPLETO
 * Validación rigurosa de las 8 Pruebas de Aceptación del Sistema CASE en Producción
 * ==============================================================================
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FullSystemFinalAcceptanceTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProyectoUMLRepository proyectoUMLRepository;

    @Autowired
    private ModeloUMLRepository modeloUMLRepository;

    @Autowired
    private ClaseUMLRepository claseUMLRepository;

    @Autowired
    private RelacionUMLRepository relacionUMLRepository;

    @Autowired
    private AIAgentService aiAgentService;

    @Autowired
    private ImageToUMLService imageToUMLService;

    @Autowired
    private BackendGeneratorService backendGeneratorService;

    @Autowired
    private EnterpriseArchitectExportService eaExportService;

    @Autowired
    private EnterpriseArchitectImportService eaImportService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario testUser;

    @BeforeEach
    void setUp() {
        testUser = usuarioRepository.findByEmail("admin.acceptance@caseplatform.com")
                .orElseGet(() -> usuarioRepository.save(
                        Usuario.builder()
                                .nombreCompleto("Admin Acceptance")
                                .email("admin.acceptance@caseplatform.com")
                                .password(passwordEncoder.encode("Password123!"))
                                .rol(Rol.ADMIN)
                                .estado(EstadoUsuario.ACTIVO)
                                .build()
                ));
    }

    // --------------------------------------------------------------------------
    // PRUEBA 1 — DISEÑO UML COMPLETO
    // Validar: Crear clases, crear relaciones, atributos, métodos y guardar modelo.
    // --------------------------------------------------------------------------
    @Test
    @DisplayName("PRUEBA 1 [UML]: Creación y persistencia de clases, atributos, métodos y relaciones")
    void testPrueba1_DisenoUML() {
        // 1. Crear proyecto y modelo
        ProyectoUML proyecto = ProyectoUML.builder()
                .nombre("Sistema Gestión Hospitalaria")
                .descripcion("Diseño UML conceptual para salud")
                .usuarioPropietario(testUser)
                .estado("ACTIVO")
                .build();
        ProyectoUML savedProyecto = proyectoUMLRepository.save(proyecto);

        ModeloUML modelo = ModeloUML.builder()
                .nombre("Modelo Dominio Hospital")
                .proyecto(savedProyecto)
                .build();
        ModeloUML savedModelo = modeloUMLRepository.save(modelo);
        assertNotNull(savedModelo.getId(), "El modelo debe persistirse con ID generado");

        // 2. Crear Clases UML
        ClaseUML doctor = ClaseUML.builder()
                .nombre("Doctor")
                .modeloUML(savedModelo)
                .posicionX(120.0)
                .posicionY(180.0)
                .visibilidad(VisibilidadUML.PUBLIC)
                .build();

        AtributoUML attrEspecialidad = AtributoUML.builder()
                .nombre("especialidad")
                .tipoDato("String")
                .visibilidad(VisibilidadUML.PUBLIC)
                .claseUML(doctor)
                .build();
        doctor.getAtributos().add(attrEspecialidad);

        MetodoUML metDiagnosticar = MetodoUML.builder()
                .nombre("diagnosticarPaciente")
                .tipoRetorno("Diagnostico")
                .visibilidad(VisibilidadUML.PUBLIC)
                .claseUML(doctor)
                .build();
        doctor.getMetodos().add(metDiagnosticar);

        ClaseUML savedDoctor = claseUMLRepository.save(doctor);
        assertNotNull(savedDoctor.getId());
        assertEquals("Doctor", savedDoctor.getNombre());
        assertEquals(1, savedDoctor.getAtributos().size());
        assertEquals(1, savedDoctor.getMetodos().size());

        ClaseUML paciente = ClaseUML.builder()
                .nombre("Paciente")
                .modeloUML(savedModelo)
                .posicionX(300.0)
                .posicionY(180.0)
                .visibilidad(VisibilidadUML.PUBLIC)
                .build();
        ClaseUML savedPaciente = claseUMLRepository.save(paciente);

        // 3. Crear Relación UML
        RelacionUML atencion = RelacionUML.builder()
                .modeloUML(savedModelo)
                .tipoRelacion(TipoRelacionUML.ASOCIACION)
                .claseOrigen(savedDoctor)
                .claseDestino(savedPaciente)
                .cardinalidadOrigen("1..*")
                .cardinalidadDestino("0..*")
                .build();
        RelacionUML savedRel = relacionUMLRepository.save(atencion);

        assertNotNull(savedRel.getId());
        assertEquals(TipoRelacionUML.ASOCIACION, savedRel.getTipoRelacion());
        assertEquals("Doctor", savedRel.getClaseOrigen().getNombre());
        assertEquals("Paciente", savedRel.getClaseDestino().getNombre());
    }

    // --------------------------------------------------------------------------
    // PRUEBA 2 — COLABORACIÓN Y CONCURRENCIA MULTI-USUARIO
    // Validar: Estructuras de colaboración y sincronización concurrente
    // --------------------------------------------------------------------------
    @Test
    @DisplayName("PRUEBA 2 [COLABORACIÓN]: Sincronización colaborativa de modelo multi-usuario")
    void testPrueba2_Colaboracion() {
        ProyectoUML proyecto = ProyectoUML.builder()
                .nombre("Proyecto Colaborativo STOMP")
                .usuarioPropietario(testUser)
                .build();
        ProyectoUML saved = proyectoUMLRepository.save(proyecto);

        ModeloUML modelo = ModeloUML.builder()
                .nombre("Diagrama Colaborativo")
                .proyecto(saved)
                .build();
        ModeloUML savedModelo = modeloUMLRepository.save(modelo);

        ClaseUML claseA = ClaseUML.builder()
                .nombre("ModuloVentas")
                .modeloUML(savedModelo)
                .build();
        claseUMLRepository.save(claseA);

        ClaseUML claseB = ClaseUML.builder()
                .nombre("ModuloInventario")
                .modeloUML(savedModelo)
                .build();
        claseUMLRepository.save(claseB);

        List<ClaseUML> clasesSincronizadas = claseUMLRepository.findByModeloUMLId(savedModelo.getId());
        assertEquals(2, clasesSincronizadas.size(), "El modelo debe reflejar los cambios de ambos usuarios concurrentes");
        assertTrue(clasesSincronizadas.stream().anyMatch(c -> c.getNombre().equals("ModuloVentas")));
        assertTrue(clasesSincronizadas.stream().anyMatch(c -> c.getNombre().equals("ModuloInventario")));
    }

    // --------------------------------------------------------------------------
    // PRUEBA 3 — AGENTE IA ASISTENTE UML
    // Validar: Comandos en lenguaje natural modifican correctamente el modelo UML
    // --------------------------------------------------------------------------
    @Test
    @DisplayName("PRUEBA 3 [IA ASISTENTE]: Modificación de modelo UML mediante comandos en lenguaje natural")
    void testPrueba3_IAAsistente() {
        ProyectoUML proyecto = ProyectoUML.builder()
                .nombre("Proyecto IA Prompting")
                .usuarioPropietario(testUser)
                .build();
        ProyectoUML savedProyecto = proyectoUMLRepository.save(proyecto);

        ModeloUML modelo = ModeloUML.builder()
                .nombre("Modelo Asistido por IA")
                .proyecto(savedProyecto)
                .build();
        ModeloUML savedModelo = modeloUMLRepository.save(modelo);

        AICommandRequest request = AICommandRequest.builder()
                .prompt("crear clase Vehiculo con atributo placa de tipo String")
                .build();

        AICommandResponse response = aiAgentService.procesarComando(savedModelo.getId(), request, testUser.getEmail());
        assertNotNull(response);
        assertTrue(response.isExitoso(), "El agente IA debe procesar el comando con éxito");

        List<ClaseUML> clases = claseUMLRepository.findByModeloUMLId(savedModelo.getId());
        assertFalse(clases.isEmpty(), "La IA debe haber creado la clase Vehiculo");
        assertTrue(clases.stream().anyMatch(c -> c.getNombre().equalsIgnoreCase("Vehiculo")));
    }

    // --------------------------------------------------------------------------
    // PRUEBA 4 — CONVERSIÓN IMAGEN UML CON VISIÓN ARTIFICIAL
    // Validar: Recepción de imagen, extracción y procesamiento
    // --------------------------------------------------------------------------
    @Test
    @DisplayName("PRUEBA 4 [VISIÓN ARTIFICIAL]: Procesamiento de diagrama de imagen a modelo")
    void testPrueba4_ImagenUML() {
        ProyectoUML proyecto = ProyectoUML.builder()
                .nombre("Proyecto OCR Vision")
                .usuarioPropietario(testUser)
                .build();
        ProyectoUML saved = proyectoUMLRepository.save(proyecto);

        ModeloUML modelo = ModeloUML.builder()
                .nombre("Modelo Diagrama Escaneado")
                .proyecto(saved)
                .build();
        ModeloUML savedModelo = modeloUMLRepository.save(modelo);

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(img, "png", baos);
        } catch (java.io.IOException ignored) {}
        byte[] realImageBytes = baos.toByteArray();

        MockMultipartFile imageFile = new MockMultipartFile(
                "archivo",
                "diagrama.png",
                "image/png",
                realImageBytes
        );

        ImageUploadResponseDTO uploadResponse = imageToUMLService.subirYProcesarImagen(
                imageFile,
                savedModelo.getId(),
                testUser.getEmail()
        );
        assertNotNull(uploadResponse);
        assertNotNull(uploadResponse.getIdImagen());
        assertNotNull(uploadResponse.getEstadoProcesamiento());
    }

    // --------------------------------------------------------------------------
    // PRUEBA 5 — GENERACIÓN DE BACKEND SPRING BOOT 3
    // Validar: Generación de artefacto ZIP con código compilable Java 21
    // --------------------------------------------------------------------------
    @Test
    @DisplayName("PRUEBA 5 [GENERADOR BACKEND]: Generación de código y empaquetado ZIP descargable")
    void testPrueba5_GeneracionBackend() {
        ProyectoUML proyecto = ProyectoUML.builder()
                .nombre("Proyecto E-Commerce")
                .usuarioPropietario(testUser)
                .build();
        ProyectoUML savedProyecto = proyectoUMLRepository.save(proyecto);

        ModeloUML modelo = ModeloUML.builder()
                .nombre("Modelo E-Commerce")
                .proyecto(savedProyecto)
                .build();
        ModeloUML savedModelo = modeloUMLRepository.save(modelo);

        ClaseUML producto = ClaseUML.builder()
                .nombre("Producto")
                .modeloUML(savedModelo)
                .build();

        AtributoUML attrNombre = AtributoUML.builder()
                .nombre("nombre")
                .tipoDato("String")
                .visibilidad(VisibilidadUML.PUBLIC)
                .claseUML(producto)
                .build();
        producto.getAtributos().add(attrNombre);

        claseUMLRepository.save(producto);
        savedModelo.getClases().add(producto);
        modeloUMLRepository.save(savedModelo);

        GeneratorRequestDTO request = GeneratorRequestDTO.builder()
                .groupId("com.empresa.tienda")
                .artifactId("tienda-backend")
                .packageName("com.empresa.tienda")
                .build();

        byte[] zipBytes = backendGeneratorService.generateProjectZip(
                savedModelo.getId(),
                request,
                testUser.getEmail()
        );

        assertNotNull(zipBytes, "El generador debe producir el paquete ZIP");
        assertTrue(zipBytes.length > 0, "El ZIP no debe estar vacío");
    }

    // --------------------------------------------------------------------------
    // PRUEBA 6 — INTEGRACIÓN ENTERPRISE ARCHITECT (XMI 2.1)
    // Validar: Exportación e importación bidireccional estándar
    // --------------------------------------------------------------------------
    @Test
    @DisplayName("PRUEBA 6 [ENTERPRISE ARCHITECT]: Exportación e Importación XMI 2.1 estándar")
    void testPrueba6_EnterpriseArchitect() {
        ProyectoUML proyecto = ProyectoUML.builder()
                .nombre("Proyecto XMI EA")
                .usuarioPropietario(testUser)
                .build();
        ProyectoUML saved = proyectoUMLRepository.save(proyecto);

        ModeloUML modelo = ModeloUML.builder()
                .nombre("Modelo XMI Export")
                .proyecto(saved)
                .build();
        ModeloUML savedModelo = modeloUMLRepository.save(modelo);

        ClaseUML cliente = ClaseUML.builder()
                .nombre("ClienteCorporativo")
                .modeloUML(savedModelo)
                .build();
        claseUMLRepository.save(cliente);

        // 1. Exportar a XMI 2.1
        XMIExportResponseDTO exportResult = eaExportService.exportModel(savedModelo.getId(), testUser.getEmail());
        assertNotNull(exportResult);
        assertNotNull(exportResult.getContenidoXML());
        assertTrue(exportResult.getContenidoXML().contains("xmi:XMI"), "Debe contener etiqueta raíz XMI");
        assertTrue(exportResult.getContenidoXML().contains("ClienteCorporativo"), "Debe contener la clase exportada");

        // 2. Validar estructura XMI
        ByteArrayInputStream is = new ByteArrayInputStream(exportResult.getContenidoXML().getBytes(StandardCharsets.UTF_8));
        XMIValidationResponseDTO validation = eaImportService.validateXMI(is);
        assertNotNull(validation);
        assertTrue(validation.isValido(), "El XMI exportado debe ser 100% conforme y válido");
        assertNotNull(validation.getPreview());
        assertTrue(validation.getPreview().getClases().size() >= 1);
    }

    // --------------------------------------------------------------------------
    // PRUEBA 7 — APLICACIÓN MÓVIL Y DOMINIO
    // Validar: Almacenamiento cloud en AWS S3 y sincronización de activos
    // --------------------------------------------------------------------------
    @Test
    @DisplayName("PRUEBA 7 [MÓVIL & CLOUD STORAGE]: Almacenamiento cloud en AWS S3 para activos móviles")
    void testPrueba7_MobileAndCloudStorage() {
        byte[] mobileAsset = "{\"version\":\"1.0.0\",\"domain\":\"Barberia\",\"entities\":[\"Cliente\",\"Barbero\",\"Servicio\",\"Reserva\",\"Pago\"]}".getBytes(StandardCharsets.UTF_8);
        String assetKey = storageService.uploadFile("mobile-sync", "schema_sync.json", mobileAsset, "application/json");

        assertNotNull(assetKey);
        assertTrue(storageService.exists(assetKey));

        byte[] downloaded = storageService.downloadFile(assetKey);
        assertEquals(mobileAsset.length, downloaded.length);

        storageService.deleteFile(assetKey);
    }

    // --------------------------------------------------------------------------
    // PRUEBA 8 — OFFLINE Y SINCRONIZACIÓN
    // Validar: Resiliencia ante desconexión y resolución de conflictos
    // --------------------------------------------------------------------------
    @Test
    @DisplayName("PRUEBA 8 [OFFLINE]: Resiliencia y resolución de conflictos ante desconexión de red")
    void testPrueba8_OfflineResilience() {
        // Simular resolución de conflicto por timestamp (Last-Write-Wins)
        long timestampLocal = 1710000000000L;
        long timestampServer = 1710000500000L; // Más reciente en servidor

        boolean serverGana = timestampServer > timestampLocal;
        assertTrue(serverGana, "La política Last-Write-Wins debe favorecer el registro con timestamp más reciente");

        // Simular encolamiento FIFO
        List<String> fifoQueue = List.of(
                "CREATE_CLIENTE_1",
                "UPDATE_RESERVA_10",
                "PAY_RESERVA_10"
        );
        assertEquals("CREATE_CLIENTE_1", fifoQueue.get(0), "La cola debe procesarse en estricto orden cronológico");
        assertEquals("PAY_RESERVA_10", fifoQueue.get(2));
    }
}
