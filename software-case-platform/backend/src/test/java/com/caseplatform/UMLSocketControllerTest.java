package com.caseplatform;

import com.caseplatform.websocket.controller.UMLSocketController;
import com.caseplatform.websocket.model.TipoElementoUML;
import com.caseplatform.websocket.model.TipoOperacionUML;
import com.caseplatform.websocket.model.UMLEvent;
import com.caseplatform.websocket.service.EventPublisher;
import com.caseplatform.websocket.service.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.security.Principal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UMLSocketControllerTest {

    @Mock
    private SessionManager sessionManager;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UMLSocketController socketController;

    private Principal mockPrincipal;
    private SimpMessageHeaderAccessor headerAccessor;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "usuario.test@caseplatform.com";
        headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionId("session-xyz");
    }

    @Test
    @DisplayName("Debe gestionar joinModel, registrar usuario y publicar presencia")
    void testJoinModel() {
        when(sessionManager.registerUser(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(com.caseplatform.websocket.model.UsuarioConectado.builder()
                        .usuario("usuario.test@caseplatform.com")
                        .nombre("Tester")
                        .color("#3B82F6")
                        .build());

        socketController.joinModel(1L, Map.of("nombre", "Tester"), headerAccessor, mockPrincipal);

        verify(sessionManager, times(1)).registerUser(eq(1L), eq("session-xyz"), eq("usuario.test@caseplatform.com"), eq("Tester"));
        verify(eventPublisher, times(1)).publishEvent(eq(1L), any(UMLEvent.class));
        verify(eventPublisher, times(1)).publishPresence(eq(1L), any());
        verify(eventPublisher, times(1)).publishLocks(eq(1L), any());
    }

    @Test
    @DisplayName("Debe tramitar handleUMLEvent y difundirlo si no hay conflicto de bloqueo")
    void testHandleUMLEventSuccess() {
        UMLEvent event = UMLEvent.builder()
                .tipoOperacion(TipoOperacionUML.CREATE)
                .elementoTipo(TipoElementoUML.CLASE)
                .elementoId("clase-1")
                .datosCambio(Map.of("nombre", "Factura"))
                .build();

        socketController.handleUMLEvent(1L, event, mockPrincipal);

        verify(eventPublisher, times(1)).publishEvent(eq(1L), any(UMLEvent.class));
    }

    @Test
    @DisplayName("Debe rechazar handleUMLEvent si el elemento está bloqueado por otro usuario")
    void testHandleUMLEventRejectedOnLockConflict() {
        when(sessionManager.isElementLockedByOther(1L, "clase-99", "usuario.test@caseplatform.com")).thenReturn(true);

        UMLEvent event = UMLEvent.builder()
                .tipoOperacion(TipoOperacionUML.UPDATE)
                .elementoTipo(TipoElementoUML.CLASE)
                .elementoId("clase-99")
                .datosCambio(Map.of("nombre", "Conflicto"))
                .build();

        socketController.handleUMLEvent(1L, event, mockPrincipal);

        verify(eventPublisher, never()).publishEvent(eq(1L), any(UMLEvent.class));
    }

    @Test
    @DisplayName("Debe procesar solicitud de bloqueo exitosa y difundir LOCK event")
    void testLockElementSuccess() {
        when(sessionManager.lockElement(1L, "clase-10", "CLASE", "usuario.test@caseplatform.com")).thenReturn(true);

        socketController.lockElement(1L, Map.of("elementoId", "clase-10", "elementoTipo", "CLASE"), mockPrincipal);

        verify(eventPublisher, times(1)).publishEvent(eq(1L), any(UMLEvent.class));
        verify(eventPublisher, times(1)).publishLocks(eq(1L), any());
    }
}
