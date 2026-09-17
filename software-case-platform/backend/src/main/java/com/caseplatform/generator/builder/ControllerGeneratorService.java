package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.GeneratedEntityModel;
import com.caseplatform.generator.model.GeneratedFile;

public interface ControllerGeneratorService {

    /**
     * Genera el controlador REST profesional con endpoints GET, POST, PUT y DELETE.
     * Trabaja exclusivamente con DTOs.
     */
    GeneratedFile generateController(String basePackage, GeneratedEntityModel entity);
}
