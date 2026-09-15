package com.caseplatform.controller;

import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.MetodoUMLDTO;
import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.dto.uml.ProyectoUMLDTO;
import com.caseplatform.dto.uml.RelacionUMLDTO;
import com.caseplatform.service.AtributoUMLService;
import com.caseplatform.service.ClaseUMLService;
import com.caseplatform.service.MetodoUMLService;
import com.caseplatform.service.ModeloUMLService;
import com.caseplatform.service.ProyectoUMLService;
import com.caseplatform.service.RelacionUMLService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST principal para la gestión del núcleo UML 2.5:
 * Proyectos, Modelos, Clases, Atributos, Métodos y Relaciones.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UMLController {

    private final ProyectoUMLService proyectoUMLService;
    private final ModeloUMLService modeloUMLService;
    private final ClaseUMLService claseUMLService;
    private final AtributoUMLService atributoUMLService;
    private final MetodoUMLService metodoUMLService;
    private final RelacionUMLService relacionUMLService;

    // =========================================================================
    // PROYECTOS UML
    // =========================================================================

    @PostMapping("/proyectos")
    public ResponseEntity<ProyectoUMLDTO> crearProyecto(
            @Valid @RequestBody ProyectoUMLDTO dto,
            Authentication authentication
    ) {
        log.info("Petición POST /api/proyectos de usuario: {}", authentication.getName());
        ProyectoUMLDTO creado = proyectoUMLService.crearProyecto(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/proyectos")
    public ResponseEntity<List<ProyectoUMLDTO>> listarProyectos(Authentication authentication) {
        List<ProyectoUMLDTO> proyectos = proyectoUMLService.listarProyectos(authentication.getName());
        return ResponseEntity.ok(proyectos);
    }

    @GetMapping("/proyectos/{id}")
    public ResponseEntity<ProyectoUMLDTO> obtenerProyecto(
            @PathVariable Long id,
            Authentication authentication
    ) {
        ProyectoUMLDTO proyecto = proyectoUMLService.obtenerPorId(id, authentication.getName());
        return ResponseEntity.ok(proyecto);
    }

    @PutMapping("/proyectos/{id}")
    public ResponseEntity<ProyectoUMLDTO> actualizarProyecto(
            @PathVariable Long id,
            @Valid @RequestBody ProyectoUMLDTO dto,
            Authentication authentication
    ) {
        ProyectoUMLDTO actualizado = proyectoUMLService.actualizar(id, dto, authentication.getName());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/proyectos/{id}")
    public ResponseEntity<Void> eliminarProyecto(
            @PathVariable Long id,
            Authentication authentication
    ) {
        proyectoUMLService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // MODELOS UML
    // =========================================================================

    @PostMapping("/proyectos/{proyectoId}/modelos")
    public ResponseEntity<ModeloUMLDTO> crearModelo(
            @PathVariable Long proyectoId,
            @Valid @RequestBody ModeloUMLDTO dto,
            Authentication authentication
    ) {
        ModeloUMLDTO creado = modeloUMLService.crearModelo(proyectoId, dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/proyectos/{proyectoId}/modelos")
    public ResponseEntity<List<ModeloUMLDTO>> listarModelosPorProyecto(@PathVariable Long proyectoId) {
        List<ModeloUMLDTO> modelos = modeloUMLService.listarPorProyecto(proyectoId);
        return ResponseEntity.ok(modelos);
    }

    @GetMapping("/modelos/{id}")
    public ResponseEntity<ModeloUMLDTO> obtenerModeloPorId(@PathVariable Long id) {
        ModeloUMLDTO modelo = modeloUMLService.obtenerPorId(id);
        return ResponseEntity.ok(modelo);
    }

    @DeleteMapping("/modelos/{id}")
    public ResponseEntity<Void> eliminarModelo(
            @PathVariable Long id,
            Authentication authentication
    ) {
        modeloUMLService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // CLASES UML
    // =========================================================================

    @PostMapping("/modelos/{id}/clases")
    public ResponseEntity<ClaseUMLDTO> crearClase(
            @PathVariable Long id,
            @Valid @RequestBody ClaseUMLDTO dto
    ) {
        ClaseUMLDTO creada = claseUMLService.crearClase(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/modelos/{id}/clases")
    public ResponseEntity<List<ClaseUMLDTO>> listarClasesPorModelo(@PathVariable Long id) {
        List<ClaseUMLDTO> clases = claseUMLService.listarPorModelo(id);
        return ResponseEntity.ok(clases);
    }

    @GetMapping("/clases/{id}")
    public ResponseEntity<ClaseUMLDTO> obtenerClasePorId(@PathVariable Long id) {
        ClaseUMLDTO clase = claseUMLService.obtenerPorId(id);
        return ResponseEntity.ok(clase);
    }

    @PutMapping("/clases/{id}")
    public ResponseEntity<ClaseUMLDTO> actualizarClase(
            @PathVariable Long id,
            @Valid @RequestBody ClaseUMLDTO dto
    ) {
        ClaseUMLDTO actualizada = claseUMLService.actualizar(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/clases/{id}")
    public ResponseEntity<Void> eliminarClase(@PathVariable Long id) {
        claseUMLService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // ATRIBUTOS UML
    // =========================================================================

    @PostMapping("/clases/{id}/atributos")
    public ResponseEntity<AtributoUMLDTO> agregarAtributo(
            @PathVariable Long id,
            @Valid @RequestBody AtributoUMLDTO dto
    ) {
        AtributoUMLDTO creado = atributoUMLService.agregarAtributo(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/clases/{id}/atributos")
    public ResponseEntity<List<AtributoUMLDTO>> listarAtributosPorClase(@PathVariable Long id) {
        List<AtributoUMLDTO> atributos = atributoUMLService.listarPorClase(id);
        return ResponseEntity.ok(atributos);
    }

    @DeleteMapping("/atributos/{id}")
    public ResponseEntity<Void> eliminarAtributo(@PathVariable Long id) {
        atributoUMLService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // MÉTODOS UML
    // =========================================================================

    @PostMapping("/clases/{id}/metodos")
    public ResponseEntity<MetodoUMLDTO> agregarMetodo(
            @PathVariable Long id,
            @Valid @RequestBody MetodoUMLDTO dto
    ) {
        MetodoUMLDTO creado = metodoUMLService.agregarMetodo(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/clases/{id}/metodos")
    public ResponseEntity<List<MetodoUMLDTO>> listarMetodosPorClase(@PathVariable Long id) {
        List<MetodoUMLDTO> metodos = metodoUMLService.listarPorClase(id);
        return ResponseEntity.ok(metodos);
    }

    @DeleteMapping("/metodos/{id}")
    public ResponseEntity<Void> eliminarMetodo(@PathVariable Long id) {
        metodoUMLService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // RELACIONES UML
    // =========================================================================

    @PostMapping("/relaciones")
    public ResponseEntity<RelacionUMLDTO> crearRelacion(@Valid @RequestBody RelacionUMLDTO dto) {
        RelacionUMLDTO creada = relacionUMLService.crearRelacion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/modelos/{id}/relaciones")
    public ResponseEntity<List<RelacionUMLDTO>> listarRelacionesPorModelo(@PathVariable Long id) {
        List<RelacionUMLDTO> relaciones = relacionUMLService.listarPorModelo(id);
        return ResponseEntity.ok(relaciones);
    }

    @GetMapping("/relaciones/{id}")
    public ResponseEntity<RelacionUMLDTO> obtenerRelacionPorId(@PathVariable Long id) {
        RelacionUMLDTO relacion = relacionUMLService.obtenerPorId(id);
        return ResponseEntity.ok(relacion);
    }

    @DeleteMapping("/relaciones/{id}")
    public ResponseEntity<Void> eliminarRelacion(@PathVariable Long id) {
        relacionUMLService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
