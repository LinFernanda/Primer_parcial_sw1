package com.caseplatform.ai.service.impl;

import com.caseplatform.ai.command.AICommandRequest;
import com.caseplatform.ai.command.AICommandResponse;
import com.caseplatform.ai.command.ParsedAIAction;
import com.caseplatform.ai.command.VoiceCommandRequest;
import com.caseplatform.ai.model.AICommandHistory;
import com.caseplatform.ai.model.TipoOperacionAI;
import com.caseplatform.ai.parser.AICommandParser;
import com.caseplatform.ai.repository.AICommandHistoryRepository;
import com.caseplatform.ai.service.AIAgentService;
import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.dto.uml.RelacionUMLDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.AtributoUML;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.RelacionUML;
import com.caseplatform.model.TipoRelacionUML;
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
import com.caseplatform.versioning.model.TipoOperacionHistorial;
import com.caseplatform.versioning.service.VersionService;
import com.caseplatform.websocket.model.TipoElementoUML;
import com.caseplatform.websocket.model.TipoOperacionUML;
import com.caseplatform.websocket.model.UMLEvent;
import com.caseplatform.websocket.service.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAgentServiceImpl implements AIAgentService {

    private final AICommandParser aiCommandParser;
    private final AICommandHistoryRepository aiCommandHistoryRepository;
    private final ModeloUMLService modeloUMLService;
    private final ClaseUMLService claseUMLService;
    private final AtributoUMLService atributoUMLService;
    private final RelacionUMLService relacionUMLService;
    private final ModeloUMLRepository modeloUMLRepository;
    private final ClaseUMLRepository claseUMLRepository;
    private final AtributoUMLRepository atributoUMLRepository;
    private final RelacionUMLRepository relacionUMLRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private EventPublisher eventPublisher;

    @Autowired(required = false)
    private VersionService versionService;

    @Autowired(required = false)
    private com.caseplatform.generator.service.BackendGeneratorService backendGeneratorService;

    @Autowired(required = false)
    private com.caseplatform.integration.enterprisearchitect.service.EnterpriseArchitectExportService eaExportService;

    @Override
    @Transactional
    public AICommandResponse procesarComando(Long modeloId, AICommandRequest request, String usuarioEmail) {
        log.info("Agente IA procesando instrucción para modelo ID {}: '{}' de usuario: {}",
                modeloId, request.getPrompt(), usuarioEmail);

        ModeloUML modelo = modeloUMLRepository.findById(modeloId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId));

        Usuario usuario = null;
        if (usuarioEmail != null && !usuarioEmail.isBlank()) {
            usuario = usuarioRepository.findByEmailIgnoreCase(usuarioEmail.trim()).orElse(null);
        }

        // 1. Interpretar o recibir acción confirmada
        ParsedAIAction accion;
        if (Boolean.TRUE.equals(request.getConfirmado()) && request.getAccionConfirmada() != null) {
            accion = request.getAccionConfirmada();
            if (accion.getTipoOperacion() == TipoOperacionAI.CONFIRMATION_REQUIRED) {
                accion.setTipoOperacion(TipoOperacionAI.CREATE_CLASS);
            }
        } else {
            accion = aiCommandParser.parse(request.getPrompt());
        }

        // 2. Si la acción requiere confirmación previa por ambigüedad
        if (accion.isRequiereConfirmacion() && !Boolean.TRUE.equals(request.getConfirmado())) {
            registrarHistorialComando(modelo, usuario, usuarioEmail, request.getPrompt(), accion,
                    accion.getPreguntaConfirmacion(), true, true);

            return AICommandResponse.builder()
                    .mensaje(accion.getPreguntaConfirmacion())
                    .exitoso(true)
                    .requiereConfirmacion(true)
                    .preguntaConfirmacion(accion.getPreguntaConfirmacion())
                    .accion(accion)
                    .fecha(LocalDateTime.now())
                    .build();
        }

        // 3. Ejecución de la acción interpretada sobre el modelo UML
        String mensajeRespuesta;
        boolean exitoso = true;

        try {
            switch (accion.getTipoOperacion()) {
                case CREATE_CLASS:
                    mensajeRespuesta = ejecutarCrearClase(modelo, accion, usuarioEmail);
                    break;
                case UPDATE_CLASS:
                    mensajeRespuesta = ejecutarModificarClase(modelo, accion, usuarioEmail);
                    break;
                case DELETE_CLASS:
                    mensajeRespuesta = ejecutarEliminarClase(modelo, accion, usuarioEmail);
                    break;
                case CREATE_ATTRIBUTE:
                    mensajeRespuesta = ejecutarAgregarAtributo(modelo, accion, usuarioEmail);
                    break;
                case UPDATE_ATTRIBUTE:
                    mensajeRespuesta = ejecutarModificarAtributo(modelo, accion, usuarioEmail);
                    break;
                case DELETE_ATTRIBUTE:
                    mensajeRespuesta = ejecutarEliminarAtributo(modelo, accion, usuarioEmail);
                    break;
                case CREATE_RELATION:
                    mensajeRespuesta = ejecutarCrearRelacion(modelo, accion, usuarioEmail);
                    break;
                case UPDATE_RELATION:
                    mensajeRespuesta = ejecutarModificarRelacion(modelo, accion, usuarioEmail);
                    break;
                case DELETE_RELATION:
                    mensajeRespuesta = ejecutarEliminarRelacion(modelo, accion, usuarioEmail);
                    break;
                case GENERATE_BACKEND:
                    mensajeRespuesta = ejecutarGenerarBackend(modelo, usuarioEmail);
                    break;
                case EXPORT_ENTERPRISE_ARCHITECT:
                    mensajeRespuesta = ejecutarExportarEA(modelo, usuarioEmail);
                    break;
                case IMPORT_ENTERPRISE_ARCHITECT:
                    mensajeRespuesta = "Para importar modelos desde Enterprise Architect, utiliza el botón 'Enterprise Architect' en la barra de herramientas o realiza un POST a /api/integration/ea/models/" + modelo.getId() + "/import con tu archivo XMI.";
                    break;
                default:
                    exitoso = false;
                    mensajeRespuesta = accion.getExplicacion() != null
                            ? accion.getExplicacion()
                            : "No se reconoció una instrucción válida de edición UML.";
                    break;
            }
        } catch (Exception e) {
            log.error("Error al ejecutar comando IA sobre modelo {}: {}", modeloId, e.getMessage(), e);
            exitoso = false;
            mensajeRespuesta = "Error al ejecutar la acción: " + e.getMessage();
        }

        // Registrar en historial de comandos IA
        registrarHistorialComando(modelo, usuario, usuarioEmail, request.getPrompt(), accion, mensajeRespuesta, exitoso, false);

        // Obtener el estado actualizado del modelo UML
        ModeloUMLDTO modeloActualizado = modeloUMLService.obtenerPorId(modelo.getId());
        if (modeloActualizado != null) {
            modeloActualizado.setClases(claseUMLService.listarPorModelo(modelo.getId()));
            modeloActualizado.setRelaciones(relacionUMLService.listarPorModelo(modelo.getId()));
        }

        return AICommandResponse.builder()
                .mensaje(mensajeRespuesta)
                .exitoso(exitoso)
                .requiereConfirmacion(false)
                .accion(accion)
                .modeloActualizado(modeloActualizado)
                .fecha(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public AICommandResponse procesarVoz(Long modeloId, VoiceCommandRequest request, String usuarioEmail) {
        String texto = request.getTextoTranscrito();

        // Si no vino texto transcrito pero sí vino audioBase64, transcribir con Groq Whisper
        if ((texto == null || texto.isBlank()) && request.getAudioBase64() != null && !request.getAudioBase64().isBlank()) {
            log.info("Comando de voz recibido en audioBase64, transcribiendo con Groq Whisper...");
            texto = aiCommandParser.transcribeAudioBase64(request.getAudioBase64());
        }

        if (texto == null || texto.isBlank()) {
            throw new ValidationException("No se pudo reconocer audio ni texto válido en el comando de voz.");
        }

        log.info("Comando de voz procesado como texto: '{}'", texto);
        AICommandRequest cmdRequest = AICommandRequest.builder()
                .prompt(texto.trim())
                .build();
        AICommandResponse response = procesarComando(modeloId, cmdRequest, usuarioEmail);
        if (response != null && response.getMensaje() != null) {
            response.setMensaje("🎙️ Voz reconocida: \"" + texto + "\"\n" + response.getMensaje());
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AICommandHistory> obtenerHistorialComandos(Long modeloId) {
        if (!modeloUMLRepository.existsById(modeloId)) {
            throw new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId);
        }
        return aiCommandHistoryRepository.findByModeloUMLIdOrderByFechaDesc(modeloId);
    }

    // =========================================================================
    // EJECUTORES ATÓMICOS DE OPERACIONES UML CON VALIDACIÓN Y TRAZABILIDAD
    // =========================================================================

    private String ejecutarCrearClase(ModeloUML modelo, ParsedAIAction accion, String usuarioEmail) {
        String nombre = accion.getNombreClase();
        if (nombre == null || nombre.isBlank()) {
            throw new ValidationException("El nombre de la clase a crear es obligatorio.");
        }

        // Posicionamiento inteligente distribuido en la pizarra
        int totalClases = modelo.getClases() != null ? modelo.getClases().size() : 0;
        double posX = 100.0 + (totalClases % 4) * 240.0;
        double posY = 100.0 + (totalClases / 4) * 200.0;

        VisibilidadUML vis = VisibilidadUML.PUBLIC;
        if (accion.getVisibilidad() != null) {
            try {
                vis = VisibilidadUML.valueOf(accion.getVisibilidad().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        ClaseUMLDTO nuevaClase = claseUMLService.crearClase(modelo.getId(), ClaseUMLDTO.builder()
                .nombre(nombre)
                .visibilidad(vis)
                .descripcion(accion.getDescripcion())
                .posicionX(posX)
                .posicionY(posY)
                .build());

        // Agregar atributos si venían en la instrucción compuesta
        if (accion.getAtributos() != null && !accion.getAtributos().isEmpty()) {
            for (ParsedAIAction.AtributoSimple attr : accion.getAtributos()) {
                VisibilidadUML attrVis = VisibilidadUML.PRIVATE;
                if (attr.getVisibilidad() != null) {
                    try {
                        attrVis = VisibilidadUML.valueOf(attr.getVisibilidad().toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                atributoUMLService.agregarAtributo(nuevaClase.getId(), AtributoUMLDTO.builder()
                        .nombre(attr.getNombre())
                        .tipoDato(attr.getTipo())
                        .visibilidad(attrVis)
                        .build());
            }
        }

        // Difusión en tiempo real por WebSocket colaborativo
        difundirEvento(modelo.getId(), TipoOperacionUML.CREATE, TipoElementoUML.CLASE,
                nuevaClase.getId().toString(), Map.of("clase", nuevaClase), usuarioEmail);

        // Registro en historial de trazabilidad (Fase 6)
        if (versionService != null) {
            versionService.registrarCambio(modelo.getId(), TipoOperacionHistorial.CREATE,
                    "Clase UML (IA)", nuevaClase.getId().toString(), null, nuevaClase.getNombre(), usuarioEmail);
        }

        return "Clase " + nombre + " creada correctamente";
    }

    private String ejecutarModificarClase(ModeloUML modelo, ParsedAIAction accion, String usuarioEmail) {
        String nombreActual = accion.getNombreClase();
        String nuevoNombre = accion.getNuevoNombreClase();
        if (nombreActual == null || nuevoNombre == null) {
            throw new ValidationException("Debe especificar el nombre actual y el nuevo nombre de la clase.");
        }

        ClaseUML clase = claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(modelo.getId(), nombreActual)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la clase '" + nombreActual + "' en este modelo."));

        ClaseUMLDTO actualizada = claseUMLService.actualizar(clase.getId(), ClaseUMLDTO.builder()
                .nombre(nuevoNombre)
                .build());

        difundirEvento(modelo.getId(), TipoOperacionUML.UPDATE, TipoElementoUML.CLASE,
                clase.getId().toString(), Map.of("clase", actualizada), usuarioEmail);

        if (versionService != null) {
            versionService.registrarCambio(modelo.getId(), TipoOperacionHistorial.UPDATE,
                    "Clase UML (IA)", clase.getId().toString(), nombreActual, nuevoNombre, usuarioEmail);
        }

        return "Clase " + nombreActual + " modificada a " + nuevoNombre + " correctamente";
    }

    private String ejecutarEliminarClase(ModeloUML modelo, ParsedAIAction accion, String usuarioEmail) {
        String nombre = accion.getNombreClase();
        if (nombre == null || nombre.isBlank()) {
            throw new ValidationException("Debe especificar el nombre de la clase a eliminar.");
        }

        ClaseUML clase = claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(modelo.getId(), nombre)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la clase '" + nombre + "' en este modelo."));

        claseUMLService.eliminar(clase.getId());

        difundirEvento(modelo.getId(), TipoOperacionUML.DELETE, TipoElementoUML.CLASE,
                clase.getId().toString(), Map.of("nombre", nombre), usuarioEmail);

        if (versionService != null) {
            versionService.registrarCambio(modelo.getId(), TipoOperacionHistorial.DELETE,
                    "Clase UML (IA)", clase.getId().toString(), nombre, null, usuarioEmail);
        }

        return "Clase " + nombre + " eliminada correctamente";
    }

    private String ejecutarAgregarAtributo(ModeloUML modelo, ParsedAIAction accion, String usuarioEmail) {
        String nombreClase = accion.getNombreClase();
        String nombreAttr = accion.getNombreAtributo();
        String tipoDato = accion.getTipoDatoAtributo() != null ? accion.getTipoDatoAtributo() : "String";

        if (nombreClase == null || nombreAttr == null) {
            throw new ValidationException("Debe especificar el atributo y la clase de destino.");
        }

        ClaseUML clase = claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(modelo.getId(), nombreClase)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la clase '" + nombreClase + "' en este modelo."));

        VisibilidadUML vis = VisibilidadUML.PRIVATE;
        if (accion.getVisibilidad() != null) {
            try {
                vis = VisibilidadUML.valueOf(accion.getVisibilidad().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        AtributoUMLDTO nuevo = atributoUMLService.agregarAtributo(clase.getId(), AtributoUMLDTO.builder()
                .nombre(nombreAttr)
                .tipoDato(tipoDato)
                .visibilidad(vis)
                .build());

        difundirEvento(modelo.getId(), TipoOperacionUML.UPDATE, TipoElementoUML.CLASE,
                clase.getId().toString(), Map.of("atributo", nuevo), usuarioEmail);

        if (versionService != null) {
            versionService.registrarCambio(modelo.getId(), TipoOperacionHistorial.CREATE,
                    "Atributo (IA)", nuevo.getId() != null ? nuevo.getId().toString() : null,
                    null, nombreClase + "." + nombreAttr + ":" + tipoDato, usuarioEmail);
        }

        return "Atributo " + nombreAttr + " (" + tipoDato + ") agregado a " + nombreClase + " correctamente";
    }

    private String ejecutarModificarAtributo(ModeloUML modelo, ParsedAIAction accion, String usuarioEmail) {
        String nombreClase = accion.getNombreClase();
        String nombreAttr = accion.getNombreAtributo();
        String nuevoNombre = accion.getNuevoNombreAtributo();

        if (nombreAttr == null || nuevoNombre == null) {
            throw new ValidationException("Debe indicar el atributo actual y su nuevo nombre.");
        }

        // Buscar atributo en la clase especificada o globalmente en el modelo
        AtributoUML atributo = null;
        if (nombreClase != null) {
            ClaseUML clase = claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(modelo.getId(), nombreClase)
                    .orElseThrow(() -> new ResourceNotFoundException("Clase '" + nombreClase + "' no encontrada."));
            for (AtributoUML a : clase.getAtributos()) {
                if (a.getNombre().equalsIgnoreCase(nombreAttr)) {
                    atributo = a;
                    break;
                }
            }
        } else {
            for (ClaseUML c : modelo.getClases()) {
                for (AtributoUML a : c.getAtributos()) {
                    if (a.getNombre().equalsIgnoreCase(nombreAttr)) {
                        atributo = a;
                        break;
                    }
                }
                if (atributo != null) break;
            }
        }

        if (atributo == null) {
            throw new ResourceNotFoundException("Atributo '" + nombreAttr + "' no encontrado en el modelo.");
        }

        atributo.setNombre(nuevoNombre);
        if (accion.getTipoDatoAtributo() != null && !accion.getTipoDatoAtributo().isBlank()) {
            atributo.setTipoDato(accion.getTipoDatoAtributo());
        }
        atributoUMLRepository.save(atributo);

        if (versionService != null) {
            versionService.registrarCambio(modelo.getId(), TipoOperacionHistorial.UPDATE,
                    "Atributo (IA)", atributo.getId().toString(), nombreAttr, nuevoNombre, usuarioEmail);
        }

        return "Atributo " + nombreAttr + " modificado a " + nuevoNombre + " correctamente";
    }

    private String ejecutarEliminarAtributo(ModeloUML modelo, ParsedAIAction accion, String usuarioEmail) {
        String nombreClase = accion.getNombreClase();
        String nombreAttr = accion.getNombreAtributo();

        if (nombreAttr == null) {
            throw new ValidationException("Debe indicar el nombre del atributo a eliminar.");
        }

        AtributoUML atributo = null;
        if (nombreClase != null) {
            ClaseUML clase = claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(modelo.getId(), nombreClase)
                    .orElseThrow(() -> new ResourceNotFoundException("Clase '" + nombreClase + "' no encontrada."));
            for (AtributoUML a : clase.getAtributos()) {
                if (a.getNombre().equalsIgnoreCase(nombreAttr)) {
                    atributo = a;
                    break;
                }
            }
        } else {
            for (ClaseUML c : modelo.getClases()) {
                for (AtributoUML a : c.getAtributos()) {
                    if (a.getNombre().equalsIgnoreCase(nombreAttr)) {
                        atributo = a;
                        break;
                    }
                }
                if (atributo != null) break;
            }
        }

        if (atributo == null) {
            throw new ResourceNotFoundException("Atributo '" + nombreAttr + "' no encontrado.");
        }

        atributoUMLService.eliminar(atributo.getId());

        if (versionService != null) {
            versionService.registrarCambio(modelo.getId(), TipoOperacionHistorial.DELETE,
                    "Atributo (IA)", atributo.getId().toString(), nombreAttr, null, usuarioEmail);
        }

        return "Atributo " + nombreAttr + " eliminado correctamente";
    }

    private String ejecutarCrearRelacion(ModeloUML modelo, ParsedAIAction accion, String usuarioEmail) {
        String origen = accion.getClaseOrigen();
        String destino = accion.getClaseDestino();

        if (origen == null || destino == null) {
            throw new ValidationException("Debe especificar la clase de origen y la clase de destino.");
        }

        ClaseUML claseOrigen = claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(modelo.getId(), origen)
                .orElseThrow(() -> new ResourceNotFoundException("Clase origen '" + origen + "' no encontrada en el modelo."));

        ClaseUML claseDestino = claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(modelo.getId(), destino)
                .orElseThrow(() -> new ResourceNotFoundException("Clase destino '" + destino + "' no encontrada en el modelo."));

        TipoRelacionUML tipoRel = TipoRelacionUML.ASOCIACION;
        if (accion.getTipoRelacion() != null) {
            try {
                tipoRel = TipoRelacionUML.valueOf(accion.getTipoRelacion().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        RelacionUMLDTO nuevaRelacion = relacionUMLService.crearRelacion(RelacionUMLDTO.builder()
                .tipoRelacion(tipoRel)
                .claseOrigenId(claseOrigen.getId())
                .claseDestinoId(claseDestino.getId())
                .cardinalidadOrigen(accion.getCardinalidadOrigen() != null ? accion.getCardinalidadOrigen() : "1")
                .cardinalidadDestino(accion.getCardinalidadDestino() != null ? accion.getCardinalidadDestino() : "*")
                .descripcion(accion.getDescripcion())
                .modeloId(modelo.getId())
                .build());

        difundirEvento(modelo.getId(), TipoOperacionUML.CREATE, TipoElementoUML.RELACION,
                nuevaRelacion.getId().toString(), Map.of("relacion", nuevaRelacion), usuarioEmail);

        if (versionService != null) {
            versionService.registrarCambio(modelo.getId(), TipoOperacionHistorial.CREATE,
                    "Relación UML (IA)", nuevaRelacion.getId().toString(), null,
                    origen + " -> " + destino + " (" + tipoRel + ")", usuarioEmail);
        }

        return "Relación " + tipoRel + " entre " + origen + " y " + destino + " creada correctamente";
    }

    private String ejecutarModificarRelacion(ModeloUML modelo, ParsedAIAction accion, String usuarioEmail) {
        String origen = accion.getClaseOrigen();
        String destino = accion.getClaseDestino();

        if (origen == null || destino == null) {
            throw new ValidationException("Debe especificar las clases involucradas en la relación.");
        }

        List<RelacionUML> relaciones = relacionUMLRepository.findByModeloUMLId(modelo.getId());
        RelacionUML relacionEncontrada = null;
        for (RelacionUML r : relaciones) {
            boolean matchDirecto = r.getClaseOrigen().getNombre().equalsIgnoreCase(origen)
                    && r.getClaseDestino().getNombre().equalsIgnoreCase(destino);
            boolean matchInverso = r.getClaseOrigen().getNombre().equalsIgnoreCase(destino)
                    && r.getClaseDestino().getNombre().equalsIgnoreCase(origen);
            if (matchDirecto || matchInverso) {
                relacionEncontrada = r;
                break;
            }
        }

        if (relacionEncontrada == null) {
            throw new ResourceNotFoundException("No se encontró una relación existente entre '" + origen + "' y '" + destino + "'.");
        }

        if (accion.getCardinalidadOrigen() != null) {
            relacionEncontrada.setCardinalidadOrigen(accion.getCardinalidadOrigen());
        }
        if (accion.getCardinalidadDestino() != null) {
            relacionEncontrada.setCardinalidadDestino(accion.getCardinalidadDestino());
        }
        relacionUMLRepository.save(relacionEncontrada);

        if (versionService != null) {
            versionService.registrarCambio(modelo.getId(), TipoOperacionHistorial.UPDATE,
                    "Relación UML (IA)", relacionEncontrada.getId().toString(),
                    "Cardinalidad previa",
                    accion.getCardinalidadOrigen() + ":" + accion.getCardinalidadDestino(), usuarioEmail);
        }

        return "Relación " + origen + "-" + destino + " modificada a cardinalidad " +
                accion.getCardinalidadOrigen() + ":" + accion.getCardinalidadDestino() + " correctamente";
    }

    private String ejecutarEliminarRelacion(ModeloUML modelo, ParsedAIAction accion, String usuarioEmail) {
        String origen = accion.getClaseOrigen();
        String destino = accion.getClaseDestino();

        if (origen == null || destino == null) {
            throw new ValidationException("Debe indicar las clases de la relación a eliminar.");
        }

        List<RelacionUML> relaciones = relacionUMLRepository.findByModeloUMLId(modelo.getId());
        RelacionUML relacionEncontrada = null;
        for (RelacionUML r : relaciones) {
            boolean matchDirecto = r.getClaseOrigen().getNombre().equalsIgnoreCase(origen)
                    && r.getClaseDestino().getNombre().equalsIgnoreCase(destino);
            boolean matchInverso = r.getClaseOrigen().getNombre().equalsIgnoreCase(destino)
                    && r.getClaseDestino().getNombre().equalsIgnoreCase(origen);
            if (matchDirecto || matchInverso) {
                relacionEncontrada = r;
                break;
            }
        }

        if (relacionEncontrada == null) {
            throw new ResourceNotFoundException("Relación entre '" + origen + "' y '" + destino + "' no encontrada.");
        }

        relacionUMLService.eliminar(relacionEncontrada.getId());

        difundirEvento(modelo.getId(), TipoOperacionUML.DELETE, TipoElementoUML.RELACION,
                relacionEncontrada.getId().toString(), Map.of("origen", origen, "destino", destino), usuarioEmail);

        if (versionService != null) {
            versionService.registrarCambio(modelo.getId(), TipoOperacionHistorial.DELETE,
                    "Relación UML (IA)", relacionEncontrada.getId().toString(),
                    origen + "-" + destino, null, usuarioEmail);
        }

        return "Relación entre " + origen + " y " + destino + " eliminada correctamente";
    }

    private void difundirEvento(Long modeloId, TipoOperacionUML operacion, TipoElementoUML elemento,
                                String elementoId, Map<String, Object> datos, String usuarioEmail) {
        if (eventPublisher == null) return;
        try {
            UMLEvent event = UMLEvent.builder()
                    .modeloUMLId(modeloId)
                    .tipoOperacion(operacion)
                    .elementoTipo(elemento)
                    .elementoId(elementoId)
                    .usuario(usuarioEmail != null ? usuarioEmail : "agente.ia@caseplatform.com")
                    .fecha(LocalDateTime.now())
                    .datosCambio(datos)
                    .build();
            eventPublisher.publishEvent(modeloId, event);
        } catch (Exception e) {
            log.warn("No se pudo difundir evento colaborativo por WebSocket: {}", e.getMessage());
        }
    }

    private String ejecutarGenerarBackend(ModeloUML modelo, String usuarioEmail) {
        if (backendGeneratorService == null) {
            return "El motor de generación automática de backend no se encuentra disponible actualmente.";
        }
        var response = backendGeneratorService.generateProject(modelo.getId(), null, usuarioEmail);
        return String.format(
                "¡Proyecto Spring Boot '%s' generado exitosamente! Se generaron %d entidades y %d archivos en total. " +
                "El paquete descargable .ZIP está disponible para su descarga.",
                response.getProjectName(), response.getTotalEntities(), response.getTotalFiles()
        );
    }

    private String ejecutarExportarEA(ModeloUML modelo, String usuarioEmail) {
        if (eaExportService == null) {
            return "El módulo de exportación a Enterprise Architect no se encuentra disponible actualmente.";
        }
        var meta = eaExportService.exportModel(modelo.getId(), usuarioEmail);
        return String.format(
                "¡Modelo UML exportado exitosamente a formato Enterprise Architect XMI 2.1! " +
                "Total de clases: %d, relaciones: %d. Archivo disponible: '%s' (%d bytes). " +
                "Puedes descargarlo directamente desde el menú 'Enterprise Architect'.",
                meta.getTotalClases(), meta.getTotalRelaciones(), meta.getNombreArchivo(), meta.getTamanoBytes()
        );
    }

    private void registrarHistorialComando(ModeloUML modelo, Usuario usuario, String usuarioEmail,
                                           String prompt, ParsedAIAction accion, String respuesta,
                                           boolean exitoso, boolean requiereConfirmacion) {
        try {
            String parametrosJson = null;
            if (accion != null) {
                try {
                    parametrosJson = objectMapper.writeValueAsString(accion);
                } catch (Exception ignored) {
                }
            }

            AICommandHistory history = AICommandHistory.builder()
                    .tipoOperacion(accion != null ? accion.getTipoOperacion() : TipoOperacionAI.UNKNOWN)
                    .elementoObjetivo(accion != null && accion.getNombreClase() != null ? accion.getNombreClase() : "")
                    .parametros(parametrosJson)
                    .promptOriginal(prompt)
                    .respuestaGenerada(respuesta)
                    .exitoso(exitoso)
                    .requiereConfirmacion(requiereConfirmacion)
                    .usuario(usuario)
                    .usuarioEmail(usuarioEmail != null ? usuarioEmail : "agente.ia@caseplatform.com")
                    .fecha(LocalDateTime.now())
                    .modeloUML(modelo)
                    .build();

            aiCommandHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Error al registrar comando IA en el historial: {}", e.getMessage());
        }
    }
}
