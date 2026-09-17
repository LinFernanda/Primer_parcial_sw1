package com.caseplatform.generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parámetros opcionales para personalizar la generación del proyecto Spring Boot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratorRequestDTO {

    @Builder.Default
    private String groupId = "com.caseplatform.generated";

    private String artifactId;

    @Builder.Default
    private String packageName = "com.caseplatform.generated";

    private String projectName;

    @Builder.Default
    private String databaseName = "app_db";

    @Builder.Default
    private Integer serverPort = 8081;

    @Builder.Default
    private Boolean includeDocker = true;

    @Builder.Default
    private Boolean includeSwagger = true;
}
