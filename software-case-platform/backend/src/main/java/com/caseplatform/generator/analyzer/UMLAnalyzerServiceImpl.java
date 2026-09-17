package com.caseplatform.generator.analyzer;

import com.caseplatform.generator.dto.GeneratorRequestDTO;
import com.caseplatform.generator.model.*;
import com.caseplatform.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UMLAnalyzerServiceImpl implements UMLAnalyzerService {

    @Override
    public GeneratedProjectModel analyze(ModeloUML modelo, GeneratorRequestDTO request) {
        if (modelo == null) {
            throw new IllegalArgumentException("El modelo UML a analizar no puede ser nulo.");
        }

        GeneratorRequestDTO config = request != null ? request : GeneratorRequestDTO.builder().build();

        String basePackage = cleanPackageName(config.getPackageName());
        String projectName = cleanPascalCase(config.getProjectName() != null && !config.getProjectName().isBlank()
                ? config.getProjectName()
                : (modelo.getNombre() != null ? modelo.getNombre() : "AppGenerated"));
        String artifactId = cleanArtifactId(config.getArtifactId() != null && !config.getArtifactId().isBlank()
                ? config.getArtifactId()
                : projectName.toLowerCase());

        GeneratedProjectModel project = GeneratedProjectModel.builder()
                .modeloId(modelo.getId())
                .projectName(projectName)
                .artifactId(artifactId)
                .groupId(config.getGroupId() != null ? config.getGroupId() : "com.caseplatform.generated")
                .basePackage(basePackage)
                .databaseName(config.getDatabaseName() != null ? config.getDatabaseName() : "app_db")
                .serverPort(config.getServerPort() != null ? config.getServerPort() : 8081)
                .description("Backend Spring Boot generado desde modelo UML: " + modelo.getNombre())
                .entities(new ArrayList<>())
                .files(new ArrayList<>())
                .build();

        Map<Long, GeneratedEntityModel> entityMap = new HashMap<>();

        // 1. Analizar Clases y Atributos
        if (modelo.getClases() != null) {
            for (ClaseUML clase : modelo.getClases()) {
                GeneratedEntityModel entity = analyzeClase(clase);
                entityMap.put(clase.getId(), entity);
                project.getEntities().add(entity);
            }
        }

        // 2. Analizar Relaciones entre entidades
        if (modelo.getRelaciones() != null) {
            for (RelacionUML relacion : modelo.getRelaciones()) {
                GeneratedEntityModel origin = entityMap.get(relacion.getClaseOrigen() != null ? relacion.getClaseOrigen().getId() : null);
                GeneratedEntityModel target = entityMap.get(relacion.getClaseDestino() != null ? relacion.getClaseDestino().getId() : null);

                if (origin != null && target != null) {
                    analyzeRelation(relacion, origin, target);
                }
            }
        }

        log.info("Análisis UML completado para modelo '{}': {} entidades preparadas.",
                modelo.getNombre(), project.getEntities().size());

        return project;
    }

    private GeneratedEntityModel analyzeClase(ClaseUML clase) {
        String entityName = cleanPascalCase(clase.getNombre());
        String tableName = toSnakeCase(entityName) + "s";

        GeneratedEntityModel entity = GeneratedEntityModel.builder()
                .name(entityName)
                .tableName(tableName)
                .description(clase.getDescripcion() != null ? clase.getDescripcion() : "Entidad " + entityName)
                .primaryKeyName("id")
                .primaryKeyType("Long")
                .fields(new ArrayList<>())
                .relations(new ArrayList<>())
                .methods(new ArrayList<>())
                .build();

        boolean hasExplicitId = false;

        if (clase.getAtributos() != null) {
            for (AtributoUML attr : clase.getAtributos()) {
                GeneratedFieldModel field = analyzeField(attr);
                if (field.isId()) {
                    hasExplicitId = true;
                    entity.setPrimaryKeyName(field.getName());
                    entity.setPrimaryKeyType(field.getJavaType());
                }
                entity.getFields().add(field);
            }
        }

        // Si la clase no definió un campo id explícito, inyectamos la clave primaria JPA estándar
        if (!hasExplicitId) {
            GeneratedFieldModel defaultId = GeneratedFieldModel.builder()
                    .name("id")
                    .javaType("Long")
                    .umlType("Long")
                    .columnName("id")
                    .isId(true)
                    .isGenerated(true)
                    .nullable(false)
                    .unique(true)
                    .build();
            entity.getFields().add(0, defaultId);
        }

        // Analizar métodos
        if (clase.getMetodos() != null) {
            for (MetodoUML metodo : clase.getMetodos()) {
                String methodName = cleanCamelCase(metodo.getNombre());
                String returnType = mapUmlTypeToJava(metodo.getTipoRetorno());
                entity.getMethods().add(GeneratedMethodModel.builder()
                        .name(methodName)
                        .returnType(returnType)
                        .parameters(metodo.getParametros() != null ? metodo.getParametros() : "")
                        .visibility(metodo.getVisibilidad() != null ? metodo.getVisibilidad().name().toLowerCase() : "public")
                        .build());
            }
        }

        return entity;
    }

    private GeneratedFieldModel analyzeField(AtributoUML attr) {
        String fieldName = cleanCamelCase(attr.getNombre());
        String javaType = mapUmlTypeToJava(attr.getTipoDato());
        String columnName = toSnakeCase(fieldName);
        boolean isId = "id".equalsIgnoreCase(fieldName) || fieldName.toLowerCase().endsWith("_id");

        return GeneratedFieldModel.builder()
                .name(fieldName)
                .javaType(javaType)
                .umlType(attr.getTipoDato())
                .columnName(columnName)
                .isId(isId)
                .isGenerated(isId)
                .nullable(attr.getVisibilidad() != VisibilidadUML.PRIVATE)
                .unique(isId)
                .initialValue(attr.getValorInicial())
                .build();
    }

    private void analyzeRelation(RelacionUML relacion, GeneratedEntityModel origin, GeneratedEntityModel target) {
        TipoRelacionUML tipo = relacion.getTipoRelacion() != null ? relacion.getTipoRelacion() : TipoRelacionUML.ASOCIACION;

        // Herencia
        if (tipo == TipoRelacionUML.HERENCIA) {
            origin.setParentEntity(target.getName());
            return;
        }

        String cardOrigen = relacion.getCardinalidadOrigen() != null ? relacion.getCardinalidadOrigen().trim() : "1";
        String cardDestino = relacion.getCardinalidadDestino() != null ? relacion.getCardinalidadDestino().trim() : "*";

        boolean originIsMany = isMany(cardOrigen);
        boolean targetIsMany = isMany(cardDestino);

        String originVarName = cleanCamelCase(origin.getName());
        String targetVarName = cleanCamelCase(target.getName());

        boolean isComposition = tipo == TipoRelacionUML.COMPOSICION;

        if (!originIsMany && targetIsMany) {
            // Uno a Muchos (Origin 1 ---- * Target)
            // En Origin: @OneToMany(mappedBy = originVarName) List<Target>
            origin.getRelations().add(GeneratedRelationModel.builder()
                    .tipoRelacionJPA(TipoRelacionJPA.ONE_TO_MANY)
                    .fieldName(targetVarName + "List")
                    .targetEntity(target.getName())
                    .mappedBy(originVarName)
                    .cascadeType(isComposition ? "CascadeType.ALL" : "CascadeType.PERSIST, CascadeType.MERGE")
                    .orphanRemoval(isComposition)
                    .cardinalidadOrigen(cardOrigen)
                    .cardinalidadDestino(cardDestino)
                    .descripcion(relacion.getDescripcion())
                    .build());

            // En Target: @ManyToOne @JoinColumn(name = origin_id)
            target.getRelations().add(GeneratedRelationModel.builder()
                    .tipoRelacionJPA(TipoRelacionJPA.MANY_TO_ONE)
                    .fieldName(originVarName)
                    .targetEntity(origin.getName())
                    .joinColumnName(toSnakeCase(originVarName) + "_id")
                    .fetchType("FetchType.LAZY")
                    .cardinalidadOrigen(cardOrigen)
                    .cardinalidadDestino(cardDestino)
                    .descripcion(relacion.getDescripcion())
                    .build());

        } else if (originIsMany && !targetIsMany) {
            // Muchos a Uno (Origin * ---- 1 Target)
            // En Origin: @ManyToOne @JoinColumn(name = target_id)
            origin.getRelations().add(GeneratedRelationModel.builder()
                    .tipoRelacionJPA(TipoRelacionJPA.MANY_TO_ONE)
                    .fieldName(targetVarName)
                    .targetEntity(target.getName())
                    .joinColumnName(toSnakeCase(targetVarName) + "_id")
                    .fetchType("FetchType.LAZY")
                    .cardinalidadOrigen(cardOrigen)
                    .cardinalidadDestino(cardDestino)
                    .descripcion(relacion.getDescripcion())
                    .build());

            // En Target: @OneToMany(mappedBy = targetVarName) List<Origin>
            target.getRelations().add(GeneratedRelationModel.builder()
                    .tipoRelacionJPA(TipoRelacionJPA.ONE_TO_MANY)
                    .fieldName(originVarName + "List")
                    .targetEntity(origin.getName())
                    .mappedBy(targetVarName)
                    .cascadeType(isComposition ? "CascadeType.ALL" : "CascadeType.PERSIST, CascadeType.MERGE")
                    .orphanRemoval(isComposition)
                    .cardinalidadOrigen(cardOrigen)
                    .cardinalidadDestino(cardDestino)
                    .descripcion(relacion.getDescripcion())
                    .build());

        } else if (originIsMany && targetIsMany) {
            // Muchos a Muchos (Origin * ---- * Target)
            origin.getRelations().add(GeneratedRelationModel.builder()
                    .tipoRelacionJPA(TipoRelacionJPA.MANY_TO_MANY)
                    .fieldName(targetVarName + "List")
                    .targetEntity(target.getName())
                    .cardinalidadOrigen(cardOrigen)
                    .cardinalidadDestino(cardDestino)
                    .descripcion(relacion.getDescripcion())
                    .build());

            target.getRelations().add(GeneratedRelationModel.builder()
                    .tipoRelacionJPA(TipoRelacionJPA.MANY_TO_MANY)
                    .fieldName(originVarName + "List")
                    .targetEntity(origin.getName())
                    .mappedBy(targetVarName + "List")
                    .cardinalidadOrigen(cardOrigen)
                    .cardinalidadDestino(cardDestino)
                    .descripcion(relacion.getDescripcion())
                    .build());

        } else {
            // Uno a Uno (Origin 1 ---- 1 Target)
            origin.getRelations().add(GeneratedRelationModel.builder()
                    .tipoRelacionJPA(TipoRelacionJPA.ONE_TO_ONE)
                    .fieldName(targetVarName)
                    .targetEntity(target.getName())
                    .joinColumnName(toSnakeCase(targetVarName) + "_id")
                    .cascadeType(isComposition ? "CascadeType.ALL" : "CascadeType.PERSIST, CascadeType.MERGE")
                    .cardinalidadOrigen(cardOrigen)
                    .cardinalidadDestino(cardDestino)
                    .descripcion(relacion.getDescripcion())
                    .build());

            target.getRelations().add(GeneratedRelationModel.builder()
                    .tipoRelacionJPA(TipoRelacionJPA.ONE_TO_ONE)
                    .fieldName(originVarName)
                    .targetEntity(origin.getName())
                    .mappedBy(targetVarName)
                    .cardinalidadOrigen(cardOrigen)
                    .cardinalidadDestino(cardDestino)
                    .descripcion(relacion.getDescripcion())
                    .build());
        }
    }

    private boolean isMany(String cardinality) {
        if (cardinality == null) return false;
        String c = cardinality.trim();
        return c.contains("*") || c.contains("N") || c.contains("n") || c.endsWith("..*");
    }

    public static String mapUmlTypeToJava(String umlType) {
        if (umlType == null || umlType.isBlank()) {
            return "String";
        }
        String clean = umlType.trim().toLowerCase();
        return switch (clean) {
            case "int", "integer", "entero" -> "Integer";
            case "long" -> "Long";
            case "double", "float", "decimal", "real" -> "Double";
            case "bigdecimal", "money", "moneda", "precio" -> "java.math.BigDecimal";
            case "bool", "boolean", "booleano" -> "Boolean";
            case "date", "fecha" -> "java.time.LocalDate";
            case "datetime", "timestamp", "fechahora" -> "java.time.LocalDateTime";
            case "byte[]", "bytes", "blob", "binary" -> "byte[]";
            case "uuid" -> "java.util.UUID";
            case "void" -> "void";
            default -> {
                // Si es un tipo estándar de Java con mayúscula, respetarlo
                if (Character.isUpperCase(umlType.charAt(0))) {
                    yield umlType.trim();
                }
                yield "String";
            }
        };
    }

    private String cleanPackageName(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            return "com.caseplatform.generated";
        }
        return packageName.trim().toLowerCase().replaceAll("[^a-z0-9_.]", "");
    }

    private String cleanArtifactId(String artifactId) {
        return artifactId.trim().toLowerCase().replaceAll("[^a-z0-9-]", "-");
    }

    public static String cleanPascalCase(String name) {
        if (name == null || name.isBlank()) return "GeneratedClass";
        String cleaned = name.replaceAll("[^a-zA-Z0-9]", " ").trim();
        StringBuilder sb = new StringBuilder();
        for (String word : cleaned.split("\\s+")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1));
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : "GeneratedClass";
    }

    public static String cleanCamelCase(String name) {
        String pascal = cleanPascalCase(name);
        if (pascal.isEmpty()) return "item";
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }

    public static String toSnakeCase(String name) {
        if (name == null) return "";
        return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
