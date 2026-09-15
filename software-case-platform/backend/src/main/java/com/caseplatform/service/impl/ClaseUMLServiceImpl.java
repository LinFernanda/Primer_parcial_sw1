package com.caseplatform.service.impl;

import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.MetodoUMLDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.RelacionUML;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.ClaseUMLRepository;
import com.caseplatform.repository.ModeloUMLRepository;
import com.caseplatform.repository.RelacionUMLRepository;
import com.caseplatform.service.ClaseUMLService;
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
public class ClaseUMLServiceImpl implements ClaseUMLService {

    private final ClaseUMLRepository claseUMLRepository;
    private final ModeloUMLRepository modeloUMLRepository;
    private final RelacionUMLRepository relacionUMLRepository;

    @Override
    @Transactional
    public ClaseUMLDTO crearClase(Long modeloId, ClaseUMLDTO dto) {
        log.info("Creando clase UML '{}' en el modelo ID: {}", dto.getNombre(), modeloId);

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new ValidationException("El nombre de la clase UML es obligatorio");
        }

        String nombreLimpio = dto.getNombre().trim();

        if (claseUMLRepository.existsByModeloUMLIdAndNombreIgnoreCase(modeloId, nombreLimpio)) {
            throw new ValidationException("Ya existe una clase con el nombre '" + nombreLimpio + "' en este modelo UML");
        }

        ModeloUML modelo = modeloUMLRepository.findById(modeloId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId));

        ClaseUML clase = ClaseUML.builder()
                .nombre(nombreLimpio)
                .visibilidad(dto.getVisibilidad() != null ? dto.getVisibilidad() : VisibilidadUML.PUBLIC)
                .descripcion(dto.getDescripcion())
                .posicionX(dto.getPosicionX() != null ? dto.getPosicionX() : 0.0)
                .posicionY(dto.getPosicionY() != null ? dto.getPosicionY() : 0.0)
                .modeloUML(modelo)
                .build();

        ClaseUML guardada = claseUMLRepository.save(clase);
        log.info("Clase UML creada exitosamente con ID: {}", guardada.getId());

        return mapToDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaseUMLDTO> listarPorModelo(Long modeloId) {
        return claseUMLRepository.findByModeloUMLId(modeloId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClaseUMLDTO obtenerPorId(Long id) {
        ClaseUML clase = claseUMLRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clase UML no encontrada con ID: " + id));

        return mapToDTO(clase);
    }

    @Override
    @Transactional
    public ClaseUMLDTO actualizar(Long id, ClaseUMLDTO dto) {
        ClaseUML clase = claseUMLRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clase UML no encontrada con ID: " + id));

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            String nuevoNombre = dto.getNombre().trim();
            if (!clase.getNombre().equalsIgnoreCase(nuevoNombre)) {
                if (claseUMLRepository.existsByModeloUMLIdAndNombreIgnoreCase(clase.getModeloUML().getId(), nuevoNombre)) {
                    throw new ValidationException("Ya existe una clase con el nombre '" + nuevoNombre + "' en este modelo UML");
                }
                clase.setNombre(nuevoNombre);
            }
        }

        if (dto.getVisibilidad() != null) {
            clase.setVisibilidad(dto.getVisibilidad());
        }
        if (dto.getDescripcion() != null) {
            clase.setDescripcion(dto.getDescripcion());
        }
        if (dto.getPosicionX() != null) {
            clase.setPosicionX(dto.getPosicionX());
        }
        if (dto.getPosicionY() != null) {
            clase.setPosicionY(dto.getPosicionY());
        }

        ClaseUML actualizada = claseUMLRepository.save(clase);
        return mapToDTO(actualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        ClaseUML clase = claseUMLRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clase UML no encontrada con ID: " + id));

        // Limpia relaciones conectadas con esta clase antes de eliminar
        List<RelacionUML> relacionesConectadas = relacionUMLRepository.findByClaseInvolucrada(id);
        if (!relacionesConectadas.isEmpty()) {
            relacionUMLRepository.deleteAll(relacionesConectadas);
        }

        claseUMLRepository.delete(clase);
        log.info("Clase UML eliminada con ID: {}", id);
    }

    private ClaseUMLDTO mapToDTO(ClaseUML c) {
        List<AtributoUMLDTO> attrs = c.getAtributos() != null
                ? c.getAtributos().stream().map(a -> AtributoUMLDTO.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .tipoDato(a.getTipoDato())
                .visibilidad(a.getVisibilidad())
                .valorInicial(a.getValorInicial())
                .claseId(c.getId())
                .build()).collect(Collectors.toList())
                : new ArrayList<>();

        List<MetodoUMLDTO> mets = c.getMetodos() != null
                ? c.getMetodos().stream().map(met -> MetodoUMLDTO.builder()
                .id(met.getId())
                .nombre(met.getNombre())
                .tipoRetorno(met.getTipoRetorno())
                .visibilidad(met.getVisibilidad())
                .parametros(met.getParametros())
                .claseId(c.getId())
                .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return ClaseUMLDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .visibilidad(c.getVisibilidad())
                .descripcion(c.getDescripcion())
                .posicionX(c.getPosicionX())
                .posicionY(c.getPosicionY())
                .modeloId(c.getModeloUML() != null ? c.getModeloUML().getId() : null)
                .atributos(attrs)
                .metodos(mets)
                .build();
    }
}
