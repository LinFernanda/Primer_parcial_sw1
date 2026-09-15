package com.caseplatform.dto.uml;

import com.caseplatform.model.VisibilidadUML;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la definición, creación y retorno de atributos UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtributoUMLDTO {

    private Long id;

    @NotBlank(message = "El nombre del atributo es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre del atributo debe tener entre 1 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El tipo de dato es obligatorio (ej. String, Integer, Long, Double, Boolean, Date)")
    private String tipoDato;

    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PRIVATE;

    private String valorInicial;
    private Long claseId;
}
