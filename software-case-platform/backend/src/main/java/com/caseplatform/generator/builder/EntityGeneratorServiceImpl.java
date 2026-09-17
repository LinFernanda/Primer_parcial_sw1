package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.*;
import org.springframework.stereotype.Service;

@Service
public class EntityGeneratorServiceImpl implements EntityGeneratorService {

    @Override
    public GeneratedFile generateEntity(String basePackage, GeneratedEntityModel entity) {
        String entityPackage = basePackage + ".entity";
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(entityPackage).append(";\n\n");
        sb.append("import jakarta.persistence.*;\n");
        sb.append("import lombok.*;\n");
        sb.append("import java.time.*;\n");
        sb.append("import java.math.*;\n");
        sb.append("import java.util.*;\n\n");

        if (entity.getDescription() != null && !entity.getDescription().isBlank()) {
            sb.append("/**\n");
            sb.append(" * ").append(entity.getDescription()).append("\n");
            sb.append(" * Generado automáticamente por la Plataforma CASE\n");
            sb.append(" */\n");
        }

        sb.append("@Data\n");
        sb.append("@Builder\n");
        sb.append("@NoArgsConstructor\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("@Entity\n");
        sb.append("@Table(name = \"").append(entity.getTableName()).append("\")\n");

        sb.append("public class ").append(entity.getName());
        if (entity.getParentEntity() != null && !entity.getParentEntity().isBlank()) {
            sb.append(" extends ").append(entity.getParentEntity());
        }
        sb.append(" {\n\n");

        // 1. Campos de atributos propios
        for (GeneratedFieldModel field : entity.getFields()) {
            if (field.isId()) {
                sb.append("    @Id\n");
                if (field.isGenerated()) {
                    sb.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
                }
            }
            sb.append("    @Column(name = \"").append(field.getColumnName()).append("\"");
            if (!field.isNullable()) {
                sb.append(", nullable = false");
            }
            if (field.isUnique()) {
                sb.append(", unique = true");
            }
            sb.append(")\n");
            sb.append("    private ").append(field.getJavaType()).append(" ").append(field.getName()).append(";\n\n");
        }

        // 2. Relaciones JPA
        for (GeneratedRelationModel rel : entity.getRelations()) {
            switch (rel.getTipoRelacionJPA()) {
                case ONE_TO_MANY -> {
                    sb.append("    @OneToMany(");
                    if (rel.getMappedBy() != null && !rel.getMappedBy().isBlank()) {
                        sb.append("mappedBy = \"").append(rel.getMappedBy()).append("\", ");
                    }
                    sb.append("cascade = ").append(rel.getCascadeType() != null ? rel.getCascadeType() : "CascadeType.ALL");
                    if (rel.isOrphanRemoval()) {
                        sb.append(", orphanRemoval = true");
                    }
                    sb.append(")\n");
                    sb.append("    @Builder.Default\n");
                    sb.append("    @ToString.Exclude\n");
                    sb.append("    @EqualsAndHashCode.Exclude\n");
                    sb.append("    private List<").append(rel.getTargetEntity()).append("> ")
                            .append(rel.getFieldName()).append(" = new ArrayList<>();\n\n");
                }
                case MANY_TO_ONE -> {
                    sb.append("    @ManyToOne(fetch = FetchType.LAZY)\n");
                    sb.append("    @JoinColumn(name = \"").append(rel.getJoinColumnName()).append("\")\n");
                    sb.append("    @ToString.Exclude\n");
                    sb.append("    @EqualsAndHashCode.Exclude\n");
                    sb.append("    private ").append(rel.getTargetEntity()).append(" ").append(rel.getFieldName()).append(";\n\n");
                }
                case ONE_TO_ONE -> {
                    sb.append("    @OneToOne(");
                    if (rel.getMappedBy() != null && !rel.getMappedBy().isBlank()) {
                        sb.append("mappedBy = \"").append(rel.getMappedBy()).append("\"");
                    } else {
                        sb.append("cascade = CascadeType.ALL");
                    }
                    sb.append(")\n");
                    if (rel.getJoinColumnName() != null && !rel.getJoinColumnName().isBlank()) {
                        sb.append("    @JoinColumn(name = \"").append(rel.getJoinColumnName()).append("\")\n");
                    }
                    sb.append("    @ToString.Exclude\n");
                    sb.append("    @EqualsAndHashCode.Exclude\n");
                    sb.append("    private ").append(rel.getTargetEntity()).append(" ").append(rel.getFieldName()).append(";\n\n");
                }
                case MANY_TO_MANY -> {
                    sb.append("    @ManyToMany");
                    if (rel.getMappedBy() != null && !rel.getMappedBy().isBlank()) {
                        sb.append("(mappedBy = \"").append(rel.getMappedBy()).append("\")\n");
                    } else {
                        sb.append("\n    @JoinTable(\n");
                        sb.append("        name = \"").append(entity.getTableName()).append("_").append(rel.getFieldName()).append("\",\n");
                        sb.append("        joinColumns = @JoinColumn(name = \"").append(entity.getName().toLowerCase()).append("_id\"),\n");
                        sb.append("        inverseJoinColumns = @JoinColumn(name = \"").append(rel.getTargetEntity().toLowerCase()).append("_id\")\n");
                        sb.append("    )\n");
                    }
                    sb.append("    @Builder.Default\n");
                    sb.append("    @ToString.Exclude\n");
                    sb.append("    @EqualsAndHashCode.Exclude\n");
                    sb.append("    private List<").append(rel.getTargetEntity()).append("> ")
                            .append(rel.getFieldName()).append(" = new ArrayList<>();\n\n");
                }
                default -> {}
            }
        }

        sb.append("}\n");

        String path = "src/main/java/" + entityPackage.replace('.', '/') + "/" + entity.getName() + ".java";

        return GeneratedFile.builder()
                .relativePath(path)
                .content(sb.toString())
                .category(CategoryFileType.ENTITY)
                .build();
    }
}
