package com.caseplatform.imageuml.dto;

import com.caseplatform.model.VisibilidadUML;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un método u operación detectada en una clase UML de la imagen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetodoDetectadoDTO {

    private String nombre;

    @Builder.Default
    private String tipoRetorno = "void";

    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PUBLIC;

    private String parametros;
}
