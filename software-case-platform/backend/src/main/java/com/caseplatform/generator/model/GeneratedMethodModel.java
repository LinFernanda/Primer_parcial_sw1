package com.caseplatform.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un método de negocio o interfaz generado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedMethodModel {

    private String name;
    
    @Builder.Default
    private String returnType = "void";

    private String parameters;
    
    @Builder.Default
    private String visibility = "public";
}
