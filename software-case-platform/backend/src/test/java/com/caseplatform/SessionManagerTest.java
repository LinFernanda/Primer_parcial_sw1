package com.caseplatform;

import com.caseplatform.websocket.model.BloqueoElemento;
import com.caseplatform.websocket.model.UsuarioConectado;
import com.caseplatform.websocket.service.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
    }

    @Test
    @DisplayName("Debe registrar usuarios en presencia y recuperar lista de conectados")
    void testRegisterUserAndPresence() {
        UsuarioConectado u1 = sessionManager.registerUser(1L, "sess-1", "ana@case.com", "Ana Dev");
        UsuarioConectado u2 = sessionManager.registerUser(1L, "sess-2", "carlos@case.com", "Carlos Arch");

        assertNotNull(u1);
        assertNotNull(u2);
        assertNotNull(u1.getColor());
        assertNotNull(u2.getColor());

        List<UsuarioConectado> conectados = sessionManager.getConnectedUsers(1L);
        assertEquals(2, conectados.size());
    }

    @Test
    @DisplayName("Debe bloquear elemento y evitar colisión de concurrencia de otro usuario")
    void testLockElementConflict() {
        sessionManager.registerUser(1L, "sess-1", "ana@case.com", "Ana Dev");
        sessionManager.registerUser(1L, "sess-2", "carlos@case.com", "Carlos Arch");

        // Ana bloquea la clase "clase-101"
        boolean bloqueoAna = sessionManager.lockElement(1L, "clase-101", "CLASE", "ana@case.com");
        assertTrue(bloqueoAna, "Ana debe poder bloquear el elemento");

        // Carlos intenta bloquear la misma clase
        boolean bloqueoCarlos = sessionManager.lockElement(1L, "clase-101", "CLASE", "carlos@case.com");
        assertFalse(bloqueoCarlos, "Carlos NO debe poder bloquear un elemento ya bloqueado por Ana");

        assertTrue(sessionManager.isElementLockedByOther(1L, "clase-101", "carlos@case.com"));
        assertFalse(sessionManager.isElementLockedByOther(1L, "clase-101", "ana@case.com"));
    }

    @Test
    @DisplayName("Debe liberar bloqueo al invocar unlockElement o al desconectar usuario")
    void testUnlockAndDisconnect() {
        sessionManager.registerUser(1L, "sess-1", "ana@case.com", "Ana Dev");

        sessionManager.lockElement(1L, "clase-202", "CLASE", "ana@case.com");
        assertEquals(1, sessionManager.getActiveLocks(1L).size());

        // Ana libera el bloqueo
        boolean desbloqueado = sessionManager.unlockElement(1L, "clase-202", "ana@case.com");
        assertTrue(desbloqueado);
        assertEquals(0, sessionManager.getActiveLocks(1L).size());

        // Vuelve a bloquear y luego se desconecta
        sessionManager.lockElement(1L, "clase-202", "CLASE", "ana@case.com");
        List<BloqueoElemento> liberados = sessionManager.releaseAllLocksForUser(1L, "ana@case.com");
        assertEquals(1, liberados.size());
        assertEquals(0, sessionManager.getActiveLocks(1L).size());

        // Desconexión de sesión
        SessionManager.SessionInfo info = sessionManager.removeSession("sess-1");
        assertNotNull(info);
        assertEquals(0, sessionManager.getConnectedUsers(1L).size());
    }
}
