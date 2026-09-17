package com.caseplatform.websocket.controller;

import com.caseplatform.websocket.model.TipoElementoUML;
import com.caseplatform.websocket.model.TipoOperacionUML;
import com.caseplatform.websocket.model.UMLEvent;
import com.caseplatform.websocket.model.UsuarioConectado;
import com.caseplatform.websocket.service.EventPublisher;
import com.caseplatform.websocket.service.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

import com.caseplatform.versioning.model.TipoOperacionHistorial;
import com.caseplatform.versioning.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controlador de mensajería WebSocket / STOMP para eventos colaborativos en diagramas UML.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class UMLSocketController {

    private final SessionManager sessionManager;
    private final EventPublisher eventPublisher;

    @Autowired(required = false)
    private VersionService versionService;

    /**
     * Notificación de ingreso de un usuario al modelo.
     */
    @MessageMapping("/modelo/{modeloId}/join")
    public void joinModel(
            @DestinationVariable Long modeloId,
            @Payload(required = false) Map<String, Object> payload,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal
    ) {
        String username = resolveUsername(principal, payload);
        String nombre = (payload != null && payload.get("nombre") != null)
                ? payload.get("nombre").toString()
                : username;

        String sessionId = headerAccessor.getSessionId();
        UsuarioConectado usuario = sessionManager.registerUser(modeloId, sessionId, username, nombre);

        // Notificar ingreso individual
        java.util.Map<String, Object> datos = new java.util.HashMap<>();
        if (usuario != null) {
            datos.put("usuarioConectado", usuario);
        }

        UMLEvent joinEvent = UMLEvent.builder()
                .usuario(username)
                .modeloUMLId(modeloId)
                .tipoOperacion(TipoOperacionUML.JOIN)
                .elementoTipo(TipoElementoUML.PRESENCIA)
                .fecha(LocalDateTime.now())
                .datosCambio(datos)
                .build();
        eventPublisher.publishEvent(modeloId, joinEvent);

        // Sincronizar estado completo de usuarios y bloqueos
        eventPublisher.publishPresence(modeloId, sessionManager.getConnectedUsers(modeloId));
        eventPublisher.publishLocks(modeloId, sessionManager.getActiveLocks(modeloId));
    }

    /**
     * Notificación explícita de salida de un modelo.
     */
    @MessageMapping("/modelo/{modeloId}/leave")
    public void leaveModel(
            @DestinationVariable Long modeloId,
            @Payload(required = false) Map<String, Object> payload,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal
    ) {
        String username = resolveUsername(principal, payload);
        String sessionId = headerAccessor.getSessionId();

        sessionManager.removeSession(sessionId);
        sessionManager.releaseAllLocksForUser(modeloId, username);

        UMLEvent leaveEvent = UMLEvent.builder()
                .usuario(username)
                .modeloUMLId(modeloId)
                .tipoOperacion(TipoOperacionUML.LEAVE)
                .elementoTipo(TipoElementoUML.PRESENCIA)
                .fecha(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(modeloId, leaveEvent);

        eventPublisher.publishPresence(modeloId, sessionManager.getConnectedUsers(modeloId));
        eventPublisher.publishLocks(modeloId, sessionManager.getActiveLocks(modeloId));
    }

    /**
     * Procesamiento y retransmisión de eventos atómicos de edición UML.
     */
    @MessageMapping("/modelo/{modeloId}/event")
    public void handleUMLEvent(
            @DestinationVariable Long modeloId,
            @Payload UMLEvent event,
            Principal principal
    ) {
        if (event == null) {
            return;
        }

        event.setModeloUMLId(modeloId);
        if (event.getUsuario() == null || event.getUsuario().isBlank()) {
            event.setUsuario(resolveUsername(principal, null));
        }
        if (event.getFecha() == null) {
            event.setFecha(LocalDateTime.now());
        }

        // Control de concurrencia: evitar mutaciones sobre elementos bloqueados por otros
        if (event.getElementoId() != null &&
                (event.getTipoOperacion() == TipoOperacionUML.UPDATE || event.getTipoOperacion() == TipoOperacionUML.DELETE)) {
            if (sessionManager.isElementLockedByOther(modeloId, event.getElementoId(), event.getUsuario())) {
                log.warn("Rechazando evento {} de '{}' porque el elemento {} está bloqueado por otro usuario",
                        event.getTipoOperacion(), event.getUsuario(), event.getElementoId());
                return;
            }
        }

        // Si fue una eliminación, liberar bloqueo si existía
        if (event.getTipoOperacion() == TipoOperacionUML.DELETE && event.getElementoId() != null) {
            sessionManager.unlockElement(modeloId, event.getElementoId(), event.getUsuario());
            eventPublisher.publishLocks(modeloId, sessionManager.getActiveLocks(modeloId));
        }

        // Trazabilidad y registro en historial de cambios (Fase 6)
        if (versionService != null && (event.getTipoOperacion() == TipoOperacionUML.CREATE
                || event.getTipoOperacion() == TipoOperacionUML.UPDATE
                || event.getTipoOperacion() == TipoOperacionUML.DELETE)) {
            TipoOperacionHistorial tipoHistorial;
            if (event.getTipoOperacion() == TipoOperacionUML.CREATE) {
                tipoHistorial = TipoOperacionHistorial.CREATE;
            } else if (event.getTipoOperacion() == TipoOperacionUML.DELETE) {
                tipoHistorial = TipoOperacionHistorial.DELETE;
            } else {
                tipoHistorial = TipoOperacionHistorial.UPDATE;
            }

            String elementoDesc = event.getElementoTipo() != null ? event.getElementoTipo().name() : "Elemento UML";
            String detalle = event.getDatosCambio() != null ? event.getDatosCambio().toString() : null;

            versionService.registrarCambio(
                    modeloId,
                    tipoHistorial,
                    elementoDesc,
                    event.getElementoId(),
                    null,
                    detalle,
                    event.getUsuario()
            );
        }

        // Difundir evento al grupo
        eventPublisher.publishEvent(modeloId, event);
    }

    /**
     * Solicitud de bloqueo temporal de un elemento UML para prevenir conflictos.
     */
    @MessageMapping("/modelo/{modeloId}/lock")
    public void lockElement(
            @DestinationVariable Long modeloId,
            @Payload Map<String, Object> payload,
            Principal principal
    ) {
        if (payload == null || payload.get("elementoId") == null) {
            return;
        }

        String elementoId = payload.get("elementoId").toString();
        String elementoTipo = payload.getOrDefault("elementoTipo", "CLASE").toString();
        String username = resolveUsername(principal, payload);

        boolean adquirido = sessionManager.lockElement(modeloId, elementoId, elementoTipo, username);
        if (adquirido) {
            UMLEvent lockEvent = UMLEvent.builder()
                    .usuario(username)
                    .modeloUMLId(modeloId)
                    .tipoOperacion(TipoOperacionUML.LOCK)
                    .elementoTipo(TipoElementoUML.valueOf(elementoTipo.toUpperCase()))
                    .elementoId(elementoId)
                    .fecha(LocalDateTime.now())
                    .datosCambio(Map.of("elementoId", elementoId, "usuario", username))
                    .build();
            eventPublisher.publishEvent(modeloId, lockEvent);
            eventPublisher.publishLocks(modeloId, sessionManager.getActiveLocks(modeloId));
        }
    }

    /**
     * Liberación voluntaria del bloqueo de un elemento.
     */
    @MessageMapping("/modelo/{modeloId}/unlock")
    public void unlockElement(
            @DestinationVariable Long modeloId,
            @Payload Map<String, Object> payload,
            Principal principal
    ) {
        if (payload == null || payload.get("elementoId") == null) {
            return;
        }

        String elementoId = payload.get("elementoId").toString();
        String username = resolveUsername(principal, payload);

        boolean liberado = sessionManager.unlockElement(modeloId, elementoId, username);
        if (liberado) {
            UMLEvent unlockEvent = UMLEvent.builder()
                    .usuario(username)
                    .modeloUMLId(modeloId)
                    .tipoOperacion(TipoOperacionUML.UNLOCK)
                    .elementoId(elementoId)
                    .fecha(LocalDateTime.now())
                    .datosCambio(Map.of("elementoId", elementoId))
                    .build();
            eventPublisher.publishEvent(modeloId, unlockEvent);
            eventPublisher.publishLocks(modeloId, sessionManager.getActiveLocks(modeloId));
        }
    }

    private String resolveUsername(Principal principal, Map<String, Object> payload) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        if (payload != null) {
            if (payload.get("usuario") != null) {
                return payload.get("usuario").toString();
            }
            if (payload.get("email") != null) {
                return payload.get("email").toString();
            }
        }
        return "ingeniero.colaborador@caseplatform.com";
    }
}
