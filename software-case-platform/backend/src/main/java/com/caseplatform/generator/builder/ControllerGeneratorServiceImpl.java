package com.caseplatform.generator.builder;

import com.caseplatform.generator.model.CategoryFileType;
import com.caseplatform.generator.model.GeneratedEntityModel;
import com.caseplatform.generator.model.GeneratedFile;
import org.springframework.stereotype.Service;

@Service
public class ControllerGeneratorServiceImpl implements ControllerGeneratorService {

    @Override
    public GeneratedFile generateController(String basePackage, GeneratedEntityModel entity) {
        String controllerPackage = basePackage + ".controller";
        String controllerName = entity.getName() + "Controller";
        String serviceName = entity.getName() + "Service";
        String reqDto = entity.getName() + "RequestDTO";
        String resDto = entity.getName() + "ResponseDTO";
        String varService = Character.toLowerCase(serviceName.charAt(0)) + serviceName.substring(1);
        String endpointPath = "/api/v1/" + entity.getTableName();

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(controllerPackage).append(";\n\n");
        sb.append("import ").append(basePackage).append(".dto.").append(reqDto).append(";\n");
        sb.append("import ").append(basePackage).append(".dto.").append(resDto).append(";\n");
        sb.append("import ").append(basePackage).append(".service.").append(serviceName).append(";\n");
        sb.append("import jakarta.validation.Valid;\n");
        sb.append("import lombok.RequiredArgsConstructor;\n");
        sb.append("import lombok.extern.slf4j.Slf4j;\n");
        sb.append("import org.springframework.http.HttpStatus;\n");
        sb.append("import org.springframework.http.ResponseEntity;\n");
        sb.append("import org.springframework.web.bind.annotation.*;\n");
        sb.append("import java.util.List;\n\n");

        sb.append("/**\n");
        sb.append(" * Controlador REST profesional para operaciones sobre ").append(entity.getName()).append("\n");
        sb.append(" * Expone endpoints bajo ").append(endpointPath).append("\n");
        sb.append(" */\n");
        sb.append("@Slf4j\n");
        sb.append("@RestController\n");
        sb.append("@RequestMapping(\"").append(endpointPath).append("\")\n");
        sb.append("@RequiredArgsConstructor\n");
        sb.append("@CrossOrigin(origins = \"*\")\n");
        sb.append("public class ").append(controllerName).append(" {\n\n");

        sb.append("    private final ").append(serviceName).append(" ").append(varService).append(";\n\n");

        // GET all
        sb.append("    @GetMapping\n");
        sb.append("    public ResponseEntity<List<").append(resDto).append(">> getAll() {\n");
        sb.append("        log.info(\"GET ").append(endpointPath).append(" - listar todos\");\n");
        sb.append("        return ResponseEntity.ok(").append(varService).append(".findAll());\n");
        sb.append("    }\n\n");

        // GET by ID
        sb.append("    @GetMapping(\"/{id}\")\n");
        sb.append("    public ResponseEntity<").append(resDto).append("> getById(@PathVariable ").append(entity.getPrimaryKeyType()).append(" id) {\n");
        sb.append("        log.info(\"GET ").append(endpointPath).append("/{} - obtener por id\", id);\n");
        sb.append("        return ResponseEntity.ok(").append(varService).append(".findById(id));\n");
        sb.append("    }\n\n");

        // POST create
        sb.append("    @PostMapping\n");
        sb.append("    public ResponseEntity<").append(resDto).append("> create(@Valid @RequestBody ").append(reqDto).append(" request) {\n");
        sb.append("        log.info(\"POST ").append(endpointPath).append(" - crear nuevo\");\n");
        sb.append("        return ResponseEntity.status(HttpStatus.CREATED).body(").append(varService).append(".create(request));\n");
        sb.append("    }\n\n");

        // PUT update
        sb.append("    @PutMapping(\"/{id}\")\n");
        sb.append("    public ResponseEntity<").append(resDto).append("> update(@PathVariable ").append(entity.getPrimaryKeyType()).append(" id, @Valid @RequestBody ").append(reqDto).append(" request) {\n");
        sb.append("        log.info(\"PUT ").append(endpointPath).append("/{} - actualizar\", id);\n");
        sb.append("        return ResponseEntity.ok(").append(varService).append(".update(id, request));\n");
        sb.append("    }\n\n");

        // DELETE delete
        sb.append("    @DeleteMapping(\"/{id}\")\n");
        sb.append("    public ResponseEntity<Void> delete(@PathVariable ").append(entity.getPrimaryKeyType()).append(" id) {\n");
        sb.append("        log.info(\"DELETE ").append(endpointPath).append("/{} - eliminar\", id);\n");
        sb.append("        ").append(varService).append(".delete(id);\n");
        sb.append("        return ResponseEntity.noContent().build();\n");
        sb.append("    }\n");

        sb.append("}\n");

        String path = "src/main/java/" + controllerPackage.replace('.', '/') + "/" + controllerName + ".java";

        return GeneratedFile.builder()
                .relativePath(path)
                .content(sb.toString())
                .category(CategoryFileType.CONTROLLER)
                .build();
    }
}
