package com.caseplatform;

import com.caseplatform.ai.command.AICommandRequest;
import com.caseplatform.ai.command.AICommandResponse;
import com.caseplatform.ai.command.ParsedAIAction;
import com.caseplatform.ai.command.VoiceCommandRequest;
import com.caseplatform.ai.model.AICommandHistory;
import com.caseplatform.ai.model.TipoOperacionAI;
import com.caseplatform.ai.parser.AICommandParser;
import com.caseplatform.ai.repository.AICommandHistoryRepository;
import com.caseplatform.ai.service.impl.AIAgentServiceImpl;
import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.dto.uml.RelacionUMLDTO;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.Usuario;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.AtributoUMLRepository;
import com.caseplatform.repository.ClaseUMLRepository;
import com.caseplatform.repository.ModeloUMLRepository;
import com.caseplatform.repository.RelacionUMLRepository;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.service.AtributoUMLService;
import com.caseplatform.service.ClaseUMLService;
import com.caseplatform.service.ModeloUMLService;
import com.caseplatform.service.RelacionUMLService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIAgentServiceTest {

    @Mock
    private AICommandParser aiCommandParser;

    @Mock
    private AICommandHistoryRepository aiCommandHistoryRepository;

    @Mock
    private ModeloUMLService modeloUMLService;

    @Mock
    private ClaseUMLService claseUMLService;

    @Mock
    private AtributoUMLService atributoUMLService;

    @Mock
    private RelacionUMLService relacionUMLService;

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
    private EventPublisher eventPublisher;

    @Mock
    private VersionService versionService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AIAgentServiceImpl aiAgentService;

    private ModeloUML modelo;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        modelo = ModeloUML.builder()
                .id(1L)
                .nombre("Sistema E-Commerce")
                .clases(new ArrayList<>())
                .relaciones(new ArrayList<>())
                .build();

        usuario = Usuario.builder()
                .id(10L)
                .email("ingeniero@caseplatform.com")
                .nombreCompleto("Ingeniero de Software")
                .build();
    }

    @Test
    @DisplayName("Debe procesar comando para crear clase con atributos de forma exitosa")
    void testProcesarComandoCrearClase() {
        when(modeloUMLRepository.findById(1L)).thenReturn(Optional.of(modelo));
        when(usuarioRepository.findByEmailIgnoreCase("ingeniero@caseplatform.com")).thenReturn(Optional.of(usuario));

        ParsedAIAction action = ParsedAIAction.builder()
                .tipoOperacion(TipoOperacionAI.CREATE_CLASS)
                .nombreClase("Factura")
                .visibilidad("PUBLIC")
                .atributos(List.of(
                        ParsedAIAction.AtributoSimple.builder().nombre("total").tipo("Double").visibilidad("PRIVATE").build()
                ))
                .build();

        when(aiCommandParser.parse("crear clase Factura con atributo total Double")).thenReturn(action);

        ClaseUMLDTO claseCreadaDTO = ClaseUMLDTO.builder()
                .id(100L)
                .nombre("Factura")
                .visibilidad(VisibilidadUML.PUBLIC)
                .build();

        when(claseUMLService.crearClase(eq(1L), any(ClaseUMLDTO.class))).thenReturn(claseCreadaDTO);
        when(modeloUMLService.obtenerPorId(1L)).thenReturn(ModeloUMLDTO.builder().id(1L).nombre("Sistema E-Commerce").build());

        AICommandRequest request = AICommandRequest.builder()
                .prompt("crear clase Factura con atributo total Double")
                .build();

        AICommandResponse response = aiAgentService.procesarComando(1L, request, "ingeniero@caseplatform.com");

        assertNotNull(response);
        assertTrue(response.isExitoso());
        assertFalse(response.isRequiereConfirmacion());
        assertTrue(response.getMensaje().contains("Factura"));

        verify(claseUMLService).crearClase(eq(1L), any(ClaseUMLDTO.class));
        verify(atributoUMLService).agregarAtributo(eq(100L), any(AtributoUMLDTO.class));
        verify(aiCommandHistoryRepository).save(any(AICommandHistory.class));
    }

    @Test
    @DisplayName("Debe detener ejecución y devolver pregunta cuando la acción es ambigua")
    void testProcesarComandoAmbiguedad() {
        when(modeloUMLRepository.findById(1L)).thenReturn(Optional.of(modelo));

        ParsedAIAction action = ParsedAIAction.builder()
                .tipoOperacion(TipoOperacionAI.CONFIRMATION_REQUIRED)
                .nombreClase("Pedido")
                .requiereConfirmacion(true)
                .preguntaConfirmacion("¿Desea crear una clase llamada 'Pedido'?")
                .build();

        when(aiCommandParser.parse("Crear pedido")).thenReturn(action);

        AICommandRequest request = AICommandRequest.builder()
                .prompt("Crear pedido")
                .build();

        AICommandResponse response = aiAgentService.procesarComando(1L, request, "ingeniero@caseplatform.com");

        assertNotNull(response);
        assertTrue(response.isRequiereConfirmacion());
        assertEquals("¿Desea crear una clase llamada 'Pedido'?", response.getPreguntaConfirmacion());

        verify(claseUMLService, never()).crearClase(any(), any());
        verify(aiCommandHistoryRepository).save(any(AICommandHistory.class));
    }

    @Test
    @DisplayName("Debe ejecutar la acción cuando el usuario envía confirmación explícita")
    void testProcesarComandoConfirmado() {
        when(modeloUMLRepository.findById(1L)).thenReturn(Optional.of(modelo));

        ParsedAIAction confirmedAction = ParsedAIAction.builder()
                .tipoOperacion(TipoOperacionAI.CREATE_CLASS)
                .nombreClase("Pedido")
                .visibilidad("PUBLIC")
                .build();

        ClaseUMLDTO claseCreadaDTO = ClaseUMLDTO.builder()
                .id(200L)
                .nombre("Pedido")
                .build();

        when(claseUMLService.crearClase(eq(1L), any(ClaseUMLDTO.class))).thenReturn(claseCreadaDTO);
        when(modeloUMLService.obtenerPorId(1L)).thenReturn(ModeloUMLDTO.builder().id(1L).build());

        AICommandRequest request = AICommandRequest.builder()
                .prompt("Crear pedido")
                .confirmado(true)
                .accionConfirmada(confirmedAction)
                .build();

        AICommandResponse response = aiAgentService.procesarComando(1L, request, "ingeniero@caseplatform.com");

        assertNotNull(response);
        assertTrue(response.isExitoso());
        assertFalse(response.isRequiereConfirmacion());
        verify(claseUMLService).crearClase(eq(1L), any(ClaseUMLDTO.class));
    }

    @Test
    @DisplayName("Debe procesar comando de voz transcribiendo y delegando la ejecución")
    void testProcesarVoz() {
        when(modeloUMLRepository.findById(1L)).thenReturn(Optional.of(modelo));

        ParsedAIAction action = ParsedAIAction.builder()
                .tipoOperacion(TipoOperacionAI.CREATE_CLASS)
                .nombreClase("Cliente")
                .build();

        when(aiCommandParser.parse("crear clase Cliente")).thenReturn(action);
        when(claseUMLService.crearClase(eq(1L), any(ClaseUMLDTO.class)))
                .thenReturn(ClaseUMLDTO.builder().id(300L).nombre("Cliente").build());
        when(modeloUMLService.obtenerPorId(1L)).thenReturn(ModeloUMLDTO.builder().id(1L).build());

        VoiceCommandRequest voiceReq = VoiceCommandRequest.builder()
                .textoTranscrito("crear clase Cliente")
                .build();

        AICommandResponse response = aiAgentService.procesarVoz(1L, voiceReq, "ingeniero@caseplatform.com");

        assertNotNull(response);
        assertTrue(response.isExitoso());
        verify(claseUMLService).crearClase(eq(1L), any(ClaseUMLDTO.class));
    }

    @Test
    @DisplayName("Debe agregar atributo a clase existente vía agente IA")
    void testProcesarComandoAgregarAtributo() {
        when(modeloUMLRepository.findById(1L)).thenReturn(Optional.of(modelo));

        ClaseUML claseCliente = ClaseUML.builder()
                .id(10L)
                .nombre("Cliente")
                .modeloUML(modelo)
                .build();

        when(claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(1L, "Cliente"))
                .thenReturn(Optional.of(claseCliente));

        ParsedAIAction action = ParsedAIAction.builder()
                .tipoOperacion(TipoOperacionAI.CREATE_ATTRIBUTE)
                .nombreClase("Cliente")
                .nombreAtributo("email")
                .tipoDatoAtributo("String")
                .visibilidad("PRIVATE")
                .build();

        when(aiCommandParser.parse("agregar atributo email de tipo String a Cliente")).thenReturn(action);
        when(atributoUMLService.agregarAtributo(eq(10L), any(AtributoUMLDTO.class)))
                .thenReturn(AtributoUMLDTO.builder().id(50L).nombre("email").tipoDato("String").build());
        when(modeloUMLService.obtenerPorId(1L)).thenReturn(ModeloUMLDTO.builder().id(1L).build());

        AICommandRequest request = AICommandRequest.builder()
                .prompt("agregar atributo email de tipo String a Cliente")
                .build();

        AICommandResponse response = aiAgentService.procesarComando(1L, request, "ingeniero@caseplatform.com");

        assertNotNull(response);
        assertTrue(response.isExitoso());
        verify(atributoUMLService).agregarAtributo(eq(10L), any(AtributoUMLDTO.class));
    }

    @Test
    @DisplayName("Debe crear relación entre dos clases existentes")
    void testProcesarComandoCrearRelacion() {
        when(modeloUMLRepository.findById(1L)).thenReturn(Optional.of(modelo));

        ClaseUML claseA = ClaseUML.builder().id(10L).nombre("Cliente").modeloUML(modelo).build();
        ClaseUML claseB = ClaseUML.builder().id(20L).nombre("Pedido").modeloUML(modelo).build();

        when(claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(1L, "Cliente")).thenReturn(Optional.of(claseA));
        when(claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(1L, "Pedido")).thenReturn(Optional.of(claseB));

        ParsedAIAction action = ParsedAIAction.builder()
                .tipoOperacion(TipoOperacionAI.CREATE_RELATION)
                .claseOrigen("Cliente")
                .claseDestino("Pedido")
                .tipoRelacion("ASOCIACION")
                .cardinalidadOrigen("1")
                .cardinalidadDestino("*")
                .build();

        when(aiCommandParser.parse("crear relacion entre Cliente y Pedido de 1 a *")).thenReturn(action);
        when(relacionUMLService.crearRelacion(any(RelacionUMLDTO.class)))
                .thenReturn(RelacionUMLDTO.builder().id(99L).build());
        when(modeloUMLService.obtenerPorId(1L)).thenReturn(ModeloUMLDTO.builder().id(1L).build());

        AICommandRequest request = AICommandRequest.builder()
                .prompt("crear relacion entre Cliente y Pedido de 1 a *")
                .build();

        AICommandResponse response = aiAgentService.procesarComando(1L, request, "ingeniero@caseplatform.com");

        assertNotNull(response);
        assertTrue(response.isExitoso());
        verify(relacionUMLService).crearRelacion(any(RelacionUMLDTO.class));
    }
}
