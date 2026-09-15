package com.caseplatform.websocket.listener;

import com.caseplatform.websocket.model.TipoElementoUML;
import com.caseplatform.websocket.model.TipoOperacionUML;
import com.caseplatform.websocket.model.UMLEvent;
import com.caseplatform.websocket.service.EventPublisher;
import com.caseplatform.websocket.service.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

/**
 * Escuchador de eventos de ciclo de vida de WebSocket para liberar bloqueos
 * y actualizar la presencia tras desconexiones involuntarias o voluntarias.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SessionManager sessionManager;
    private final EventPublisher eventPublisher;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        SessionManager.SessionInfo sessionInfo = sessionManager.removeSession(sessionId);

        if (sessionInfo != null) {
            Long modeloId = sessionInfo.getModeloId();
            String username = sessionInfo.getUsername();

            log.info("Desconexión detectada para sesión '{}' del usuario '{}' en modelo ID: {}",
                    sessionId, username, modeloId);

            // Liberar bloqueos retenidos por el usuario desconectado
            sessionManager.releaseAllLocksForUser(modeloId, username);

            // Notificar salida del usuario al grupo
            UMLEvent leaveEvent = UMLEvent.builder()
                    .usuario(username)
                    .modeloUMLId(modeloId)
                    .tipoOperacion(TipoOperacionUML.LEAVE)
                    .elementoTipo(TipoElementoUML.PRESENCIA)
                    .fecha(LocalDateTime.now())
                    .build();
            eventPublisher.publishEvent(modeloId, leaveEvent);

            // Sincronizar listas de presencia y bloqueos activos
            eventPublisher.publishPresence(modeloId, sessionManager.getConnectedUsers(modeloId));
            eventPublisher.publishLocks(modeloId, sessionManager.getActiveLocks(modeloId));
        }
    }
}
