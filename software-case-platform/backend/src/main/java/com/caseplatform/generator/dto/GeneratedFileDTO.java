package com.caseplatform.generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representación serializable de un archivo generado para el frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedFileDTO {

    private String relativePath;
    private String content;
    private String category;
    private Long sizeBytes;
}
