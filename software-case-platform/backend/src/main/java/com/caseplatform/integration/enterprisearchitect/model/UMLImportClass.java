package com.caseplatform.integration.enterprisearchitect.model;

import com.caseplatform.model.VisibilidadUML;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una clase UML extraída de un archivo XMI de Enterprise Architect.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UMLImportClass {
    private String xmiId;
    private String nombre;
    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PUBLIC;
    private String descripcion;
    private boolean isAbstract;

    @Builder.Default
    private Double posicionX = 0.0;

    @Builder.Default
    private Double posicionY = 0.0;

    @Builder.Default
    private List<UMLImportAttribute> atributos = new ArrayList<>();

    @Builder.Default
    private List<UMLImportMethod> metodos = new ArrayList<>();
}
