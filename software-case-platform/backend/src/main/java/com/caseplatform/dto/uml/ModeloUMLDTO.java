package com.caseplatform.dto.uml;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para representar el modelo conceptual completo con todas sus clases y relaciones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeloUMLDTO {

    private Long id;

    @NotBlank(message = "El nombre del modelo UML es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre del modelo debe tener entre 2 y 150 caracteres")
    private String nombre;

    @Builder.Default
    private String version = "1.0";

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Long proyectoId;

    @Builder.Default
    private List<ClaseUMLDTO> clases = new ArrayList<>();

    @Builder.Default
    private List<RelacionUMLDTO> relaciones = new ArrayList<>();
}
