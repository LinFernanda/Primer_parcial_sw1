package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.GeneratedEntityModel;
import com.caseplatform.generator.model.GeneratedFile;

public interface RepositoryGeneratorService {

    /**
     * Genera la interfaz de persistencia Spring Data JPA para la entidad dada.
     */
    GeneratedFile generateRepository(String basePackage, GeneratedEntityModel entity);
}
