package com.caseplatform.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un archivo generado listo para empaquetar o previsualizar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedFile {

    private String relativePath;
    private String content;
    private CategoryFileType category;

    public long getSizeBytes() {
        return content != null ? content.getBytes(java.nio.charset.StandardCharsets.UTF_8).length : 0L;
    }
}
