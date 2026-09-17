package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DTOGeneratorServiceImpl implements DTOGeneratorService {

    @Override
    public List<GeneratedFile> generateDTOs(String basePackage, GeneratedEntityModel entity) {
        List<GeneratedFile> files = new ArrayList<>();
        String dtoPackage = basePackage + ".dto";

        // 1. Request DTO
        files.add(generateRequestDTO(dtoPackage, entity));

        // 2. Response DTO
        files.add(generateResponseDTO(dtoPackage, entity));

        return files;
    }

    private GeneratedFile generateRequestDTO(String dtoPackage, GeneratedEntityModel entity) {
        String className = entity.getName() + "RequestDTO";
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(dtoPackage).append(";\n\n");
        sb.append("import jakarta.validation.constraints.*;\n");
        sb.append("import lombok.*;\n");
        sb.append("import java.time.*;\n");
        sb.append("import java.math.*;\n");
        sb.append("import java.util.*;\n\n");

        sb.append("/**\n");
        sb.append(" * DTO de entrada para creación y actualización de ").append(entity.getName()).append("\n");
        sb.append(" */\n");
        sb.append("@Data\n");
        sb.append("@Builder\n");
        sb.append("@NoArgsConstructor\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("public class ").append(className).append(" {\n\n");

        for (GeneratedFieldModel field : entity.getFields()) {
            if (field.isId()) continue; // El ID se gestiona automáticamente

            if (!field.isNullable()) {
                if ("String".equals(field.getJavaType())) {
                    sb.append("    @NotBlank(message = \"El campo '").append(field.getName()).append("' no puede estar vacío\")\n");
                } else {
                    sb.append("    @NotNull(message = \"El campo '").append(field.getName()).append("' es requerido\")\n");
                }
            }

            if ("email".equalsIgnoreCase(field.getName())) {
                sb.append("    @Email(message = \"Formato de correo electrónico inválido\")\n");
            }

            sb.append("    private ").append(field.getJavaType()).append(" ").append(field.getName()).append(";\n\n");
        }

        // Claves foráneas para relaciones ManyToOne o OneToOne
        for (GeneratedRelationModel rel : entity.getRelations()) {
            if (rel.getTipoRelacionJPA() == TipoRelacionJPA.MANY_TO_ONE ||
               (rel.getTipoRelacionJPA() == TipoRelacionJPA.ONE_TO_ONE && rel.getJoinColumnName() != null)) {
                String idFieldName = rel.getFieldName() + "Id";
                sb.append("    private Long ").append(idFieldName).append(";\n\n");
            }
        }

        sb.append("}\n");

        String path = "src/main/java/" + dtoPackage.replace('.', '/') + "/" + className + ".java";

        return GeneratedFile.builder()
                .relativePath(path)
                .content(sb.toString())
                .category(CategoryFileType.DTO)
                .build();
    }

    private GeneratedFile generateResponseDTO(String dtoPackage, GeneratedEntityModel entity) {
        String className = entity.getName() + "ResponseDTO";
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(dtoPackage).append(";\n\n");
        sb.append("import lombok.*;\n");
        sb.append("import java.time.*;\n");
        sb.append("import java.math.*;\n");
        sb.append("import java.util.*;\n\n");

        sb.append("/**\n");
        sb.append(" * DTO de salida para transferir información de ").append(entity.getName()).append("\n");
        sb.append(" */\n");
        sb.append("@Data\n");
        sb.append("@Builder\n");
        sb.append("@NoArgsConstructor\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("public class ").append(className).append(" {\n\n");

        for (GeneratedFieldModel field : entity.getFields()) {
            sb.append("    private ").append(field.getJavaType()).append(" ").append(field.getName()).append(";\n");
        }
        sb.append("\n");

        // Relaciones en formato ID o resumen para evitar ciclos de serialización JSON infinitos
        for (GeneratedRelationModel rel : entity.getRelations()) {
            if (rel.getTipoRelacionJPA() == TipoRelacionJPA.MANY_TO_ONE ||
               (rel.getTipoRelacionJPA() == TipoRelacionJPA.ONE_TO_ONE && rel.getJoinColumnName() != null)) {
                sb.append("    private Long ").append(rel.getFieldName()).append("Id;\n");
            } else if (rel.getTipoRelacionJPA() == TipoRelacionJPA.ONE_TO_MANY) {
                sb.append("    @Builder.Default\n");
                sb.append("    private List<Long> ").append(rel.getFieldName()).append("Ids = new ArrayList<>();\n");
            }
        }

        sb.append("}\n");

        String path = "src/main/java/" + dtoPackage.replace('.', '/') + "/" + className + ".java";

        return GeneratedFile.builder()
                .relativePath(path)
                .content(sb.toString())
                .category(CategoryFileType.DTO)
                .build();
    }
}
