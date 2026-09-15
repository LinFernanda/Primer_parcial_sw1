package com.caseplatform.dto.uml;

import com.caseplatform.model.VisibilidadUML;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para representar una clase UML con sus atributos, métodos y coordenadas en la pizarra.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaseUMLDTO {

    private Long id;

    @NotBlank(message = "El nombre de la clase es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre de la clase debe tener entre 1 y 100 caracteres")
    private String nombre;

    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PUBLIC;

    private String descripcion;

    @Builder.Default
    private Double posicionX = 0.0;

    @Builder.Default
    private Double posicionY = 0.0;

    private Long modeloId;

    @Builder.Default
    private List<AtributoUMLDTO> atributos = new ArrayList<>();

    @Builder.Default
    private List<MetodoUMLDTO> metodos = new ArrayList<>();
}
