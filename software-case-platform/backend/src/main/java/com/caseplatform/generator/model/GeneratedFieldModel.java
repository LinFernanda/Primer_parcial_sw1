package com.caseplatform.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un campo/atributo transformado a nivel de entidad Java.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedFieldModel {

    private String name;
    private String javaType;
    private String umlType;
    private String columnName;
    
    @Builder.Default
    private boolean isId = false;

    @Builder.Default
    private boolean isGenerated = false;

    @Builder.Default
    private boolean nullable = true;

    @Builder.Default
    private boolean unique = false;

    private String initialValue;
    private String validationAnnotation;
}
