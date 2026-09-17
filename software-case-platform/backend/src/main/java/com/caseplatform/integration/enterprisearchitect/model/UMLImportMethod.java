package com.caseplatform.integration.enterprisearchitect.model;

import com.caseplatform.model.VisibilidadUML;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un método u operación extraída de un archivo XMI de Enterprise Architect.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UMLImportMethod {
    private String id;
    private String nombre;
    @Builder.Default
    private String tipoRetorno = "void";
    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PUBLIC;
    private String parametros;
}
