package com.caseplatform.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una relación JPA interpretada a partir de cardinalidades y tipos UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedRelationModel {

    private TipoRelacionJPA tipoRelacionJPA;
    private String fieldName;
    private String targetEntity;
    private String mappedBy;
    private String joinColumnName;
    private String cascadeType;
    private String fetchType;
    
    @Builder.Default
    private boolean orphanRemoval = false;

    private String cardinalidadOrigen;
    private String cardinalidadDestino;
    private String descripcion;
}
