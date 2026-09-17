package com.caseplatform.integration.enterprisearchitect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de validación previa de un archivo XMI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UMLImportValidationResult {
    private boolean valido;

    @Builder.Default
    private List<String> errores = new ArrayList<>();

    @Builder.Default
    private List<String> advertencias = new ArrayList<>();

    private int totalClases;
    private int totalAtributos;
    private int totalMetodos;
    private int totalRelaciones;

    private String nombreModelo;
    private String xmiVersion;
    private String exportador;
}
