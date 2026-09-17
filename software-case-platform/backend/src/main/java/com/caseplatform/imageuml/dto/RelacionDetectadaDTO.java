package com.caseplatform.imageuml.dto;

import com.caseplatform.model.TipoRelacionUML;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una relación detectada visualmente entre dos clases (flecha, conector, cardinalidades).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelacionDetectadaDTO {

    private String claseOrigen;
    private String claseDestino;

    @Builder.Default
    private TipoRelacionUML tipoRelacion = TipoRelacionUML.ASOCIACION;

    @Builder.Default
    private String cardinalidadOrigen = "1";

    @Builder.Default
    private String cardinalidadDestino = "1";

    private String descripcion;
}
