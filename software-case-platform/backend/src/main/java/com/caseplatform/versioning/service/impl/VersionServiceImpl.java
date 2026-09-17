package com.caseplatform.versioning.service.impl;

import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.MetodoUMLDTO;
import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.dto.uml.RelacionUMLDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.AtributoUML;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.MetodoUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.RelacionUML;
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
import com.caseplatform.versioning.service.VersionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    private final VersionModeloRepository versionModeloRepository;
    private final HistorialCambioRepository historialCambioRepository;
    private final ModeloUMLRepository modeloUMLRepository;
    private final ClaseUMLRepository claseUMLRepository;
    private final AtributoUMLRepository atributoUMLRepository;
    private final MetodoUMLRepository metodoUMLRepository;
    private final RelacionUMLRepository relacionUMLRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public VersionDTO crearVersion(CreateVersionDTO dto, String usuarioEmail) {
        log.info("Creando snapshot y versión para modelo ID: {} por usuario: {}", dto.getModeloId(), usuarioEmail);

        if (dto.getModeloId() == null) {
            throw new ValidationException("El ID del modelo UML es obligatorio");
        }
        if (dto.getNombreVersion() == null || dto.getNombreVersion().isBlank()) {
            throw new ValidationException("El nombre de la versión es obligatorio");
        }

        ModeloUML modelo = modeloUMLRepository.findById(dto.getModeloId())
                .orElseThrow(() -> new ResourceNotFoundException("Modelo UML no encontrado con ID: " + dto.getModeloId()));

        Usuario usuario = null;
        if (usuarioEmail != null && !usuarioEmail.isBlank()) {
            usuario = usuarioRepository.findByEmailIgnoreCase(usuarioEmail.trim()).orElse(null);
        }

        // Determinar número de versión semántico o correlativo
        String numeroVersion = dto.getNumeroVersion();
        if (numeroVersion == null || numeroVersion.isBlank()) {
            long totalPrevias = versionModeloRepository.countByModeloUMLId(modelo.getId());
            numeroVersion = String.format("v%d.0", totalPrevias + 1);
        }

        // Generar snapshot serializado en JSON
        SnapshotModeloDTO snapshot = generarSnapshot(modelo.getId());
        String snapshotJson;
        try {
            snapshotJson = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar snapshot del modelo UML: {}", e.getMessage(), e);
            throw new ValidationException("Error al generar el snapshot estructurado del modelo UML");
        }

        VersionModelo version = VersionModelo.builder()
                .numeroVersion(numeroVersion.trim())
                .nombreVersion(dto.getNombreVersion().trim())
                .descripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null)
                .snapshotJson(snapshotJson)
                .modeloUML(modelo)
                .usuarioCreador(usuario)
                .fechaCreacion(LocalDateTime.now())
                .estado("ACTIVA")
                .build();

        VersionModelo guardada = versionModeloRepository.save(version);

        // Actualizar la versión actual del modelo
        modelo.setVersion(numeroVersion.trim());
        modeloUMLRepository.save(modelo);

        // Registrar evento en el historial
        registrarCambio(
                modelo.getId(),
                TipoOperacionHistorial.CREATE,
                "Versión de Modelo",
                guardada.getId().toString(),
                null,
                guardada.getNombreVersion() + " (" + guardada.getNumeroVersion() + ")",
                usuarioEmail
        );

        log.info("Versión creada exitosamente: {} ID: {}", guardada.getNombreVersion(), guardada.getId());
        return mapToDTO(guardada, snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VersionDTO> listarVersionesPorModelo(Long modeloId) {
        if (!modeloUMLRepository.existsById(modeloId)) {
            throw new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId);
        }
        return versionModeloRepository.findByModeloUMLIdOrderByFechaCreacionDesc(modeloId).stream()
                .map(v -> mapToDTO(v, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VersionDTO obtenerVersionPorId(Long versionId) {
        VersionModelo version = versionModeloRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Versión de modelo no encontrada con ID: " + versionId));

        SnapshotModeloDTO snapshot = null;
        if (version.getSnapshotJson() != null && !version.getSnapshotJson().isBlank()) {
            try {
                snapshot = objectMapper.readValue(version.getSnapshotJson(), SnapshotModeloDTO.class);
            } catch (JsonProcessingException e) {
                log.warn("No se pudo deserializar snapshot para versión ID {}: {}", versionId, e.getMessage());
            }
        }

        return mapToDTO(version, snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCambioDTO> obtenerHistorialPorModelo(Long modeloId) {
        if (!modeloUMLRepository.existsById(modeloId)) {
            throw new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId);
        }
        return historialCambioRepository.findByModeloUMLIdOrderByFechaCambioDesc(modeloId).stream()
                .map(this::mapHistorialToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ModeloUMLDTO restaurarVersion(Long versionId, RestoreVersionDTO dto, String usuarioEmail) {
        log.info("Iniciando restauración de versión ID: {} por usuario: {}", versionId, usuarioEmail);

        VersionModelo version = versionModeloRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Versión no encontrada con ID: " + versionId));

        ModeloUML modelo = version.getModeloUML();
        if (modelo == null) {
            throw new ResourceNotFoundException("El modelo asociado a la versión no existe");
        }

        // Deserializar y validar compatibilidad del snapshot
        SnapshotModeloDTO snapshot;
        try {
            snapshot = objectMapper.readValue(version.getSnapshotJson(), SnapshotModeloDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Fallo de integridad: Snapshot corrupto o incompatible en versión ID {}: {}", versionId, e.getMessage());
            throw new ValidationException("El snapshot de la versión está dañado o no es compatible con el esquema actual");
        }

        if (snapshot == null) {
            throw new ValidationException("El snapshot de la versión está vacío");
        }

        // 1. Eliminar relaciones actuales para evitar violaciones de clave foránea
        relacionUMLRepository.deleteAll(modelo.getRelaciones());
        modelo.getRelaciones().clear();

        // 2. Eliminar clases actuales (en cascada elimina atributos y métodos)
        claseUMLRepository.deleteAll(modelo.getClases());
        modelo.getClases().clear();

        // Limpiar contexto de persistencia para evitar conflictos de nombres únicos
        if (entityManager != null) {
            entityManager.flush();
        }

        // 3. Recrear clases, atributos y métodos desde el snapshot
        Map<String, ClaseUML> clasesPorNombre = new HashMap<>();
        Map<Long, ClaseUML> clasesPorIdOriginal = new HashMap<>();

        if (snapshot.getClases() != null) {
            for (SnapshotModeloDTO.SnapshotClaseDTO cDto : snapshot.getClases()) {
                VisibilidadUML visibilidad = VisibilidadUML.PUBLIC;
                if (cDto.getVisibilidad() != null) {
                    try {
                        visibilidad = VisibilidadUML.valueOf(cDto.getVisibilidad().toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                ClaseUML clase = ClaseUML.builder()
                        .nombre(cDto.getNombre())
                        .visibilidad(visibilidad)
                        .descripcion(cDto.getDescripcion())
                        .posicionX(cDto.getPosicionX() != null ? cDto.getPosicionX() : 100.0)
                        .posicionY(cDto.getPosicionY() != null ? cDto.getPosicionY() : 100.0)
                        .modeloUML(modelo)
                        .atributos(new ArrayList<>())
                        .metodos(new ArrayList<>())
                        .build();

                // Reconstruir atributos
                if (cDto.getAtributos() != null) {
                    for (SnapshotModeloDTO.SnapshotAtributoDTO aDto : cDto.getAtributos()) {
                        VisibilidadUML attrVis = VisibilidadUML.PRIVATE;
                        if (aDto.getVisibilidad() != null) {
                            try {
                                attrVis = VisibilidadUML.valueOf(aDto.getVisibilidad().toUpperCase());
                            } catch (IllegalArgumentException ignored) {
                            }
                        }

                        AtributoUML attr = AtributoUML.builder()
                                .nombre(aDto.getNombre())
                                .tipoDato(aDto.getTipoDato() != null ? aDto.getTipoDato() : "String")
                                .visibilidad(attrVis)
                                .valorInicial(aDto.getValorInicial())
                                .claseUML(clase)
                                .build();
                        clase.getAtributos().add(attr);
                    }
                }

                // Reconstruir métodos
                if (cDto.getMetodos() != null) {
                    for (SnapshotModeloDTO.SnapshotMetodoDTO mDto : cDto.getMetodos()) {
                        VisibilidadUML metVis = VisibilidadUML.PUBLIC;
                        if (mDto.getVisibilidad() != null) {
                            try {
                                metVis = VisibilidadUML.valueOf(mDto.getVisibilidad().toUpperCase());
                            } catch (IllegalArgumentException ignored) {
                            }
                        }

                        MetodoUML metodo = MetodoUML.builder()
                                .nombre(mDto.getNombre())
                                .tipoRetorno(mDto.getTipoRetorno() != null ? mDto.getTipoRetorno() : "void")
                                .visibilidad(metVis)
                                .parametros(mDto.getParametros())
                                .claseUML(clase)
                                .build();
                        clase.getMetodos().add(metodo);
                    }
                }

                ClaseUML guardada = claseUMLRepository.save(clase);
                clasesPorNombre.put(guardada.getNombre(), guardada);
                if (cDto.getId() != null) {
                    clasesPorIdOriginal.put(cDto.getId(), guardada);
                }
                modelo.getClases().add(guardada);
            }
        }

        if (entityManager != null) {
            entityManager.flush();
        }

        // 4. Reconstruir relaciones desde el snapshot
        if (snapshot.getRelaciones() != null) {
            for (SnapshotModeloDTO.SnapshotRelacionDTO rDto : snapshot.getRelaciones()) {
                ClaseUML origen = null;
                ClaseUML destino = null;

                if (rDto.getClaseOrigenNombre() != null) {
                    origen = clasesPorNombre.get(rDto.getClaseOrigenNombre());
                }
                if (origen == null && rDto.getClaseOrigenId() != null) {
                    origen = clasesPorIdOriginal.get(rDto.getClaseOrigenId());
                }

                if (rDto.getClaseDestinoNombre() != null) {
                    destino = clasesPorNombre.get(rDto.getClaseDestinoNombre());
                }
                if (destino == null && rDto.getClaseDestinoId() != null) {
                    destino = clasesPorIdOriginal.get(rDto.getClaseDestinoId());
                }

                if (origen != null && destino != null) {
                    TipoRelacionUML tipoRelacion = TipoRelacionUML.ASOCIACION;
                    if (rDto.getTipoRelacion() != null) {
                        try {
                            tipoRelacion = TipoRelacionUML.valueOf(rDto.getTipoRelacion().toUpperCase());
                        } catch (IllegalArgumentException ignored) {
                        }
                    }

                    RelacionUML relacion = RelacionUML.builder()
                            .tipoRelacion(tipoRelacion)
                            .claseOrigen(origen)
                            .claseDestino(destino)
                            .cardinalidadOrigen(rDto.getCardinalidadOrigen() != null ? rDto.getCardinalidadOrigen() : "1")
                            .cardinalidadDestino(rDto.getCardinalidadDestino() != null ? rDto.getCardinalidadDestino() : "1")
                            .descripcion(rDto.getDescripcion())
                            .modeloUML(modelo)
                            .build();

                    RelacionUML guardadaRelacion = relacionUMLRepository.save(relacion);
                    modelo.getRelaciones().add(guardadaRelacion);
                }
            }
        }

        // Actualizar la versión actual del modelo reflejando la restauración
        modelo.setVersion(version.getNumeroVersion());
        modelo.setFechaActualizacion(LocalDateTime.now());
        ModeloUML modeloActualizado = modeloUMLRepository.save(modelo);

        // Registrar en historial el evento de restauración
        String detalleComentario = (dto != null && dto.getComentario() != null && !dto.getComentario().isBlank())
                ? " Comentario: " + dto.getComentario().trim()
                : "";

        registrarCambio(
                modelo.getId(),
                TipoOperacionHistorial.RESTORE,
                "Modelo UML",
                modelo.getId().toString(),
                "Estado previo",
                "Restaurado a " + version.getNombreVersion() + " (" + version.getNumeroVersion() + ")." + detalleComentario,
                usuarioEmail
        );

        log.info("Modelo ID: {} restaurado con éxito a versión {}", modelo.getId(), version.getNumeroVersion());
        return mapModeloToDTO(modeloActualizado);
    }

    @Override
    @Transactional
    public void registrarCambio(
            Long modeloId,
            TipoOperacionHistorial tipo,
            String elementoModificado,
            String idElemento,
            String datosAnteriores,
            String datosNuevos,
            String usuarioEmail
    ) {
        try {
            ModeloUML modelo = modeloUMLRepository.findById(modeloId).orElse(null);
            if (modelo == null) {
                log.warn("No se pudo registrar cambio histórico: Modelo ID {} no existe", modeloId);
                return;
            }

            Usuario usuario = null;
            String emailFinal = (usuarioEmail != null && !usuarioEmail.isBlank())
                    ? usuarioEmail.trim()
                    : "sistema@caseplatform.com";

            if (usuarioEmail != null && !usuarioEmail.isBlank()) {
                usuario = usuarioRepository.findByEmailIgnoreCase(usuarioEmail.trim()).orElse(null);
            }

            HistorialCambio cambio = HistorialCambio.builder()
                    .tipoOperacion(tipo != null ? tipo : TipoOperacionHistorial.UPDATE)
                    .elementoModificado(elementoModificado != null ? elementoModificado : "Elemento UML")
                    .idElemento(idElemento)
                    .datosAnteriores(datosAnteriores)
                    .datosNuevos(datosNuevos)
                    .usuario(usuario)
                    .usuarioEmail(emailFinal)
                    .fechaCambio(LocalDateTime.now())
                    .modeloUML(modelo)
                    .build();

            historialCambioRepository.save(cambio);
            log.debug("Cambio histórico registrado: [{}] {} sobre modelo ID: {}", tipo, elementoModificado, modeloId);
        } catch (Exception e) {
            log.error("Error al registrar cambio en el historial: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SnapshotModeloDTO generarSnapshot(Long modeloId) {
        ModeloUML modelo = modeloUMLRepository.findById(modeloId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId));

        List<SnapshotModeloDTO.SnapshotClaseDTO> clasesSnapshot = new ArrayList<>();
        if (modelo.getClases() != null) {
            for (ClaseUML c : modelo.getClases()) {
                List<SnapshotModeloDTO.SnapshotAtributoDTO> attrs = new ArrayList<>();
                if (c.getAtributos() != null) {
                    for (AtributoUML a : c.getAtributos()) {
                        attrs.add(SnapshotModeloDTO.SnapshotAtributoDTO.builder()
                                .id(a.getId())
                                .nombre(a.getNombre())
                                .tipoDato(a.getTipoDato())
                                .visibilidad(a.getVisibilidad() != null ? a.getVisibilidad().name() : "PRIVATE")
                                .valorInicial(a.getValorInicial())
                                .build());
                    }
                }

                List<SnapshotModeloDTO.SnapshotMetodoDTO> mets = new ArrayList<>();
                if (c.getMetodos() != null) {
                    for (MetodoUML m : c.getMetodos()) {
                        mets.add(SnapshotModeloDTO.SnapshotMetodoDTO.builder()
                                .id(m.getId())
                                .nombre(m.getNombre())
                                .tipoRetorno(m.getTipoRetorno())
                                .visibilidad(m.getVisibilidad() != null ? m.getVisibilidad().name() : "PUBLIC")
                                .parametros(m.getParametros())
                                .build());
                    }
                }

                clasesSnapshot.add(SnapshotModeloDTO.SnapshotClaseDTO.builder()
                        .id(c.getId())
                        .nombre(c.getNombre())
                        .visibilidad(c.getVisibilidad() != null ? c.getVisibilidad().name() : "PUBLIC")
                        .descripcion(c.getDescripcion())
                        .posicionX(c.getPosicionX())
                        .posicionY(c.getPosicionY())
                        .atributos(attrs)
                        .metodos(mets)
                        .build());
            }
        }

        List<SnapshotModeloDTO.SnapshotRelacionDTO> relacionesSnapshot = new ArrayList<>();
        if (modelo.getRelaciones() != null) {
            for (RelacionUML r : modelo.getRelaciones()) {
                relacionesSnapshot.add(SnapshotModeloDTO.SnapshotRelacionDTO.builder()
                        .id(r.getId())
                        .tipoRelacion(r.getTipoRelacion() != null ? r.getTipoRelacion().name() : "ASOCIACION")
                        .claseOrigenId(r.getClaseOrigen() != null ? r.getClaseOrigen().getId() : null)
                        .claseOrigenNombre(r.getClaseOrigen() != null ? r.getClaseOrigen().getNombre() : null)
                        .claseDestinoId(r.getClaseDestino() != null ? r.getClaseDestino().getId() : null)
                        .claseDestinoNombre(r.getClaseDestino() != null ? r.getClaseDestino().getNombre() : null)
                        .cardinalidadOrigen(r.getCardinalidadOrigen())
                        .cardinalidadDestino(r.getCardinalidadDestino())
                        .descripcion(r.getDescripcion())
                        .build());
            }
        }

        return SnapshotModeloDTO.builder()
                .modeloId(modelo.getId())
                .nombreModelo(modelo.getNombre())
                .version(modelo.getVersion())
                .clases(clasesSnapshot)
                .relaciones(relacionesSnapshot)
                .build();
    }

    private VersionDTO mapToDTO(VersionModelo v, SnapshotModeloDTO snapshot) {
        return VersionDTO.builder()
                .id(v.getId())
                .numeroVersion(v.getNumeroVersion())
                .nombreVersion(v.getNombreVersion())
                .descripcion(v.getDescripcion())
                .modeloId(v.getModeloUML() != null ? v.getModeloUML().getId() : null)
                .modeloNombre(v.getModeloUML() != null ? v.getModeloUML().getNombre() : null)
                .usuarioCreadorId(v.getUsuarioCreador() != null ? v.getUsuarioCreador().getId() : null)
                .usuarioCreadorNombre(v.getUsuarioCreador() != null ? v.getUsuarioCreador().getNombreCompleto() : null)
                .usuarioCreadorEmail(v.getUsuarioCreador() != null ? v.getUsuarioCreador().getEmail() : null)
                .fechaCreacion(v.getFechaCreacion())
                .estado(v.getEstado())
                .snapshotJson(v.getSnapshotJson())
                .snapshot(snapshot)
                .build();
    }

    private HistorialCambioDTO mapHistorialToDTO(HistorialCambio h) {
        String autorNombre = (h.getUsuario() != null && h.getUsuario().getNombreCompleto() != null)
                ? h.getUsuario().getNombreCompleto()
                : (h.getUsuarioEmail() != null ? h.getUsuarioEmail().split("@")[0] : "Usuario");

        String descripcionResumen = generarDescripcionResumen(h, autorNombre);

        return HistorialCambioDTO.builder()
                .id(h.getId())
                .tipoOperacion(h.getTipoOperacion())
                .elementoModificado(h.getElementoModificado())
                .idElemento(h.getIdElemento())
                .datosAnteriores(h.getDatosAnteriores())
                .datosNuevos(h.getDatosNuevos())
                .usuarioId(h.getUsuario() != null ? h.getUsuario().getId() : null)
                .usuarioEmail(h.getUsuarioEmail())
                .usuarioNombre(autorNombre)
                .fechaCambio(h.getFechaCambio())
                .versionModeloId(h.getVersionModelo() != null ? h.getVersionModelo().getId() : null)
                .versionNumero(h.getVersionModelo() != null ? h.getVersionModelo().getNumeroVersion() : null)
                .modeloId(h.getModeloUML() != null ? h.getModeloUML().getId() : null)
                .descripcionResumen(descripcionResumen)
                .build();
    }

    private String generarDescripcionResumen(HistorialCambio h, String autor) {
        String elem = h.getElementoModificado() != null ? h.getElementoModificado() : "elemento";
        String detalle = h.getDatosNuevos() != null && !h.getDatosNuevos().isBlank()
                ? " " + h.getDatosNuevos()
                : (h.getDatosAnteriores() != null && !h.getDatosAnteriores().isBlank() ? " " + h.getDatosAnteriores() : "");

        switch (h.getTipoOperacion()) {
            case CREATE:
                return autor + " creó " + elem + detalle;
            case UPDATE:
                return autor + " modificó " + elem + detalle;
            case DELETE:
                return autor + " eliminó " + elem + detalle;
            case RESTORE:
                return autor + " restauró " + elem + detalle;
            default:
                return autor + " modificó " + elem;
        }
    }

    private ModeloUMLDTO mapModeloToDTO(ModeloUML m) {
        List<ClaseUMLDTO> clasesDTO = m.getClases() != null
                ? m.getClases().stream().map(this::mapClaseToDTO).collect(Collectors.toList())
                : new ArrayList<>();

        List<RelacionUMLDTO> relacionesDTO = m.getRelaciones() != null
                ? m.getRelaciones().stream().map(this::mapRelacionToDTO).collect(Collectors.toList())
                : new ArrayList<>();

        return ModeloUMLDTO.builder()
                .id(m.getId())
                .nombre(m.getNombre())
                .version(m.getVersion())
                .fechaCreacion(m.getFechaCreacion())
                .fechaActualizacion(m.getFechaActualizacion())
                .proyectoId(m.getProyecto() != null ? m.getProyecto().getId() : null)
                .clases(clasesDTO)
                .relaciones(relacionesDTO)
                .build();
    }

    private ClaseUMLDTO mapClaseToDTO(ClaseUML c) {
        List<AtributoUMLDTO> attrs = c.getAtributos() != null
                ? c.getAtributos().stream().map(a -> AtributoUMLDTO.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .tipoDato(a.getTipoDato())
                .visibilidad(a.getVisibilidad())
                .valorInicial(a.getValorInicial())
                .claseId(c.getId())
                .build()).collect(Collectors.toList())
                : new ArrayList<>();

        List<MetodoUMLDTO> mets = c.getMetodos() != null
                ? c.getMetodos().stream().map(met -> MetodoUMLDTO.builder()
                .id(met.getId())
                .nombre(met.getNombre())
                .tipoRetorno(met.getTipoRetorno())
                .visibilidad(met.getVisibilidad())
                .parametros(met.getParametros())
                .claseId(c.getId())
                .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return ClaseUMLDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .visibilidad(c.getVisibilidad())
                .descripcion(c.getDescripcion())
                .posicionX(c.getPosicionX())
                .posicionY(c.getPosicionY())
                .modeloId(c.getModeloUML() != null ? c.getModeloUML().getId() : null)
                .atributos(attrs)
                .metodos(mets)
                .build();
    }

    private RelacionUMLDTO mapRelacionToDTO(RelacionUML r) {
        return RelacionUMLDTO.builder()
                .id(r.getId())
                .tipoRelacion(r.getTipoRelacion())
                .claseOrigenId(r.getClaseOrigen() != null ? r.getClaseOrigen().getId() : null)
                .claseOrigenNombre(r.getClaseOrigen() != null ? r.getClaseOrigen().getNombre() : null)
                .claseDestinoId(r.getClaseDestino() != null ? r.getClaseDestino().getId() : null)
                .claseDestinoNombre(r.getClaseDestino() != null ? r.getClaseDestino().getNombre() : null)
                .cardinalidadOrigen(r.getCardinalidadOrigen())
                .cardinalidadDestino(r.getCardinalidadDestino())
                .descripcion(r.getDescripcion())
                .modeloId(r.getModeloUML() != null ? r.getModeloUML().getId() : null)
                .build();
    }
}
