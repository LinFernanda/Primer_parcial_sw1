package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceGeneratorServiceImpl implements ServiceGeneratorService {

    @Override
    public List<GeneratedFile> generateService(String basePackage, GeneratedEntityModel entity) {
        List<GeneratedFile> files = new ArrayList<>();
        String servicePackage = basePackage + ".service";
        String implPackage = basePackage + ".service.impl";

        // 1. Service Interface
        files.add(generateServiceInterface(servicePackage, basePackage, entity));

        // 2. Service Implementation
        files.add(generateServiceImpl(implPackage, servicePackage, basePackage, entity));

        return files;
    }

    private GeneratedFile generateServiceInterface(String servicePackage, String basePackage, GeneratedEntityModel entity) {
        String interfaceName = entity.getName() + "Service";
        String reqDto = entity.getName() + "RequestDTO";
        String resDto = entity.getName() + "ResponseDTO";

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(servicePackage).append(";\n\n");
        sb.append("import ").append(basePackage).append(".dto.").append(reqDto).append(";\n");
        sb.append("import ").append(basePackage).append(".dto.").append(resDto).append(";\n");
        sb.append("import java.util.List;\n\n");

        sb.append("/**\n");
        sb.append(" * Contrato de servicio para operaciones de negocio sobre ").append(entity.getName()).append("\n");
        sb.append(" */\n");
        sb.append("public interface ").append(interfaceName).append(" {\n\n");
        sb.append("    List<").append(resDto).append("> findAll();\n\n");
        sb.append("    ").append(resDto).append(" findById(").append(entity.getPrimaryKeyType()).append(" id);\n\n");
        sb.append("    ").append(resDto).append(" create(").append(reqDto).append(" request);\n\n");
        sb.append("    ").append(resDto).append(" update(").append(entity.getPrimaryKeyType()).append(" id, ").append(reqDto).append(" request);\n\n");
        sb.append("    void delete(").append(entity.getPrimaryKeyType()).append(" id);\n");
        sb.append("}\n");

        String path = "src/main/java/" + servicePackage.replace('.', '/') + "/" + interfaceName + ".java";

        return GeneratedFile.builder()
                .relativePath(path)
                .content(sb.toString())
                .category(CategoryFileType.SERVICE)
                .build();
    }

    private GeneratedFile generateServiceImpl(String implPackage, String servicePackage, String basePackage, GeneratedEntityModel entity) {
        String serviceName = entity.getName() + "Service";
        String implName = entity.getName() + "ServiceImpl";
        String repoName = entity.getName() + "Repository";
        String reqDto = entity.getName() + "RequestDTO";
        String resDto = entity.getName() + "ResponseDTO";
        String varRepo = Character.toLowerCase(repoName.charAt(0)) + repoName.substring(1);
        String varEntity = Character.toLowerCase(entity.getName().charAt(0)) + entity.getName().substring(1);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(implPackage).append(";\n\n");
        sb.append("import ").append(servicePackage).append(".").append(serviceName).append(";\n");
        sb.append("import ").append(basePackage).append(".entity.").append(entity.getName()).append(";\n");
        sb.append("import ").append(basePackage).append(".repository.").append(repoName).append(";\n");
        sb.append("import ").append(basePackage).append(".dto.").append(reqDto).append(";\n");
        sb.append("import ").append(basePackage).append(".dto.").append(resDto).append(";\n");
        sb.append("import lombok.RequiredArgsConstructor;\n");
        sb.append("import lombok.extern.slf4j.Slf4j;\n");
        sb.append("import org.springframework.stereotype.Service;\n");
        sb.append("import org.springframework.transaction.annotation.Transactional;\n");
        sb.append("import java.util.List;\n");
        sb.append("import java.util.stream.Collectors;\n\n");

        sb.append("@Slf4j\n");
        sb.append("@Service\n");
        sb.append("@RequiredArgsConstructor\n");
        sb.append("@Transactional(readOnly = true)\n");
        sb.append("public class ").append(implName).append(" implements ").append(serviceName).append(" {\n\n");
        sb.append("    private final ").append(repoName).append(" ").append(varRepo).append(";\n\n");

        // findAll()
        sb.append("    @Override\n");
        sb.append("    public List<").append(resDto).append("> findAll() {\n");
        sb.append("        log.debug(\"Consultando todos los registros de ").append(entity.getName()).append("\");\n");
        sb.append("        return ").append(varRepo).append(".findAll().stream()\n");
        sb.append("                .map(this::mapToResponseDTO)\n");
        sb.append("                .collect(Collectors.toList());\n");
        sb.append("    }\n\n");

        // findById()
        sb.append("    @Override\n");
        sb.append("    public ").append(resDto).append(" findById(").append(entity.getPrimaryKeyType()).append(" id) {\n");
        sb.append("        log.debug(\"Buscando ").append(entity.getName()).append(" por ID: {}\", id);\n");
        sb.append("        ").append(entity.getName()).append(" entity = ").append(varRepo).append(".findById(id)\n");
        sb.append("                .orElseThrow(() -> new RuntimeException(\"No se encontró ").append(entity.getName()).append(" con ID: \" + id));\n");
        sb.append("        return mapToResponseDTO(entity);\n");
        sb.append("    }\n\n");

        // create()
        sb.append("    @Override\n");
        sb.append("    @Transactional\n");
        sb.append("    public ").append(resDto).append(" create(").append(reqDto).append(" request) {\n");
        sb.append("        log.info(\"Creando nuevo registro de ").append(entity.getName()).append("\");\n");
        sb.append("        ").append(entity.getName()).append(" entity = mapToEntity(request);\n");
        sb.append("        ").append(entity.getName()).append(" saved = ").append(varRepo).append(".save(entity);\n");
        sb.append("        return mapToResponseDTO(saved);\n");
        sb.append("    }\n\n");

        // update()
        sb.append("    @Override\n");
        sb.append("    @Transactional\n");
        sb.append("    public ").append(resDto).append(" update(").append(entity.getPrimaryKeyType()).append(" id, ").append(reqDto).append(" request) {\n");
        sb.append("        log.info(\"Actualizando ").append(entity.getName()).append(" con ID: {}\", id);\n");
        sb.append("        ").append(entity.getName()).append(" existing = ").append(varRepo).append(".findById(id)\n");
        sb.append("                .orElseThrow(() -> new RuntimeException(\"No se encontró ").append(entity.getName()).append(" con ID: \" + id));\n\n");

        for (GeneratedFieldModel field : entity.getFields()) {
            if (field.isId()) continue;
            String cap = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            sb.append("        if (request.get").append(cap).append("() != null) {\n");
            sb.append("            existing.set").append(cap).append("(request.get").append(cap).append("());\n");
            sb.append("        }\n");
        }

        sb.append("\n        ").append(entity.getName()).append(" updated = ").append(varRepo).append(".save(existing);\n");
        sb.append("        return mapToResponseDTO(updated);\n");
        sb.append("    }\n\n");

        // delete()
        sb.append("    @Override\n");
        sb.append("    @Transactional\n");
        sb.append("    public void delete(").append(entity.getPrimaryKeyType()).append(" id) {\n");
        sb.append("        log.info(\"Eliminando ").append(entity.getName()).append(" con ID: {}\", id);\n");
        sb.append("        if (!").append(varRepo).append(".existsById(id)) {\n");
        sb.append("            throw new RuntimeException(\"No se encontró ").append(entity.getName()).append(" con ID: \" + id);\n");
        sb.append("        }\n");
        sb.append("        ").append(varRepo).append(".deleteById(id);\n");
        sb.append("    }\n\n");

        // Mappers auxiliares
        sb.append("    private ").append(resDto).append(" mapToResponseDTO(").append(entity.getName()).append(" entity) {\n");
        sb.append("        ").append(resDto).append(" dto = ").append(resDto).append(".builder()\n");
        for (GeneratedFieldModel field : entity.getFields()) {
            String cap = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            sb.append("                .").append(field.getName()).append("(entity.get").append(cap).append("())\n");
        }
        sb.append("                .build();\n");
        sb.append("        return dto;\n");
        sb.append("    }\n\n");

        sb.append("    private ").append(entity.getName()).append(" mapToEntity(").append(reqDto).append(" request) {\n");
        sb.append("        return ").append(entity.getName()).append(".builder()\n");
        for (GeneratedFieldModel field : entity.getFields()) {
            if (field.isId()) continue;
            String cap = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            sb.append("                .").append(field.getName()).append("(request.get").append(cap).append("())\n");
        }
        sb.append("                .build();\n");
        sb.append("    }\n");

        sb.append("}\n");

        String path = "src/main/java/" + implPackage.replace('.', '/') + "/" + implName + ".java";

        return GeneratedFile.builder()
                .relativePath(path)
                .content(sb.toString())
                .category(CategoryFileType.SERVICE)
                .build();
    }
}
