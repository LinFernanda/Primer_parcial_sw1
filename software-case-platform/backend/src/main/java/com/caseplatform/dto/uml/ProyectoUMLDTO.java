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
 * DTO para la creación, actualización y respuesta de proyectos UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoUMLDTO {

    private Long id;

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre del proyecto debe tener entre 2 y 150 caracteres")
    private String nombre;

    private String descripcion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Long usuarioPropietarioId;
    private String usuarioPropietarioEmail;

    @Builder.Default
    private String estado = "ACTIVO";

    @Builder.Default
    private List<ModeloUMLDTO> modelos = new ArrayList<>();
}
