package com.caseplatform;

import com.caseplatform.dto.uml.ModeloUMLDTO;
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
import com.caseplatform.repository.RelacionUMLRepository;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.versioning.dto.CreateVersionDTO;
import com.caseplatform.versioning.dto.HistorialCambioDTO;
import com.caseplatform.versioning.dto.RestoreVersionDTO;
import com.caseplatform.versioning.dto.SnapshotModeloDTO;
import com.caseplatform.versioning.dto.VersionDTO;
import com.caseplatform.versioning.model.HistorialCambio;
import com.caseplatform.versioning.model.TipoOperacionHistorial;
import com.caseplatform.versioning.model.VersionModelo;
import com.caseplatform.versioning.repository.HistorialCambioRepository;
import com.caseplatform.versioning.repository.VersionModeloRepository;
import com.caseplatform.versioning.service.impl.VersionServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @Mock
    private VersionModeloRepository versionModeloRepository;

    @Mock
    private HistorialCambioRepository historialCambioRepository;

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

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EntityManager entityManager;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private VersionServiceImpl versionService;

    private Usuario usuario;
    private ProyectoUML proyecto;
    private ModeloUML modelo;
    private ClaseUML claseCliente;
    private ClaseUML clasePedido;
    private RelacionUML relacion;
    private VersionModelo version1;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .email("juan@caseplatform.com")
                .nombreCompleto("Juan Desarrollador")
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        proyecto = ProyectoUML.builder()
                .id(10L)
                .nombre("Sistema Ventas")
                .usuarioPropietario(usuario)
                .build();

        modelo = ModeloUML.builder()
                .id(100L)
                .nombre("Modelo Sistema Ventas")
                .version("1.0")
                .proyecto(proyecto)
                .clases(new ArrayList<>())
                .relaciones(new ArrayList<>())
                .build();

        claseCliente = ClaseUML.builder()
                .id(1001L)
                .nombre("Cliente")
                .visibilidad(VisibilidadUML.PUBLIC)
                .posicionX(120.0)
                .posicionY(150.0)
                .modeloUML(modelo)
                .atributos(new ArrayList<>())
                .metodos(new ArrayList<>())
                .build();

        clasePedido = ClaseUML.builder()
                .id(1002L)
                .nombre("Pedido")
                .visibilidad(VisibilidadUML.PUBLIC)
                .posicionX(400.0)
                .posicionY(150.0)
                .modeloUML(modelo)
                .atributos(new ArrayList<>())
                .metodos(new ArrayList<>())
                .build();

        relacion = RelacionUML.builder()
                .id(501L)
                .tipoRelacion(TipoRelacionUML.ASOCIACION)
                .claseOrigen(claseCliente)
                .claseDestino(clasePedido)
                .cardinalidadOrigen("1")
                .cardinalidadDestino("*")
                .descripcion("Un Cliente tiene varios Pedidos")
                .modeloUML(modelo)
                .build();

        modelo.getClases().add(claseCliente);
        modelo.getClases().add(clasePedido);
        modelo.getRelaciones().add(relacion);

        // Snapshot serializado para version1
        SnapshotModeloDTO snap = SnapshotModeloDTO.builder()
                .modeloId(100L)
                .nombreModelo("Modelo Sistema Ventas")
                .version("v1.0")
                .clases(List.of(
                        SnapshotModeloDTO.SnapshotClaseDTO.builder()
                                .id(1001L)
                                .nombre("Cliente")
                                .visibilidad("PUBLIC")
                                .posicionX(120.0)
                                .posicionY(150.0)
                                .atributos(new ArrayList<>())
                                .metodos(new ArrayList<>())
                                .build(),
                        SnapshotModeloDTO.SnapshotClaseDTO.builder()
                                .id(1002L)
                                .nombre("Pedido")
                                .visibilidad("PUBLIC")
                                .posicionX(400.0)
                                .posicionY(150.0)
                                .atributos(new ArrayList<>())
                                .metodos(new ArrayList<>())
                                .build()
                ))
                .relaciones(List.of(
                        SnapshotModeloDTO.SnapshotRelacionDTO.builder()
                                .id(501L)
                                .tipoRelacion("ASOCIACION")
                                .claseOrigenNombre("Cliente")
                                .claseDestinoNombre("Pedido")
                                .cardinalidadOrigen("1")
                                .cardinalidadDestino("*")
                                .descripcion("Un Cliente tiene varios Pedidos")
                                .build()
                ))
                .build();

        String snapJson = "";
        try {
            snapJson = objectMapper.writeValueAsString(snap);
        } catch (Exception ignored) {
        }

        version1 = VersionModelo.builder()
                .id(1L)
                .numeroVersion("v1.0")
                .nombreVersion("Versión 1.0 Inicial")
                .descripcion("Primer snapshot estable")
                .snapshotJson(snapJson)
                .modeloUML(modelo)
                .usuarioCreador(usuario)
                .fechaCreacion(LocalDateTime.now())
                .estado("ACTIVA")
                .build();
    }

    // -------------------------------------------------------------------------
    // 1. CREACIÓN DE VERSIÓN Y ALMACENAMIENTO DE SNAPSHOT
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Creación de Versión: Se guarda correctamente con snapshot estructurado")
    void testCrearVersion_Exitoso() {
        CreateVersionDTO request = CreateVersionDTO.builder()
                .modeloId(100L)
                .nombreVersion("Versión 1.0")
                .descripcion("Modelo inicial")
                .build();

        when(modeloUMLRepository.findById(100L)).thenReturn(Optional.of(modelo));
        when(usuarioRepository.findByEmailIgnoreCase("juan@caseplatform.com")).thenReturn(Optional.of(usuario));
        when(versionModeloRepository.countByModeloUMLId(100L)).thenReturn(0L);
        when(versionModeloRepository.save(any(VersionModelo.class))).thenReturn(version1);
        when(modeloUMLRepository.save(any(ModeloUML.class))).thenReturn(modelo);

        VersionDTO resultado = versionService.crearVersion(request, "juan@caseplatform.com");

        assertNotNull(resultado);
        assertEquals("v1.0", resultado.getNumeroVersion());
        assertEquals("Versión 1.0 Inicial", resultado.getNombreVersion());
        assertEquals("ACTIVA", resultado.getEstado());
        assertEquals("juan@caseplatform.com", resultado.getUsuarioCreadorEmail());

        verify(versionModeloRepository).save(any(VersionModelo.class));
        verify(historialCambioRepository).save(any(HistorialCambio.class));
    }

    @Test
    @DisplayName("Creación de Versión: Rechaza solicitud sin nombre de versión")
    void testCrearVersion_SinNombre_LanzaExcepcion() {
        CreateVersionDTO request = CreateVersionDTO.builder()
                .modeloId(100L)
                .nombreVersion("")
                .build();

        assertThrows(ValidationException.class, () ->
                versionService.crearVersion(request, "juan@caseplatform.com"));
    }

    // -------------------------------------------------------------------------
    // 2. CONSULTA DE VERSIONES Y DETALLE
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Listado de Versiones: Consulta cronológica descendente")
    void testListarVersionesPorModelo_Exitoso() {
        when(modeloUMLRepository.existsById(100L)).thenReturn(true);
        when(versionModeloRepository.findByModeloUMLIdOrderByFechaCreacionDesc(100L)).thenReturn(List.of(version1));

        List<VersionDTO> versiones = versionService.listarVersionesPorModelo(100L);

        assertNotNull(versiones);
        assertEquals(1, versiones.size());
        assertEquals("v1.0", versiones.get(0).getNumeroVersion());
    }

    @Test
    @DisplayName("Detalle de Versión: Recupera versión y deserializa snapshot")
    void testObtenerVersionPorId_Exitoso() {
        when(versionModeloRepository.findById(1L)).thenReturn(Optional.of(version1));

        VersionDTO resultado = versionService.obtenerVersionPorId(1L);

        assertNotNull(resultado);
        assertNotNull(resultado.getSnapshot());
        assertEquals(2, resultado.getSnapshot().getClases().size());
        assertEquals(1, resultado.getSnapshot().getRelaciones().size());
    }

    // -------------------------------------------------------------------------
    // 3. REGISTRO Y CONSULTA DE HISTORIAL DE CAMBIOS
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Historial: Registra usuario, fecha y operación con descripción comprensible")
    void testObtenerHistorialPorModelo_Exitoso() {
        HistorialCambio h1 = HistorialCambio.builder()
                .id(101L)
                .tipoOperacion(TipoOperacionHistorial.CREATE)
                .elementoModificado("Clase UML")
                .idElemento("1001")
                .datosNuevos("Cliente")
                .usuario(usuario)
                .usuarioEmail("juan@caseplatform.com")
                .fechaCambio(LocalDateTime.now())
                .modeloUML(modelo)
                .build();

        HistorialCambio h2 = HistorialCambio.builder()
                .id(102L)
                .tipoOperacion(TipoOperacionHistorial.DELETE)
                .elementoModificado("Atributo")
                .idElemento("2001")
                .datosAnteriores("precio")
                .usuario(usuario)
                .usuarioEmail("juan@caseplatform.com")
                .fechaCambio(LocalDateTime.now())
                .modeloUML(modelo)
                .build();

        when(modeloUMLRepository.existsById(100L)).thenReturn(true);
        when(historialCambioRepository.findByModeloUMLIdOrderByFechaCambioDesc(100L)).thenReturn(List.of(h1, h2));

        List<HistorialCambioDTO> resultado = versionService.obtenerHistorialPorModelo(100L);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        HistorialCambioDTO dto1 = resultado.get(0);
        assertEquals(TipoOperacionHistorial.CREATE, dto1.getTipoOperacion());
        assertEquals("Clase UML", dto1.getElementoModificado());
        assertEquals("juan@caseplatform.com", dto1.getUsuarioEmail());
        assertNotNull(dto1.getFechaCambio());
        assertTrue(dto1.getDescripcionResumen().contains("creó Clase UML"));

        HistorialCambioDTO dto2 = resultado.get(1);
        assertEquals(TipoOperacionHistorial.DELETE, dto2.getTipoOperacion());
        assertTrue(dto2.getDescripcionResumen().contains("eliminó Atributo"));
    }

    // -------------------------------------------------------------------------
    // 4. RESTAURACIÓN DE VERSIÓN Y RECUPERACIÓN DEL MODELO
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Restauración: Modelo vuelve al estado anterior manteniendo integridad UML")
    void testRestaurarVersion_Exitoso() {
        RestoreVersionDTO request = RestoreVersionDTO.builder()
                .comentario("Regresando a versión estable tras prueba")
                .build();

        when(versionModeloRepository.findById(1L)).thenReturn(Optional.of(version1));
        when(claseUMLRepository.save(any(ClaseUML.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(relacionUMLRepository.save(any(RelacionUML.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modeloUMLRepository.save(any(ModeloUML.class))).thenReturn(modelo);
        when(modeloUMLRepository.findById(100L)).thenReturn(Optional.of(modelo));

        ModeloUMLDTO modeloRestaurado = versionService.restaurarVersion(1L, request, "juan@caseplatform.com");

        assertNotNull(modeloRestaurado);
        assertEquals("v1.0", modeloRestaurado.getVersion());
        verify(relacionUMLRepository).deleteAll(any());
        verify(claseUMLRepository).deleteAll(any());
        verify(historialCambioRepository).save(any(HistorialCambio.class));
    }

    @Test
    @DisplayName("Restauración: Rechaza restauración si el snapshot está corrupto o es inválido")
    void testRestaurarVersion_SnapshotCorrupto_LanzaExcepcion() {
        VersionModelo versionDañada = VersionModelo.builder()
                .id(2L)
                .numeroVersion("v2.0")
                .nombreVersion("Corrupta")
                .snapshotJson("{ json_invalido_sin_cerrar: ")
                .modeloUML(modelo)
                .build();

        when(versionModeloRepository.findById(2L)).thenReturn(Optional.of(versionDañada));

        assertThrows(ValidationException.class, () ->
                versionService.restaurarVersion(2L, new RestoreVersionDTO(), "juan@caseplatform.com"));
    }
}
