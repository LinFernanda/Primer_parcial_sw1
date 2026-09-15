package com.caseplatform.websocket.service;

import com.caseplatform.websocket.model.BloqueoElemento;
import com.caseplatform.websocket.model.UsuarioConectado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor en memoria de sesiones activas, presencia y bloqueos de concurrencia
 * para colaboración multiusuario en diagramas UML.
 */
@Slf4j
@Service
public class SessionManager {

    private static final List<String> PALETA_COLORES = List.of(
            "#3B82F6", // Azul
            "#10B981", // Verde esmeralda
            "#F59E0B", // Ámbar
            "#EF4444", // Rojo
            "#8B5CF6", // Violeta
            "#EC4899", // Rosa
            "#06B6D4", // Cian
            "#14B8A6"  // Verde azulado
    );

    @Data
    @AllArgsConstructor
    public static class SessionInfo {
        private String sessionId;
        private String username;
        private Long modeloId;
    }

    // sessionId -> SessionInfo
    private final Map<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();

    // modeloId -> (username -> UsuarioConectado)
    private final Map<Long, Map<String, UsuarioConectado>> modelUsers = new ConcurrentHashMap<>();

    // modeloId -> (elementoId -> BloqueoElemento)
    private final Map<Long, Map<String, BloqueoElemento>> modelLocks = new ConcurrentHashMap<>();

    /**
     * Registra un usuario en la sesión colaborativa de un modelo UML.
     */
    public synchronized UsuarioConectado registerUser(Long modeloId, String sessionId, String username, String nombre) {
        sessionMap.put(sessionId, new SessionInfo(sessionId, username, modeloId));

        Map<String, UsuarioConectado> users = modelUsers.computeIfAbsent(modeloId, k -> new ConcurrentHashMap<>());
        
        String displayName = (nombre != null && !nombre.isBlank()) ? nombre : username;
        int colorIdx = Math.abs(username.hashCode()) % PALETA_COLORES.size();
        String color = PALETA_COLORES.get(colorIdx);

        UsuarioConectado usuario = users.computeIfAbsent(username, u -> UsuarioConectado.builder()
                .usuario(username)
                .nombre(displayName)
                .color(color)
                .conectadoDesde(LocalDateTime.now())
                .build());

        log.info("Usuario '{}' ({}) registrado en sesión modelo ID: {}. Total activos: {}",
                username, sessionId, modeloId, users.size());
        return usuario;
    }

    /**
     * Remueve una sesión por desconexión y retorna la información de la sesión liberada.
     */
    public synchronized SessionInfo removeSession(String sessionId) {
        SessionInfo info = sessionMap.remove(sessionId);
        if (info == null) {
            return null;
        }

        Long modeloId = info.getModeloId();
        String username = info.getUsername();

        // Verificar si el usuario tiene otras sesiones activas en el mismo modelo
        boolean hasOtherSessions = sessionMap.values().stream()
                .anyMatch(s -> s.getModeloId().equals(modeloId) && s.getUsername().equals(username));

        if (!hasOtherSessions) {
            Map<String, UsuarioConectado> users = modelUsers.get(modeloId);
            if (users != null) {
                users.remove(username);
                if (users.isEmpty()) {
                    modelUsers.remove(modeloId);
                }
            }
            log.info("Usuario '{}' salió completamente del modelo ID: {}", username, modeloId);
        }

        return info;
    }

    /**
     * Intenta bloquear un elemento para edición exclusiva de un usuario.
     * Retorna true si se adquiere o ya pertenecía al usuario; false si está bloqueado por otro.
     */
    public synchronized boolean lockElement(Long modeloId, String elementoId, String elementoTipo, String username) {
        Map<String, BloqueoElemento> locks = modelLocks.computeIfAbsent(modeloId, k -> new ConcurrentHashMap<>());
        BloqueoElemento actual = locks.get(elementoId);

        if (actual != null && !actual.getUsuario().equalsIgnoreCase(username)) {
            log.warn("Conflicto de concurrencia: Elemento '{}' bloqueado por '{}', no se permite a '{}'",
                    elementoId, actual.getUsuario(), username);
            return false;
        }

        BloqueoElemento nuevoBloqueo = BloqueoElemento.builder()
                .elementoId(elementoId)
                .elementoTipo(elementoTipo)
                .usuario(username)
                .fechaBloqueo(LocalDateTime.now())
                .build();
        locks.put(elementoId, nuevoBloqueo);

        // Actualizar estado en UsuarioConectado
        Map<String, UsuarioConectado> users = modelUsers.get(modeloId);
        if (users != null && users.containsKey(username)) {
            users.get(username).setElementoEditando(elementoId);
        }

        log.info("Elemento '{}' bloqueado con éxito por '{}' en modelo ID: {}", elementoId, username, modeloId);
        return true;
    }

    /**
     * Libera el bloqueo de un elemento si pertenece al usuario.
     */
    public synchronized boolean unlockElement(Long modeloId, String elementoId, String username) {
        Map<String, BloqueoElemento> locks = modelLocks.get(modeloId);
        if (locks == null) {
            return false;
        }

        BloqueoElemento actual = locks.get(elementoId);
        if (actual != null && (actual.getUsuario().equalsIgnoreCase(username) || username.equalsIgnoreCase("SYSTEM"))) {
            locks.remove(elementoId);

            Map<String, UsuarioConectado> users = modelUsers.get(modeloId);
            if (users != null && users.containsKey(actual.getUsuario())) {
                users.get(actual.getUsuario()).setElementoEditando(null);
            }

            log.info("Elemento '{}' desbloqueado por '{}' en modelo ID: {}", elementoId, username, modeloId);
            return true;
        }
        return false;
    }

    /**
     * Libera todos los bloqueos en poder de un usuario (por ejemplo ante desconexión).
     */
    public synchronized List<BloqueoElemento> releaseAllLocksForUser(Long modeloId, String username) {
        Map<String, BloqueoElemento> locks = modelLocks.get(modeloId);
        if (locks == null || locks.isEmpty()) {
            return Collections.emptyList();
        }

        List<BloqueoElemento> liberados = new ArrayList<>();
        Iterator<Map.Entry<String, BloqueoElemento>> it = locks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BloqueoElemento> entry = it.next();
            if (entry.getValue().getUsuario().equalsIgnoreCase(username)) {
                liberados.add(entry.getValue());
                it.remove();
            }
        }

        if (!liberados.isEmpty()) {
            log.info("Liberados {} bloqueos retenidos por '{}' en modelo ID: {}",
                    liberados.size(), username, modeloId);
        }
        return liberados;
    }

    /**
     * Retorna la lista de usuarios actualmente conectados a un modelo.
     */
    public List<UsuarioConectado> getConnectedUsers(Long modeloId) {
        Map<String, UsuarioConectado> users = modelUsers.get(modeloId);
        if (users == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(users.values());
    }

    /**
     * Retorna la lista de bloqueos activos en un modelo.
     */
    public List<BloqueoElemento> getActiveLocks(Long modeloId) {
        Map<String, BloqueoElemento> locks = modelLocks.get(modeloId);
        if (locks == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(locks.values());
    }

    /**
     * Comprueba si un elemento está bloqueado por otro usuario.
     */
    public boolean isElementLockedByOther(Long modeloId, String elementoId, String username) {
        Map<String, BloqueoElemento> locks = modelLocks.get(modeloId);
        if (locks == null) {
            return false;
        }
        BloqueoElemento lock = locks.get(elementoId);
        return lock != null && !lock.getUsuario().equalsIgnoreCase(username);
    }
}
