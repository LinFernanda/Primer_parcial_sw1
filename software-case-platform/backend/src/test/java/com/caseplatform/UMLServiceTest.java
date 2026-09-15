package com.caseplatform;

import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.MetodoUMLDTO;
import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.dto.uml.ProyectoUMLDTO;
import com.caseplatform.dto.uml.RelacionUMLDTO;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.ProyectoUML;
import com.caseplatform.model.RelacionUML;
import com.caseplatform.model.Rol;
import com.caseplatform.model.TipoRelacionUML;
import com.caseplatform.model.Usuario;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.AtributoUMLRepository;
import com.caseplatform.repository.ClaseUMLRepository;
import com.caseplatform.repository.MetodoUMLRepository;
import com.caseplatform.repository.ModeloUMLRepository;
import com.caseplatform.repository.ProyectoUMLRepository;
import com.caseplatform.repository.RelacionUMLRepository;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.service.impl.AtributoUMLServiceImpl;
import com.caseplatform.service.impl.ClaseUMLServiceImpl;
import com.caseplatform.service.impl.MetodoUMLServiceImpl;
import com.caseplatform.service.impl.ModeloUMLServiceImpl;
import com.caseplatform.service.impl.ProyectoUMLServiceImpl;
import com.caseplatform.service.impl.RelacionUMLServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UMLServiceTest {

    @Mock
    private ProyectoUMLRepository proyectoUMLRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ModeloUMLRepository modeloUMLRepository;
    @Mock
    private ClaseUMLRepository claseUMLRepository;
    @Mock
    private AtributoUMLRepository atributoUMLRepository;
    @Mock
    private MetodoUMLRepository metodoUMLRepository;
    @Mock
    private RelacionUMLRepository relacionUMLRepository;

    @InjectMocks
    private ProyectoUMLServiceImpl proyectoUMLService;
    @InjectMocks
    private ModeloUMLServiceImpl modeloUMLService;
    @InjectMocks
    private ClaseUMLServiceImpl claseUMLService;
    @InjectMocks
    private AtributoUMLServiceImpl atributoUMLService;
    @InjectMocks
    private MetodoUMLServiceImpl metodoUMLService;
    @InjectMocks
    private RelacionUMLServiceImpl relacionUMLService;

    private Usuario usuario;
    private ProyectoUML proyecto;
    private ModeloUML modelo;
    private ClaseUML claseCliente;
    private ClaseUML clasePedido;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .email("ingeniero@caseplatform.com")
                .nombreCompleto("Ingeniero de Software")
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        proyecto = ProyectoUML.builder()
                .id(10L)
                .nombre("Sistema E-Commerce")
                .descripcion("Plataforma de ventas online")
                .usuarioPropietario(usuario)
                .modelos(new ArrayList<>())
                .build();

        modelo = ModeloUML.builder()
                .id(100L)
                .nombre("Diagrama Conceptual Inicial")
                .version("1.0")
                .proyecto(proyecto)
                .clases(new ArrayList<>())
                .relaciones(new ArrayList<>())
                .build();

        claseCliente = ClaseUML.builder()
                .id(1001L)
                .nombre("Cliente")
                .visibilidad(VisibilidadUML.PUBLIC)
                .modeloUML(modelo)
                .atributos(new ArrayList<>())
                .metodos(new ArrayList<>())
                .build();

        clasePedido = ClaseUML.builder()
                .id(1002L)
                .nombre("Pedido")
                .visibilidad(VisibilidadUML.PUBLIC)
                .modeloUML(modelo)
                .atributos(new ArrayList<>())
                .metodos(new ArrayList<>())
                .build();
    }

    // -------------------------------------------------------------------------
    // PRUEBAS DE PROYECTO UML
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Proyecto: Crear proyecto exitoso inicializa modelo inicial")
    void testCrearProyecto_Exitoso() {
        ProyectoUMLDTO request = ProyectoUMLDTO.builder()
                .nombre("Sistema E-Commerce")
                .descripcion("Plataforma de ventas online")
                .build();

        when(usuarioRepository.findByEmailIgnoreCase("ingeniero@caseplatform.com")).thenReturn(Optional.of(usuario));
        when(proyectoUMLRepository.save(any(ProyectoUML.class))).thenReturn(proyecto);
        when(modeloUMLRepository.save(any(ModeloUML.class))).thenReturn(modelo);

        ProyectoUMLDTO resultado = proyectoUMLService.crearProyecto(request, "ingeniero@caseplatform.com");

        assertNotNull(resultado);
        assertEquals("Sistema E-Commerce", resultado.getNombre());
        verify(proyectoUMLRepository).save(any(ProyectoUML.class));
        verify(modeloUMLRepository).save(any(ModeloUML.class));
    }

    @Test
    @DisplayName("Proyecto: Nombre vacío lanza ValidationException")
    void testCrearProyecto_NombreVacio_LanzaExcepcion() {
        ProyectoUMLDTO request = ProyectoUMLDTO.builder().nombre("").build();

        assertThrows(ValidationException.class, () ->
                proyectoUMLService.crearProyecto(request, "ingeniero@caseplatform.com"));
    }

    // -------------------------------------------------------------------------
    // PRUEBAS DE CLASE UML
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Clase: Crear clase exitosa en el modelo")
    void testCrearClase_Exitoso() {
        ClaseUMLDTO request = ClaseUMLDTO.builder()
                .nombre("Producto")
                .visibilidad(VisibilidadUML.PUBLIC)
                .build();

        ClaseUML claseProducto = ClaseUML.builder()
                .id(1003L)
                .nombre("Producto")
                .visibilidad(VisibilidadUML.PUBLIC)
                .modeloUML(modelo)
                .build();

        when(claseUMLRepository.existsByModeloUMLIdAndNombreIgnoreCase(100L, "Producto")).thenReturn(false);
        when(modeloUMLRepository.findById(100L)).thenReturn(Optional.of(modelo));
        when(claseUMLRepository.save(any(ClaseUML.class))).thenReturn(claseProducto);

        ClaseUMLDTO resultado = claseUMLService.crearClase(100L, request);

        assertNotNull(resultado);
        assertEquals("Producto", resultado.getNombre());
        verify(claseUMLRepository).save(any(ClaseUML.class));
    }

    @Test
    @DisplayName("Clase: Rechaza clase duplicada en el mismo modelo")
    void testCrearClase_Duplicada_LanzaExcepcion() {
        ClaseUMLDTO request = ClaseUMLDTO.builder().nombre("Cliente").build();

        when(claseUMLRepository.existsByModeloUMLIdAndNombreIgnoreCase(100L, "Cliente")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                claseUMLService.crearClase(100L, request));

        assertNotNull(ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // PRUEBAS DE ATRIBUTOS Y MÉTODOS
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Atributo: Agregar atributo con tipo y visibilidad válidos")
    void testAgregarAtributo_Exitoso() {
        AtributoUMLDTO request = AtributoUMLDTO.builder()
                .nombre("email")
                .tipoDato("String")
                .visibilidad(VisibilidadUML.PRIVATE)
                .build();

        when(atributoUMLRepository.existsByClaseUMLIdAndNombreIgnoreCase(1001L, "email")).thenReturn(false);
        when(claseUMLRepository.findById(1001L)).thenReturn(Optional.of(claseCliente));
        when(atributoUMLRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AtributoUMLDTO resultado = atributoUMLService.agregarAtributo(1001L, request);

        assertNotNull(resultado);
        assertEquals("email", resultado.getNombre());
        assertEquals("String", resultado.getTipoDato());
        assertEquals(VisibilidadUML.PRIVATE, resultado.getVisibilidad());
    }

    @Test
    @DisplayName("Método: Agregar método con retorno y visibilidad")
    void testAgregarMetodo_Exitoso() {
        MetodoUMLDTO request = MetodoUMLDTO.builder()
                .nombre("calcularTotal")
                .tipoRetorno("Double")
                .visibilidad(VisibilidadUML.PUBLIC)
                .build();

        when(claseUMLRepository.findById(1002L)).thenReturn(Optional.of(clasePedido));
        when(metodoUMLRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MetodoUMLDTO resultado = metodoUMLService.agregarMetodo(1002L, request);

        assertNotNull(resultado);
        assertEquals("calcularTotal", resultado.getNombre());
        assertEquals("Double", resultado.getTipoRetorno());
    }

    // -------------------------------------------------------------------------
    // PRUEBAS DE RELACIONES UML Y CARDINALIDADES
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Relación: Crear relación válida de asociación con cardinalidades 1 y *")
    void testCrearRelacion_Exitoso() {
        RelacionUMLDTO request = RelacionUMLDTO.builder()
                .tipoRelacion(TipoRelacionUML.ASOCIACION)
                .claseOrigenId(1001L)
                .claseDestinoId(1002L)
                .cardinalidadOrigen("1")
                .cardinalidadDestino("*")
                .descripcion("Un Cliente realiza varios Pedidos")
                .build();

        RelacionUML relacionGuardada = RelacionUML.builder()
                .id(501L)
                .tipoRelacion(TipoRelacionUML.ASOCIACION)
                .claseOrigen(claseCliente)
                .claseDestino(clasePedido)
                .cardinalidadOrigen("1")
                .cardinalidadDestino("*")
                .descripcion("Un Cliente realiza varios Pedidos")
                .modeloUML(modelo)
                .build();

        when(claseUMLRepository.findById(1001L)).thenReturn(Optional.of(claseCliente));
        when(claseUMLRepository.findById(1002L)).thenReturn(Optional.of(clasePedido));
        when(relacionUMLRepository.save(any(RelacionUML.class))).thenReturn(relacionGuardada);

        RelacionUMLDTO resultado = relacionUMLService.crearRelacion(request);

        assertNotNull(resultado);
        assertEquals(TipoRelacionUML.ASOCIACION, resultado.getTipoRelacion());
        assertEquals("1", resultado.getCardinalidadOrigen());
        assertEquals("*", resultado.getCardinalidadDestino());
    }

    @Test
    @DisplayName("Relación: Rechaza herencia reflexiva sobre la misma clase")
    void testCrearRelacion_HerenciaSobreSiMisma_LanzaExcepcion() {
        RelacionUMLDTO request = RelacionUMLDTO.builder()
                .tipoRelacion(TipoRelacionUML.HERENCIA)
                .claseOrigenId(1001L)
                .claseDestinoId(1001L)
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () ->
                relacionUMLService.crearRelacion(request));

        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Relación: Rechaza cardinalidad inválida")
    void testCrearRelacion_CardinalidadInvalida_LanzaExcepcion() {
        RelacionUMLDTO request = RelacionUMLDTO.builder()
                .tipoRelacion(TipoRelacionUML.ASOCIACION)
                .claseOrigenId(1001L)
                .claseDestinoId(1002L)
                .cardinalidadOrigen("9..9")
                .cardinalidadDestino("1")
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () ->
                relacionUMLService.crearRelacion(request));

        assertNotNull(ex.getMessage());
    }
}
