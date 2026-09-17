package com.caseplatform.generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Respuesta devuelta tras la generación exitosa del backend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratorResponseDTO {

    private Long modeloId;
    private String projectName;
    private String packageName;
    private Integer totalFiles;
    private Integer totalEntities;
    private String zipDownloadUrl;
    private LocalDateTime fechaGeneracion;
    private String mensaje;
}
