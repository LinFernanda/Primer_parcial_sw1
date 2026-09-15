package com.caseplatform.service.impl;

import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.AtributoUML;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.AtributoUMLRepository;
import com.caseplatform.repository.ClaseUMLRepository;
import com.caseplatform.service.AtributoUMLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtributoUMLServiceImpl implements AtributoUMLService {

    private final AtributoUMLRepository atributoUMLRepository;
    private final ClaseUMLRepository claseUMLRepository;

    @Override
    @Transactional
    public AtributoUMLDTO agregarAtributo(Long claseId, AtributoUMLDTO dto) {
        log.info("Agregando atributo '{}' a la clase ID: {}", dto.getNombre(), claseId);

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new ValidationException("El nombre del atributo es obligatorio");
        }
        if (dto.getTipoDato() == null || dto.getTipoDato().isBlank()) {
            throw new ValidationException("El tipo de dato del atributo es obligatorio");
        }

        String nombreLimpio = dto.getNombre().trim();

        if (atributoUMLRepository.existsByClaseUMLIdAndNombreIgnoreCase(claseId, nombreLimpio)) {
            throw new ValidationException("Ya existe un atributo con el nombre '" + nombreLimpio + "' en esta clase");
        }

        ClaseUML clase = claseUMLRepository.findById(claseId)
                .orElseThrow(() -> new ResourceNotFoundException("Clase UML no encontrada con ID: " + claseId));

        AtributoUML atributo = AtributoUML.builder()
                .nombre(nombreLimpio)
                .tipoDato(dto.getTipoDato().trim())
                .visibilidad(dto.getVisibilidad() != null ? dto.getVisibilidad() : VisibilidadUML.PRIVATE)
                .valorInicial(dto.getValorInicial())
                .claseUML(clase)
                .build();

        AtributoUML guardado = atributoUMLRepository.save(atributo);
        log.info("Atributo UML guardado con ID: {}", guardado.getId());

        return mapToDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtributoUMLDTO> listarPorClase(Long claseId) {
        return atributoUMLRepository.findByClaseUMLId(claseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!atributoUMLRepository.existsById(id)) {
            throw new ResourceNotFoundException("Atributo UML no encontrado con ID: " + id);
        }
        atributoUMLRepository.deleteById(id);
        log.info("Atributo UML eliminado con ID: {}", id);
    }

    private AtributoUMLDTO mapToDTO(AtributoUML a) {
        return AtributoUMLDTO.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .tipoDato(a.getTipoDato())
                .visibilidad(a.getVisibilidad())
                .valorInicial(a.getValorInicial())
                .claseId(a.getClaseUML() != null ? a.getClaseUML().getId() : null)
                .build();
    }
}
