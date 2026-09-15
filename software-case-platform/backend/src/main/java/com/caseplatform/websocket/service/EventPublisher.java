package com.caseplatform.websocket.service;

import com.caseplatform.websocket.model.BloqueoElemento;
import com.caseplatform.websocket.model.TipoElementoUML;
import com.caseplatform.websocket.model.TipoOperacionUML;
import com.caseplatform.websocket.model.UMLEvent;
import com.caseplatform.websocket.model.UsuarioConectado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de difundir eventos colaborativos en tiempo real
 * a través de tópicos STOMP dedicados por modelo UML.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Publica un evento UML a todos los suscriptores del modelo.
     */
    public void publishEvent(Long modeloId, UMLEvent event) {
        String destination = "/topic/modelo/" + modeloId;
        log.debug("Difundiendo evento {} a destino: {}", event.getTipoOperacion(), destination);
        messagingTemplate.convertAndSend(destination, event);
    }

    /**
     * Publica la lista sincronizada de usuarios en línea en el modelo.
     */
    public void publishPresence(Long modeloId, List<UsuarioConectado> usuarios) {
        UMLEvent event = UMLEvent.builder()
                .usuario("SYSTEM")
                .modeloUMLId(modeloId)
                .tipoOperacion(TipoOperacionUML.PRESENCE_SYNC)
                .elementoTipo(TipoElementoUML.PRESENCIA)
                .fecha(LocalDateTime.now())
                .datosCambio(Map.of("usuarios", usuarios))
                .build();
        publishEvent(modeloId, event);
    }

    /**
     * Publica la lista sincronizada de elementos bloqueados en el modelo.
     */
    public void publishLocks(Long modeloId, List<BloqueoElemento> locks) {
        UMLEvent event = UMLEvent.builder()
                .usuario("SYSTEM")
                .modeloUMLId(modeloId)
                .tipoOperacion(TipoOperacionUML.LOCKS_SYNC)
                .elementoTipo(TipoElementoUML.MODELO)
                .fecha(LocalDateTime.now())
                .datosCambio(Map.of("bloqueos", locks))
                .build();
        publishEvent(modeloId, event);
    }

    /**
     * Envía un mensaje privado o de error a una cola de usuario específica.
     */
    public void sendToUser(String username, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(username, destination, payload);
    }
}
