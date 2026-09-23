package com.caseplatform.service.impl;

import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.dto.uml.ProyectoUMLDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.ProyectoUML;
import com.caseplatform.model.Rol;
import com.caseplatform.model.Usuario;
import com.caseplatform.repository.ModeloUMLRepository;
import com.caseplatform.repository.ProyectoUMLRepository;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.service.ProyectoUMLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProyectoUMLServiceImpl implements ProyectoUMLService {

    private final ProyectoUMLRepository proyectoUMLRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModeloUMLRepository modeloUMLRepository;

    @Override
    @Transactional
    public ProyectoUMLDTO crearProyecto(ProyectoUMLDTO dto, String usuarioEmail) {
        log.info("Creando nuevo proyecto UML: '{}' para el usuario: {}", dto.getNombre(), usuarioEmail);

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new ValidationException("El nombre del proyecto UML no puede estar vacío");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(usuarioEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + usuarioEmail));

        ProyectoUML proyecto = ProyectoUML.builder()
                .nombre(dto.getNombre().trim())
                .descripcion(dto.getDescripcion())
                .usuarioPropietario(usuario)
                .estado(dto.getEstado() != null ? dto.getEstado() : "ACTIVO")
                .build();

        ProyectoUML guardado = proyectoUMLRepository.save(proyecto);

        // Inicializa automáticamente un modelo UML inicial (versión 1.0) para el proyecto
        ModeloUML modeloInicial = ModeloUML.builder()
                .nombre("Diagrama Conceptual Inicial")
                .version("1.0")
                .proyecto(guardado)
                .build();
        modeloUMLRepository.save(modeloInicial);

        guardado.getModelos().add(modeloInicial);
        log.info("Proyecto UML creado con ID: {} y modelo inicial ID: {}", guardado.getId(), modeloInicial.getId());

        return mapToDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoUMLDTO> listarProyectos(String usuarioEmail) {
        log.info("Listando proyectos colaborativos para el usuario: {}", usuarioEmail);
        // Administrador y Arquitecto (así como Ingeniero de Software) tienen acceso
        // colaborativo completo a los mismos proyectos. Cuando uno crea un proyecto,
        // al otro le aparece inmediatamente para visualizarlo y editarlo.
        return proyectoUMLRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProyectoUMLDTO obtenerPorId(Long id, String usuarioEmail) {
        ProyectoUML proyecto = proyectoUMLRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto UML no encontrado con ID: " + id));

        return mapToDTO(proyecto);
    }

    @Override
    @Transactional
    public ProyectoUMLDTO actualizar(Long id, ProyectoUMLDTO dto, String usuarioEmail) {
        ProyectoUML proyecto = proyectoUMLRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto UML no encontrado con ID: " + id));

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            proyecto.setNombre(dto.getNombre().trim());
        }
        if (dto.getDescripcion() != null) {
            proyecto.setDescripcion(dto.getDescripcion());
        }
        if (dto.getEstado() != null) {
            proyecto.setEstado(dto.getEstado());
        }

        ProyectoUML actualizado = proyectoUMLRepository.save(proyecto);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id, String usuarioEmail) {
        if (!proyectoUMLRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proyecto UML no encontrado con ID: " + id);
        }
        proyectoUMLRepository.deleteById(id);
        log.info("Proyecto UML eliminado con ID: {}", id);
    }

    private ProyectoUMLDTO mapToDTO(ProyectoUML p) {
        List<ModeloUMLDTO> modelosDTO = p.getModelos() != null
                ? p.getModelos().stream().map(m -> ModeloUMLDTO.builder()
                .id(m.getId())
                .nombre(m.getNombre())
                .version(m.getVersion())
                .fechaCreacion(m.getFechaCreacion())
                .fechaActualizacion(m.getFechaActualizacion())
                .proyectoId(p.getId())
                .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return ProyectoUMLDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .fechaCreacion(p.getFechaCreacion())
                .fechaActualizacion(p.getFechaActualizacion())
                .usuarioPropietarioId(p.getUsuarioPropietario() != null ? p.getUsuarioPropietario().getId() : null)
                .usuarioPropietarioEmail(p.getUsuarioPropietario() != null ? p.getUsuarioPropietario().getEmail() : null)
                .estado(p.getEstado())
                .modelos(modelosDTO)
                .build();
    }
}
