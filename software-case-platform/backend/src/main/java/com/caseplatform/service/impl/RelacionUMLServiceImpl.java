package com.caseplatform.service.impl;

import com.caseplatform.dto.uml.RelacionUMLDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.RelacionUML;
import com.caseplatform.model.TipoRelacionUML;
import com.caseplatform.repository.ClaseUMLRepository;
import com.caseplatform.repository.RelacionUMLRepository;
import com.caseplatform.service.RelacionUMLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RelacionUMLServiceImpl implements RelacionUMLService {

    private static final Set<String> CARDINALIDADES_VALIDAS = Set.of("1", "0..1", "*", "1..*", "0..*");

    private final RelacionUMLRepository relacionUMLRepository;
    private final ClaseUMLRepository claseUMLRepository;

    @Override
    @Transactional
    public RelacionUMLDTO crearRelacion(RelacionUMLDTO dto) {
        log.info("Creando relación UML de tipo: {} entre origen ID: {} y destino ID: {}",
                dto.getTipoRelacion(), dto.getClaseOrigenId(), dto.getClaseDestinoId());

        if (dto.getClaseOrigenId() == null) {
            throw new ValidationException("La relación UML debe especificar una clase de origen obligatoriamente");
        }
        if (dto.getClaseDestinoId() == null) {
            throw new ValidationException("La relación UML debe especificar una clase de destino obligatoriamente");
        }
        if (dto.getTipoRelacion() == null) {
            throw new ValidationException("El tipo de relación UML es obligatorio");
        }

        // Validación de herencia sobre sí misma
        if (dto.getTipoRelacion() == TipoRelacionUML.HERENCIA && dto.getClaseOrigenId().equals(dto.getClaseDestinoId())) {
            throw new ValidationException("Una clase no puede heredar de sí misma (autorreferencia de herencia no permitida)");
        }

        // Validación de cardinalidades
        String cardOrigen = dto.getCardinalidadOrigen() != null && !dto.getCardinalidadOrigen().isBlank()
                ? dto.getCardinalidadOrigen().trim() : "1";
        String cardDestino = dto.getCardinalidadDestino() != null && !dto.getCardinalidadDestino().isBlank()
                ? dto.getCardinalidadDestino().trim() : "1";

        if (!CARDINALIDADES_VALIDAS.contains(cardOrigen)) {
            throw new ValidationException("Cardinalidad de origen inválida: '" + cardOrigen + "'. Soportadas: " + CARDINALIDADES_VALIDAS);
        }
        if (!CARDINALIDADES_VALIDAS.contains(cardDestino)) {
            throw new ValidationException("Cardinalidad de destino inválida: '" + cardDestino + "'. Soportadas: " + CARDINALIDADES_VALIDAS);
        }

        ClaseUML origen = claseUMLRepository.findById(dto.getClaseOrigenId())
                .orElseThrow(() -> new ResourceNotFoundException("Clase origen no encontrada con ID: " + dto.getClaseOrigenId()));

        ClaseUML destino = claseUMLRepository.findById(dto.getClaseDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Clase destino no encontrada con ID: " + dto.getClaseDestinoId()));

        // Ambas clases deben pertenecer al mismo modelo UML
        if (!origen.getModeloUML().getId().equals(destino.getModeloUML().getId())) {
            throw new ValidationException("Las clases origen y destino deben pertenecer al mismo modelo UML");
        }

        ModeloUML modelo = origen.getModeloUML();

        RelacionUML relacion = RelacionUML.builder()
                .tipoRelacion(dto.getTipoRelacion())
                .claseOrigen(origen)
                .claseDestino(destino)
                .cardinalidadOrigen(cardOrigen)
                .cardinalidadDestino(cardDestino)
                .descripcion(dto.getDescripcion())
                .modeloUML(modelo)
                .build();

        RelacionUML guardada = relacionUMLRepository.save(relacion);
        log.info("Relación UML creada con ID: {}", guardada.getId());

        return mapToDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelacionUMLDTO> listarPorModelo(Long modeloId) {
        return relacionUMLRepository.findByModeloUMLId(modeloId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RelacionUMLDTO obtenerPorId(Long id) {
        RelacionUML relacion = relacionUMLRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relación UML no encontrada con ID: " + id));

        return mapToDTO(relacion);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!relacionUMLRepository.existsById(id)) {
            throw new ResourceNotFoundException("Relación UML no encontrada con ID: " + id);
        }
        relacionUMLRepository.deleteById(id);
        log.info("Relación UML eliminada con ID: {}", id);
    }

    private RelacionUMLDTO mapToDTO(RelacionUML r) {
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
