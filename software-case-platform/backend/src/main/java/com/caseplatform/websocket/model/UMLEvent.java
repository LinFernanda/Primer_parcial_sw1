package com.caseplatform.websocket.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Representa un evento atómico de mutación o sincronización en el modelo UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UMLEvent {

    @Builder.Default
    private String idEvento = UUID.randomUUID().toString();

    private String usuario;

    private Long modeloUMLId;

    private TipoOperacionUML tipoOperacion;

    private TipoElementoUML elementoTipo;

    private String elementoId;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha = LocalDateTime.now();

    private Map<String, Object> datosCambio;
}
