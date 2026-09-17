package com.caseplatform.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo intermedio de una entidad JPA con sus campos, relaciones y métodos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedEntityModel {

    private String name;
    private String tableName;
    private String description;
    
    @Builder.Default
    private String primaryKeyName = "id";

    @Builder.Default
    private String primaryKeyType = "Long";

    private String parentEntity; // Para herencia

    @Builder.Default
    private List<GeneratedFieldModel> fields = new ArrayList<>();

    @Builder.Default
    private List<GeneratedRelationModel> relations = new ArrayList<>();

    @Builder.Default
    private List<GeneratedMethodModel> methods = new ArrayList<>();
}
