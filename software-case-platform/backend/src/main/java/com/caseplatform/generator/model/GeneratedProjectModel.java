package com.caseplatform.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo intermedio global del proyecto Spring Boot a generar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedProjectModel {

    private Long modeloId;
    
    @Builder.Default
    private String projectName = "SpringBootBackend";

    @Builder.Default
    private String artifactId = "spring-boot-backend";

    @Builder.Default
    private String groupId = "com.caseplatform.generated";

    @Builder.Default
    private String basePackage = "com.caseplatform.generated";

    @Builder.Default
    private String springBootVersion = "3.3.4";

    @Builder.Default
    private String javaVersion = "21";

    @Builder.Default
    private String description = "Backend generado automáticamente desde modelo conceptual UML";

    @Builder.Default
    private String databaseName = "app_db";

    @Builder.Default
    private Integer serverPort = 8081;

    @Builder.Default
    private List<GeneratedEntityModel> entities = new ArrayList<>();

    @Builder.Default
    private List<GeneratedFile> files = new ArrayList<>();
}
