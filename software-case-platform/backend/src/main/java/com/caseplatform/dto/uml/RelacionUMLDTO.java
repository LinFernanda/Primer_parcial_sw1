package com.caseplatform.dto.uml;

import com.caseplatform.model.TipoRelacionUML;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación, actualización y consulta de relaciones UML entre clases.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelacionUMLDTO {

    private Long id;

    @NotNull(message = "El tipo de relación es obligatorio (ASOCIACION, HERENCIA, DEPENDENCIA, AGREGACION, COMPOSICION)")
    private TipoRelacionUML tipoRelacion;

    @NotNull(message = "El ID de la clase origen es obligatorio")
    private Long claseOrigenId;

    private String claseOrigenNombre;

    @NotNull(message = "El ID de la clase destino es obligatorio")
    private Long claseDestinoId;

    private String claseDestinoNombre;

    @Builder.Default
    private String cardinalidadOrigen = "1";

    @Builder.Default
    private String cardinalidadDestino = "1";

    private String descripcion;

    private Long modeloId;
}
