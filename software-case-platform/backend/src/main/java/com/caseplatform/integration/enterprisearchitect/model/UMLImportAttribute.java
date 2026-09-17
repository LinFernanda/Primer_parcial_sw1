package com.caseplatform.integration.enterprisearchitect.model;

import com.caseplatform.model.VisibilidadUML;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un atributo extraído de un archivo XMI de Enterprise Architect.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UMLImportAttribute {
    private String id;
    private String nombre;
    private String tipoDato;
    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PRIVATE;
    private String valorInicial;
}
