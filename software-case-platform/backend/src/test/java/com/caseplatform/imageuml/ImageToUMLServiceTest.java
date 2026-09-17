package com.caseplatform.imageuml;

import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.imageuml.detector.UMLDetectorService;
import com.caseplatform.imageuml.dto.*;
import com.caseplatform.imageuml.model.EstadoProcesamientoImagen;
import com.caseplatform.imageuml.model.ImagenUML;
import com.caseplatform.imageuml.processor.ImageProcessorService;
import com.caseplatform.imageuml.repository.ImagenUMLRepository;
import com.caseplatform.imageuml.service.impl.ImageToUMLServiceImpl;
import com.caseplatform.model.*;
import com.caseplatform.repository.*;
import com.caseplatform.service.ModeloUMLService;
import com.caseplatform.versioning.service.VersionService;
import com.caseplatform.websocket.service.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.awt.image.BufferedImage;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - ImageToUMLService (Orquestación y Persistencia)")
class ImageToUMLServiceTest {

    @Mock
    private ImageProcessorService imageProcessorService;

    @Mock
    private UMLDetectorService umlDetectorService;

    @Mock
    private ImagenUMLRepository imagenUMLRepository;

    @Mock
    private ModeloUMLRepository modeloUMLRepository;

    @Mock
    private ClaseUMLRepository claseUMLRepository;

    @Mock
    private AtributoUMLRepository atributoUMLRepository;

    @Mock
    private RelacionUMLRepository relacionUMLRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ModeloUMLService modeloUMLService;

    @Mock
    private VersionService versionService;

    @Mock
    private EventPublisher eventPublisher;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ImageToUMLServiceImpl imageToUMLService;

    private ModeloUML modeloMock;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .email("ingeniero@caseplatform.com")
                .nombreCompleto("Ingeniero de Software")
                .build();

        modeloMock = ModeloUML.builder()
                .id(10L)
                .nombre("Diagrama Conceptual")
                .version("1.0")
                .clases(new ArrayList<>())
                .relaciones(new ArrayList<>())
                .build();

        imageToUMLService.setVersionService(versionService);
        imageToUMLService.setEventPublisher(eventPublisher);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el archivo de imagen está vacío")
    void testSubirYProcesarImagen_ArchivoVacio_LanzaExcepcion() {
        MockMultipartFile fileVacio = new MockMultipartFile("file", "test.png", "image/png", new byte[0]);

        assertThrows(ValidationException.class, () ->
                imageToUMLService.subirYProcesarImagen(fileVacio, 10L, "ingeniero@caseplatform.com")
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción si el formato del archivo no es permitido (ej. PDF o TXT)")
    void testSubirYProcesarImagen_FormatoInvalido_LanzaExcepcion() {
        MockMultipartFile filePdf = new MockMultipartFile("file", "documento.pdf", "application/pdf", new byte[]{1, 2, 3});

        assertThrows(ValidationException.class, () ->
                imageToUMLService.subirYProcesarImagen(filePdf, 10L, "ingeniero@caseplatform.com")
        );
    }

    @Test
    @DisplayName("Debe procesar la imagen y registrar la entidad ImagenUML con sus elementos detectados")
    void testSubirYProcesarImagen_Exitoso() {
        byte[] fakeImageBytes = new byte[]{1, 2, 3, 4, 5};
        MockMultipartFile file = new MockMultipartFile("file", "diagrama_ventas.png", "image/png", fakeImageBytes);

        BufferedImage imgMock = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);

        when(usuarioRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(usuarioMock));
        when(modeloUMLRepository.findById(10L)).thenReturn(Optional.of(modeloMock));
        when(imageProcessorService.fromByteArray(any())).thenReturn(imgMock);
        when(imageProcessorService.preprocess(any())).thenReturn(imgMock);

        ImageUMLDetectedDTO detectedDTO = ImageUMLDetectedDTO.builder()
                .clases(List.of(
                        ClaseDetectadaDTO.builder().nombre("Cliente").atributos(new ArrayList<>()).metodos(new ArrayList<>()).build(),
                        ClaseDetectadaDTO.builder().nombre("Factura").atributos(new ArrayList<>()).metodos(new ArrayList<>()).build()
                ))
                .relaciones(List.of(
                        RelacionDetectadaDTO.builder().claseOrigen("Cliente").claseDestino("Factura").tipoRelacion(TipoRelacionUML.ASOCIACION).build()
                ))
                .build();

        when(umlDetectorService.detectUMLFromImage(any(), any(), anyString())).thenReturn(detectedDTO);

        ImagenUML savedEntity = ImagenUML.builder()
                .id(100L)
                .nombreArchivo("diagrama_ventas.png")
                .tamanioBytes((long) fakeImageBytes.length)
                .ancho(300)
                .alto(200)
                .estado(EstadoProcesamientoImagen.PROCESADA)
                .build();

        when(imagenUMLRepository.save(any(ImagenUML.class))).thenReturn(savedEntity);

        ImageUploadResponseDTO response = imageToUMLService.subirYProcesarImagen(file, 10L, "ingeniero@caseplatform.com");

        assertNotNull(response);
        assertEquals(100L, response.getIdImagen());
        assertEquals("diagrama_ventas.png", response.getNombreArchivo());
        assertEquals(EstadoProcesamientoImagen.PROCESADA, response.getEstadoProcesamiento());
        assertNotNull(response.getResultadoUML());
        assertEquals(2, response.getResultadoUML().getClases().size());
        assertEquals(1, response.getResultadoUML().getRelaciones().size());

        verify(imagenUMLRepository, times(1)).save(any(ImagenUML.class));
    }

    @Test
    @DisplayName("Debe aplicar el modelo detectado a las entidades JPA del modelo UML y registrar versión")
    void testAplicarModeloDetectado_Exitoso() {
        Long modeloId = 10L;
        when(modeloUMLRepository.findById(modeloId)).thenReturn(Optional.of(modeloMock));

        ClaseUML clienteGuardado = ClaseUML.builder().id(1L).nombre("Cliente").modeloUML(modeloMock).atributos(new ArrayList<>()).metodos(new ArrayList<>()).build();
        ClaseUML facturaGuardada = ClaseUML.builder().id(2L).nombre("Factura").modeloUML(modeloMock).atributos(new ArrayList<>()).metodos(new ArrayList<>()).build();

        when(claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(eq(modeloId), eq("Cliente"))).thenReturn(Optional.empty());
        when(claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(eq(modeloId), eq("Factura"))).thenReturn(Optional.empty());
        when(claseUMLRepository.save(any(ClaseUML.class))).thenReturn(clienteGuardado, facturaGuardada);

        when(relacionUMLRepository.findByModeloUMLId(modeloId)).thenReturn(Collections.emptyList());
        when(relacionUMLRepository.save(any(RelacionUML.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ModeloUMLDTO dtoFinal = ModeloUMLDTO.builder().id(modeloId).nombre("Diagrama Conceptual").build();
        when(modeloUMLService.obtenerPorId(modeloId)).thenReturn(dtoFinal);

        ImageUMLDetectedDTO modeloADetectar = ImageUMLDetectedDTO.builder()
                .clases(List.of(
                        ClaseDetectadaDTO.builder()
                                .nombre("Cliente")
                                .atributos(List.of(AtributoDetectadoDTO.builder().nombre("nombre").tipoDato("String").build()))
                                .metodos(new ArrayList<>())
                                .build(),
                        ClaseDetectadaDTO.builder()
                                .nombre("Factura")
                                .atributos(List.of(AtributoDetectadoDTO.builder().nombre("total").tipoDato("Double").build()))
                                .metodos(new ArrayList<>())
                                .build()
                ))
                .relaciones(List.of(
                        RelacionDetectadaDTO.builder()
                                .claseOrigen("Cliente")
                                .claseDestino("Factura")
                                .tipoRelacion(TipoRelacionUML.ASOCIACION)
                                .cardinalidadOrigen("1")
                                .cardinalidadDestino("*")
                                .build()
                ))
                .build();

        ApplyImageUMLRequestDTO request = ApplyImageUMLRequestDTO.builder()
                .modeloAjustado(modeloADetectar)
                .build();

        ModeloUMLDTO resultado = imageToUMLService.aplicarModeloDetectado(modeloId, request, "ingeniero@caseplatform.com");

        assertNotNull(resultado);
        assertEquals(modeloId, resultado.getId());

        // Verificar que se guardaron las clases y relaciones
        verify(claseUMLRepository, atLeastOnce()).save(any(ClaseUML.class));
        verify(relacionUMLRepository, atLeastOnce()).save(any(RelacionUML.class));

        // Verificar integración con versionado y WebSocket
        verify(versionService, times(1)).crearVersion(any(), anyString());
        verify(eventPublisher, times(1)).publishEvent(eq(modeloId), any());
    }
}
