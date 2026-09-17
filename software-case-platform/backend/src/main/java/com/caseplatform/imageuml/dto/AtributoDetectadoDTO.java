package com.caseplatform.imageuml.dto;

import com.caseplatform.model.VisibilidadUML;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un atributo detectado visual o textualmente en una clase UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtributoDetectadoDTO {

    private String nombre;
    private String tipoDato;

    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PRIVATE;

    private String valorInicial;
}
