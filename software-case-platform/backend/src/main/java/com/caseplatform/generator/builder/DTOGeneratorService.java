package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.GeneratedEntityModel;
import com.caseplatform.generator.model.GeneratedFile;

import java.util.List;

public interface DTOGeneratorService {

    /**
     * Genera los DTOs de petición y respuesta para una entidad JPA.
     * Regla: No exponer directamente entidades JPA en los controladores REST.
     */
    List<GeneratedFile> generateDTOs(String basePackage, GeneratedEntityModel entity);
}
