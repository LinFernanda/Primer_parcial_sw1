package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.GeneratedFile;
import com.caseplatform.generator.model.GeneratedProjectModel;

import java.util.List;

public interface ProjectStructureBuilder {

    /**
     * Genera la estructura raíz del proyecto: pom.xml, application.yml, clase Application principal,
     * Dockerfile, docker-compose.yml y README.md.
     */
    List<GeneratedFile> buildProjectStructure(GeneratedProjectModel project);
}
