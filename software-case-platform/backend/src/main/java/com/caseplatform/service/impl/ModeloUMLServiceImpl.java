package com.caseplatform.service.impl;

import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.MetodoUMLDTO;
import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.dto.uml.RelacionUMLDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.ProyectoUML;
import com.caseplatform.model.RelacionUML;
import com.caseplatform.repository.ModeloUMLRepository;
import com.caseplatform.repository.ProyectoUMLRepository;
import com.caseplatform.service.ModeloUMLService;
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
public class ModeloUMLServiceImpl implements ModeloUMLService {

    private final ModeloUMLRepository modeloUMLRepository;
    private final ProyectoUMLRepository proyectoUMLRepository;

    @Override
    @Transactional
    public ModeloUMLDTO crearModelo(Long proyectoId, ModeloUMLDTO dto, String usuarioEmail) {
        log.info("Creando nuevo modelo UML '{}' para el proyecto ID: {}", dto.getNombre(), proyectoId);

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new ValidationException("El nombre del modelo UML es obligatorio");
        }

        ProyectoUML proyecto = proyectoUMLRepository.findById(proyectoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto UML no encontrado con ID: " + proyectoId));

        ModeloUML modelo = ModeloUML.builder()
                .nombre(dto.getNombre().trim())
                .version(dto.getVersion() != null && !dto.getVersion().isBlank() ? dto.getVersion().trim() : "1.0")
                .proyecto(proyecto)
                .build();

        ModeloUML guardado = modeloUMLRepository.save(modelo);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModeloUMLDTO> listarPorProyecto(Long proyectoId) {
        return modeloUMLRepository.findByProyectoId(proyectoId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ModeloUMLDTO obtenerPorId(Long id) {
        ModeloUML modelo = modeloUMLRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo UML no encontrado con ID: " + id));

        return mapToDTO(modelo);
    }

    @Override
    @Transactional
    public void eliminar(Long id, String usuarioEmail) {
        if (!modeloUMLRepository.existsById(id)) {
            throw new ResourceNotFoundException("Modelo UML no encontrado con ID: " + id);
        }
        modeloUMLRepository.deleteById(id);
        log.info("Modelo UML eliminado con ID: {}", id);
    }

    private ModeloUMLDTO mapToDTO(ModeloUML m) {
        List<ClaseUMLDTO> clasesDTO = m.getClases() != null
                ? m.getClases().stream().map(this::mapClaseToDTO).collect(Collectors.toList())
                : new ArrayList<>();

        List<RelacionUMLDTO> relacionesDTO = m.getRelaciones() != null
                ? m.getRelaciones().stream().map(this::mapRelacionToDTO).collect(Collectors.toList())
                : new ArrayList<>();

        return ModeloUMLDTO.builder()
                .id(m.getId())
                .nombre(m.getNombre())
                .version(m.getVersion())
                .fechaCreacion(m.getFechaCreacion())
                .fechaActualizacion(m.getFechaActualizacion())
                .proyectoId(m.getProyecto() != null ? m.getProyecto().getId() : null)
                .clases(clasesDTO)
                .relaciones(relacionesDTO)
                .build();
    }

    private ClaseUMLDTO mapClaseToDTO(ClaseUML c) {
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

    private RelacionUMLDTO mapRelacionToDTO(RelacionUML r) {
        return RelacionUMLDTO.builder()
                .id(r.getId())
                .tipoRelacion(r.getTipoRelacion())
                .claseOrigenId(r.getClaseOrigen() != null ? r.getClaseOrigen().getId() : null)
                .claseOrigenNombre(r.getClaseOrigen() != null ? r.getClaseOrigen().getNombre() : null)
                .claseDestinoId(r.getClaseDestino() != null ? r.getClaseDestino().getId() : null)
                .claseDestinoNombre(r.getClaseDestino() != null ? r.getClaseDestino().getNombre() : null)
                .cardinalidadOrigen(r.getCardinalidadOrigen())
                .cardinalidadDestino(r.getCardinalidadDestino())
                .descripcion(r.getDescripcion())
                .modeloId(r.getModeloUML() != null ? r.getModeloUML().getId() : null)
                .build();
    }
}
