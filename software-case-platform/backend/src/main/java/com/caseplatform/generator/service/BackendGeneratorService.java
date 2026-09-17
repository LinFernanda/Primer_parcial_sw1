package com.caseplatform.generator.service;

import com.caseplatform.generator.dto.GeneratedProjectPreviewDTO;
import com.caseplatform.generator.dto.GeneratorRequestDTO;
import com.caseplatform.generator.dto.GeneratorResponseDTO;

public interface BackendGeneratorService {

    /**
     * Previsualiza los archivos y código que serán generados a partir del modelo UML.
     */
    GeneratedProjectPreviewDTO previewProject(Long modeloId, GeneratorRequestDTO request);

    /**
     * Genera el proyecto Spring Boot completo y lo empaqueta en un archivo .ZIP descargable.
     */
    byte[] generateProjectZip(Long modeloId, GeneratorRequestDTO request, String usuarioEmail);

    /**
     * Orquesta la generación y retorna la información resumen del proyecto generado.
     */
    GeneratorResponseDTO generateProject(Long modeloId, GeneratorRequestDTO request, String usuarioEmail);
}
