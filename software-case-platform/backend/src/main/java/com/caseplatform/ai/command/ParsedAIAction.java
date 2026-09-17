package com.caseplatform.ai.command;

import com.caseplatform.ai.model.TipoOperacionAI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa la acción UML estructurada resultante de la interpretación
 * semántica del comando en lenguaje natural por parte de la IA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedAIAction {

    private TipoOperacionAI tipoOperacion;
    private String nombreClase;
    private String nuevoNombreClase;
    private String nombreAtributo;
    private String nuevoNombreAtributo;
    private String tipoDatoAtributo;
    private String visibilidad;
    private String claseOrigen;
    private String claseDestino;
    private String tipoRelacion;
    private String cardinalidadOrigen;
    private String cardinalidadDestino;
    private String descripcion;
    private String explicacion;
    private boolean requiereConfirmacion;
    private String preguntaConfirmacion;

    @Builder.Default
    private List<AtributoSimple> atributos = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtributoSimple {
        private String nombre;
        private String tipo;
        private String visibilidad;
    }
}
