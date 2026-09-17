package com.caseplatform.generator.analyzer;

import com.caseplatform.generator.dto.GeneratorRequestDTO;
import com.caseplatform.generator.model.GeneratedProjectModel;
import com.caseplatform.model.ModeloUML;

/**
 * Servicio encargado de analizar y transformar un ModeloUML conceptual en un
 * modelo intermedio preparado para la generación de código Spring Boot.
 */
public interface UMLAnalyzerService {

    /**
     * Analiza el modelo conceptual UML y genera el modelo intermedio estructurado.
     */
    GeneratedProjectModel analyze(ModeloUML modelo, GeneratorRequestDTO request);
}
