package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.GeneratedEntityModel;
import com.caseplatform.generator.model.GeneratedFile;

public interface EntityGeneratorService {

    /**
     * Genera el archivo fuente Java de una entidad JPA completa con Lombok y relaciones.
     */
    GeneratedFile generateEntity(String basePackage, GeneratedEntityModel entity);
}
