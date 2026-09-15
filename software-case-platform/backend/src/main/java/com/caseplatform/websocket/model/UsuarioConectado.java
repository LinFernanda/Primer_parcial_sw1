package com.caseplatform.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa la información de presencia de un usuario conectado a un modelo UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioConectado {
    private String usuario; // Email identificador
    private String nombre;  // Nombre para mostrar
    private String color;   // Color visual del cursor/nodo
    private String elementoEditando; // ID del elemento actualmente en edición, si aplica
    @Builder.Default
    private LocalDateTime conectadoDesde = LocalDateTime.now();
}
