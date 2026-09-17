package com.caseplatform.generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Previsualización del árbol de archivos y contenido del proyecto generado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedProjectPreviewDTO {

    private Long modeloId;
    private String projectName;
    private String packageName;
    private Integer totalFiles;
    private Integer totalEntities;

    @Builder.Default
    private List<GeneratedFileDTO> files = new ArrayList<>();
}
