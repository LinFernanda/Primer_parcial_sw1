package com.caseplatform.service.impl;

import com.caseplatform.dto.uml.MetodoUMLDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.MetodoUML;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.ClaseUMLRepository;
import com.caseplatform.repository.MetodoUMLRepository;
import com.caseplatform.service.MetodoUMLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetodoUMLServiceImpl implements MetodoUMLService {

    private final MetodoUMLRepository metodoUMLRepository;
    private final ClaseUMLRepository claseUMLRepository;

    @Override
    @Transactional
    public MetodoUMLDTO agregarMetodo(Long claseId, MetodoUMLDTO dto) {
        log.info("Agregando método '{}' a la clase ID: {}", dto.getNombre(), claseId);

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new ValidationException("El nombre del método es obligatorio");
        }

        ClaseUML clase = claseUMLRepository.findById(claseId)
                .orElseThrow(() -> new ResourceNotFoundException("Clase UML no encontrada con ID: " + claseId));

        MetodoUML metodo = MetodoUML.builder()
                .nombre(dto.getNombre().trim())
                .tipoRetorno(dto.getTipoRetorno() != null && !dto.getTipoRetorno().isBlank() ? dto.getTipoRetorno().trim() : "void")
                .visibilidad(dto.getVisibilidad() != null ? dto.getVisibilidad() : VisibilidadUML.PUBLIC)
                .parametros(dto.getParametros())
                .claseUML(clase)
                .build();

        MetodoUML guardado = metodoUMLRepository.save(metodo);
        log.info("Método UML guardado con ID: {}", guardado.getId());

        return mapToDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetodoUMLDTO> listarPorClase(Long claseId) {
        return metodoUMLRepository.findByClaseUMLId(claseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!metodoUMLRepository.existsById(id)) {
            throw new ResourceNotFoundException("Método UML no encontrado con ID: " + id);
        }
        metodoUMLRepository.deleteById(id);
        log.info("Método UML eliminado con ID: {}", id);
    }

    private MetodoUMLDTO mapToDTO(MetodoUML m) {
        return MetodoUMLDTO.builder()
                .id(m.getId())
                .nombre(m.getNombre())
                .tipoRetorno(m.getTipoRetorno())
                .visibilidad(m.getVisibilidad())
                .parametros(m.getParametros())
                .claseId(m.getClaseUML() != null ? m.getClaseUML().getId() : null)
                .build();
    }
}
