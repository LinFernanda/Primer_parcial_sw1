package com.caseplatform.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa el bloqueo de concurrencia temporal de un elemento UML por parte de un usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloqueoElemento {
    private String elementoId;
    private String usuario;
    private String elementoTipo;
    @Builder.Default
    private LocalDateTime fechaBloqueo = LocalDateTime.now();
}
