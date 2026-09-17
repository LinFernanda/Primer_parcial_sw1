package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.CategoryFileType;
import com.caseplatform.generator.model.GeneratedEntityModel;
import com.caseplatform.generator.model.GeneratedFile;
import com.caseplatform.generator.model.GeneratedProjectModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectStructureBuilderImpl implements ProjectStructureBuilder {

    @Override
    public List<GeneratedFile> buildProjectStructure(GeneratedProjectModel project) {
        List<GeneratedFile> files = new ArrayList<>();

        // 1. pom.xml
        files.add(generatePomXml(project));

        // 2. application.yml
        files.add(generateApplicationYml(project));

        // 3. Main Application Class
        files.add(generateApplicationClass(project));

        // 4. Dockerfile
        files.add(generateDockerfile(project));

        // 5. docker-compose.yml
        files.add(generateDockerCompose(project));

        // 6. README.md
        files.add(generateReadme(project));

        return files;
    }

    private GeneratedFile generatePomXml(GeneratedProjectModel project) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n");
        sb.append("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
        sb.append("    <modelVersion>4.0.0</modelVersion>\n\n");
        sb.append("    <parent>\n");
        sb.append("        <groupId>org.springframework.boot</groupId>\n");
        sb.append("        <artifactId>spring-boot-starter-parent</artifactId>\n");
        sb.append("        <version>").append(project.getSpringBootVersion()).append("</version>\n");
        sb.append("        <relativePath/>\n");
        sb.append("    </parent>\n\n");
        sb.append("    <groupId>").append(project.getGroupId()).append("</groupId>\n");
        sb.append("    <artifactId>").append(project.getArtifactId()).append("</artifactId>\n");
        sb.append("    <version>1.0.0-SNAPSHOT</version>\n");
        sb.append("    <name>").append(project.getProjectName()).append("</name>\n");
        sb.append("    <description>").append(project.getDescription()).append("</description>\n\n");
        sb.append("    <properties>\n");
        sb.append("        <java.version>").append(project.getJavaVersion()).append("</java.version>\n");
        sb.append("        <maven.compiler.source>").append(project.getJavaVersion()).append("</maven.compiler.source>\n");
        sb.append("        <maven.compiler.target>").append(project.getJavaVersion()).append("</maven.compiler.target>\n");
        sb.append("        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n");
        sb.append("        <lombok.version>1.18.34</lombok.version>\n");
        sb.append("    </properties>\n\n");
        sb.append("    <dependencies>\n");
        sb.append("        <!-- Spring Web -->\n");
        sb.append("        <dependency>\n");
        sb.append("            <groupId>org.springframework.boot</groupId>\n");
        sb.append("            <artifactId>spring-boot-starter-web</artifactId>\n");
        sb.append("        </dependency>\n\n");
        sb.append("        <!-- Spring Data JPA -->\n");
        sb.append("        <dependency>\n");
        sb.append("            <groupId>org.springframework.boot</groupId>\n");
        sb.append("            <artifactId>spring-boot-starter-data-jpa</artifactId>\n");
        sb.append("        </dependency>\n\n");
        sb.append("        <!-- Spring Validation -->\n");
        sb.append("        <dependency>\n");
        sb.append("            <groupId>org.springframework.boot</groupId>\n");
        sb.append("            <artifactId>spring-boot-starter-validation</artifactId>\n");
        sb.append("        </dependency>\n\n");
        sb.append("        <!-- PostgreSQL Driver -->\n");
        sb.append("        <dependency>\n");
        sb.append("            <groupId>org.postgresql</groupId>\n");
        sb.append("            <artifactId>postgresql</artifactId>\n");
        sb.append("            <scope>runtime</scope>\n");
        sb.append("        </dependency>\n\n");
        sb.append("        <!-- Lombok -->\n");
        sb.append("        <dependency>\n");
        sb.append("            <groupId>org.projectlombok</groupId>\n");
        sb.append("            <artifactId>lombok</artifactId>\n");
        sb.append("            <optional>true</optional>\n");
        sb.append("        </dependency>\n\n");
        sb.append("        <!-- Spring Boot Starter Test -->\n");
        sb.append("        <dependency>\n");
        sb.append("            <groupId>org.springframework.boot</groupId>\n");
        sb.append("            <artifactId>spring-boot-starter-test</artifactId>\n");
        sb.append("            <scope>test</scope>\n");
        sb.append("        </dependency>\n");
        sb.append("    </dependencies>\n\n");
        sb.append("    <build>\n");
        sb.append("        <plugins>\n");
        sb.append("            <plugin>\n");
        sb.append("                <groupId>org.springframework.boot</groupId>\n");
        sb.append("                <artifactId>spring-boot-maven-plugin</artifactId>\n");
        sb.append("            </plugin>\n");
        sb.append("            <plugin>\n");
        sb.append("                <groupId>org.apache.maven.plugins</groupId>\n");
        sb.append("                <artifactId>maven-compiler-plugin</artifactId>\n");
        sb.append("                <configuration>\n");
        sb.append("                    <source>").append(project.getJavaVersion()).append("</source>\n");
        sb.append("                    <target>").append(project.getJavaVersion()).append("</target>\n");
        sb.append("                    <annotationProcessorPaths>\n");
        sb.append("                        <path>\n");
        sb.append("                            <groupId>org.projectlombok</groupId>\n");
        sb.append("                            <artifactId>lombok</artifactId>\n");
        sb.append("                            <version>${lombok.version}</version>\n");
        sb.append("                        </path>\n");
        sb.append("                    </annotationProcessorPaths>\n");
        sb.append("                </configuration>\n");
        sb.append("            </plugin>\n");
        sb.append("        </plugins>\n");
        sb.append("    </build>\n");
        sb.append("</project>\n");

        return GeneratedFile.builder()
                .relativePath("pom.xml")
                .content(sb.toString())
                .category(CategoryFileType.BUILD)
                .build();
    }

    private GeneratedFile generateApplicationYml(GeneratedProjectModel project) {
        StringBuilder sb = new StringBuilder();
        sb.append("server:\n");
        sb.append("  port: ${PORT:").append(project.getServerPort()).append("}\n\n");
        sb.append("spring:\n");
        sb.append("  application:\n");
        sb.append("    name: ").append(project.getArtifactId()).append("\n\n");
        sb.append("  datasource:\n");
        sb.append("    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:").append(project.getDatabaseName()).append("}\n");
        sb.append("    username: ${DB_USER:postgres}\n");
        sb.append("    password: ${DB_PASSWORD:postgres}\n");
        sb.append("    driver-class-name: org.postgresql.Driver\n\n");
        sb.append("  jpa:\n");
        sb.append("    database-platform: org.hibernate.dialect.PostgreSQLDialect\n");
        sb.append("    hibernate:\n");
        sb.append("      ddl-auto: ${JPA_DDL_AUTO:update}\n");
        sb.append("    show-sql: true\n");
        sb.append("    properties:\n");
        sb.append("      hibernate:\n");
        sb.append("        format_sql: true\n");
        sb.append("        default_schema: public\n");

        return GeneratedFile.builder()
                .relativePath("src/main/resources/application.yml")
                .content(sb.toString())
                .category(CategoryFileType.CONFIG)
                .build();
    }

    private GeneratedFile generateApplicationClass(GeneratedProjectModel project) {
        String className = project.getProjectName() + "Application";
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(project.getBasePackage()).append(";\n\n");
        sb.append("import org.springframework.boot.SpringApplication;\n");
        sb.append("import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n");
        sb.append("/**\n");
        sb.append(" * Punto de entrada principal de la aplicación Spring Boot.\n");
        sb.append(" * Generado automáticamente por la Plataforma CASE\n");
        sb.append(" */\n");
        sb.append("@SpringBootApplication\n");
        sb.append("public class ").append(className).append(" {\n\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        SpringApplication.run(").append(className).append(".class, args);\n");
        sb.append("    }\n");
        sb.append("}\n");

        String path = "src/main/java/" + project.getBasePackage().replace('.', '/') + "/" + className + ".java";

        return GeneratedFile.builder()
                .relativePath(path)
                .content(sb.toString())
                .category(CategoryFileType.CONFIG)
                .build();
    }

    private GeneratedFile generateDockerfile(GeneratedProjectModel project) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Etapa 1: Compilación con Maven y Eclipse Temurin JDK 21\n");
        sb.append("FROM maven:3.9.8-eclipse-temurin-21 AS build\n");
        sb.append("WORKDIR /app\n");
        sb.append("COPY pom.xml .\n");
        sb.append("RUN mvn dependency:go-offline -B\n");
        sb.append("COPY src ./src\n");
        sb.append("RUN mvn package -DskipTests -B\n\n");
        sb.append("# Etapa 2: Imagen de ejecución ligera con JRE 21\n");
        sb.append("FROM eclipse-temurin:21-jre-alpine\n");
        sb.append("WORKDIR /app\n");
        sb.append("COPY --from=build /app/target/*.jar app.jar\n");
        sb.append("EXPOSE ").append(project.getServerPort()).append("\n");
        sb.append("ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]\n");

        return GeneratedFile.builder()
                .relativePath("Dockerfile")
                .content(sb.toString())
                .category(CategoryFileType.DOCKER)
                .build();
    }

    private GeneratedFile generateDockerCompose(GeneratedProjectModel project) {
        StringBuilder sb = new StringBuilder();
        sb.append("version: '3.8'\n\n");
        sb.append("services:\n");
        sb.append("  postgres-db:\n");
        sb.append("    image: postgres:16-alpine\n");
        sb.append("    container_name: ").append(project.getArtifactId()).append("-db\n");
        sb.append("    environment:\n");
        sb.append("      POSTGRES_DB: ").append(project.getDatabaseName()).append("\n");
        sb.append("      POSTGRES_USER: postgres\n");
        sb.append("      POSTGRES_PASSWORD: postgres\n");
        sb.append("    ports:\n");
        sb.append("      - \"5432:5432\"\n");
        sb.append("    volumes:\n");
        sb.append("      - pgdata:/var/lib/postgresql/data\n\n");
        sb.append("  backend-app:\n");
        sb.append("    build: .\n");
        sb.append("    container_name: ").append(project.getArtifactId()).append("-api\n");
        sb.append("    ports:\n");
        sb.append("      - \"").append(project.getServerPort()).append(":").append(project.getServerPort()).append("\"\n");
        sb.append("    environment:\n");
        sb.append("      DB_HOST: postgres-db\n");
        sb.append("      DB_PORT: 5432\n");
        sb.append("      DB_NAME: ").append(project.getDatabaseName()).append("\n");
        sb.append("      DB_USER: postgres\n");
        sb.append("      DB_PASSWORD: postgres\n");
        sb.append("      PORT: ").append(project.getServerPort()).append("\n");
        sb.append("    depends_on:\n");
        sb.append("      - postgres-db\n\n");
        sb.append("volumes:\n");
        sb.append("  pgdata:\n");

        return GeneratedFile.builder()
                .relativePath("docker-compose.yml")
                .content(sb.toString())
                .category(CategoryFileType.DOCKER)
                .build();
    }

    private GeneratedFile generateReadme(GeneratedProjectModel project) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(project.getProjectName()).append("\n\n");
        sb.append("> ").append(project.getDescription()).append("\n\n");
        sb.append("Este proyecto ha sido generado automáticamente por la **Plataforma CASE Inteligente** a partir del diseño conceptual UML.\n\n");
        sb.append("## Tecnologías Utilizadas\n");
        sb.append("- **Java 21** LTS\n");
        sb.append("- **Spring Boot ").append(project.getSpringBootVersion()).append("** (Web, Data JPA, Validation)\n");
        sb.append("- **PostgreSQL 16** con Hibernate ORM\n");
        sb.append("- **Lombok** para código limpio y conciso\n\n");
        sb.append("## Arquitectura por Capas Generada\n");
        sb.append("```\n");
        sb.append(project.getBasePackage()).append("\n");
        sb.append(" ├── entity/       (Entidades JPA con mapeo de relaciones)\n");
        sb.append(" ├── repository/   (Interfaces Spring Data JPA)\n");
        sb.append(" ├── service/      (Lógica de negocio y mappers DTO)\n");
        sb.append(" ├── controller/   (Endpoints REST con validación @Valid)\n");
        sb.append(" └── dto/          (Request y Response DTOs desacoplados)\n");
        sb.append("```\n\n");
        sb.append("## Entidades y Endpoints REST Disponibles\n\n");

        for (GeneratedEntityModel entity : project.getEntities()) {
            sb.append("### ").append(entity.getName()).append("\n");
            sb.append("- `GET /api/v1/").append(entity.getTableName()).append("` : Listar todos\n");
            sb.append("- `GET /api/v1/").append(entity.getTableName()).append("/{id}` : Consultar por ID\n");
            sb.append("- `POST /api/v1/").append(entity.getTableName()).append("` : Crear registro\n");
            sb.append("- `PUT /api/v1/").append(entity.getTableName()).append("/{id}` : Actualizar registro\n");
            sb.append("- `DELETE /api/v1/").append(entity.getTableName()).append("/{id}` : Eliminar registro\n\n");
        }

        sb.append("## Cómo Ejecutar el Proyecto\n\n");
        sb.append("### Opción 1: Con Docker Compose (Recomendado)\n");
        sb.append("```bash\n");
        sb.append("docker-compose up --build\n");
        sb.append("```\n\n");
        sb.append("### Opción 2: Con Maven y base de datos local\n");
        sb.append("1. Asegúrate de tener PostgreSQL corriendo en el puerto 5432 y una base de datos llamada `").append(project.getDatabaseName()).append("`.\n");
        sb.append("2. Ejecuta:\n");
        sb.append("```bash\n");
        sb.append("./mvnw spring-boot:run\n");
        sb.append("```\n");
        sb.append("3. La aplicación estará disponible en `http://localhost:").append(project.getServerPort()).append("`.\n");

        return GeneratedFile.builder()
                .relativePath("README.md")
                .content(sb.toString())
                .category(CategoryFileType.DOCS)
                .build();
    }
}
