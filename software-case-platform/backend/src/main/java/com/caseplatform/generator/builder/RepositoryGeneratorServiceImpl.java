package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.CategoryFileType;
import com.caseplatform.generator.model.GeneratedEntityModel;
import com.caseplatform.generator.model.GeneratedFieldModel;
import com.caseplatform.generator.model.GeneratedFile;
import org.springframework.stereotype.Service;

@Service
public class RepositoryGeneratorServiceImpl implements RepositoryGeneratorService {

    @Override
    public GeneratedFile generateRepository(String basePackage, GeneratedEntityModel entity) {
        String repoPackage = basePackage + ".repository";
        String entityPackage = basePackage + ".entity";
        String repoName = entity.getName() + "Repository";

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(repoPackage).append(";\n\n");
        sb.append("import ").append(entityPackage).append(".").append(entity.getName()).append(";\n");
        sb.append("import org.springframework.data.jpa.repository.JpaRepository;\n");
        sb.append("import org.springframework.stereotype.Repository;\n");
        sb.append("import java.util.Optional;\n");
        sb.append("import java.util.List;\n\n");

        sb.append("/**\n");
        sb.append(" * Repositorio Spring Data JPA para la entidad ").append(entity.getName()).append("\n");
        sb.append(" */\n");
        sb.append("@Repository\n");
        sb.append("public interface ").append(repoName).append(" extends JpaRepository<")
                .append(entity.getName()).append(", ").append(entity.getPrimaryKeyType()).append("> {\n\n");

        // Métodos de búsqueda especializados basados en los atributos
        for (GeneratedFieldModel field : entity.getFields()) {
            if (field.isId()) continue;

            String fNameCap = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            if ("email".equalsIgnoreCase(field.getName())) {
                sb.append("    Optional<").append(entity.getName()).append("> findByEmail(String email);\n");
                sb.append("    boolean existsByEmail(String email);\n\n");
            } else if ("codigo".equalsIgnoreCase(field.getName())) {
                sb.append("    Optional<").append(entity.getName()).append("> findByCodigo(").append(field.getJavaType()).append(" codigo);\n\n");
            } else if ("nombre".equalsIgnoreCase(field.getName()) && "String".equals(field.getJavaType())) {
                sb.append("    List<").append(entity.getName()).append("> findByNombreContainingIgnoreCase(String nombre);\n\n");
            }
        }

        sb.append("}\n");

        String path = "src/main/java/" + repoPackage.replace('.', '/') + "/" + repoName + ".java";

        return GeneratedFile.builder()
                .relativePath(path)
                .content(sb.toString())
                .category(CategoryFileType.REPOSITORY)
                .build();
    }
}
