package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.GeneratedEntityModel;
import com.caseplatform.generator.model.GeneratedFile;

import java.util.List;

public interface ServiceGeneratorService {

    /**
     * Genera la interfaz de servicio y su implementación con operaciones CRUD completas y mapeo a DTOs.
     */
    List<GeneratedFile> generateService(String basePackage, GeneratedEntityModel entity);
}
