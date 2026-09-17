package com.caseplatform.integration.enterprisearchitect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo intermedio agregado que representa el contenido completo del archivo XMI analizado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UMLImportModel {
    private String nombre;
    private String xmiVersion;
    private String exporter;
    private String exporterVersion;

    @Builder.Default
    private List<UMLImportClass> clases = new ArrayList<>();

    @Builder.Default
    private List<UMLImportRelation> relaciones = new ArrayList<>();

    @Builder.Default
    private List<String> advertencias = new ArrayList<>();
}
