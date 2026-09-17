package com.caseplatform.imageuml.dto;

import com.caseplatform.model.VisibilidadUML;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una clase UML detectada en la imagen con sus atributos, métodos y posición.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaseDetectadaDTO {

    private String nombre;

    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PUBLIC;

    private String descripcion;

    @Builder.Default
    private Double posicionX = 0.0;

    @Builder.Default
    private Double posicionY = 0.0;

    @Builder.Default
    private List<AtributoDetectadoDTO> atributos = new ArrayList<>();

    @Builder.Default
    private List<MetodoDetectadoDTO> metodos = new ArrayList<>();
}
