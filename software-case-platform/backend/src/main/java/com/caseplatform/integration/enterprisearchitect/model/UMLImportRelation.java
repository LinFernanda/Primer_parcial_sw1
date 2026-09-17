package com.caseplatform.integration.enterprisearchitect.model;

import com.caseplatform.model.TipoRelacionUML;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una relación extraída de un archivo XMI de Enterprise Architect.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UMLImportRelation {
    private String xmiId;
    private String nombre;

    @Builder.Default
    private TipoRelacionUML tipoRelacion = TipoRelacionUML.ASOCIACION;

    private String claseOrigenId;
    private String claseOrigenNombre;

    private String claseDestinoId;
    private String claseDestinoNombre;

    @Builder.Default
    private String cardinalidadOrigen = "1";

    @Builder.Default
    private String cardinalidadDestino = "1";

    private String descripcion;
}
