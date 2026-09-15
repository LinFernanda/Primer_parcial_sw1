package com.caseplatform.dto.uml;

import com.caseplatform.model.VisibilidadUML;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la definición, creación y retorno de métodos UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetodoUMLDTO {

    private Long id;

    @NotBlank(message = "El nombre del método es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre del método debe tener entre 1 y 100 caracteres")
    private String nombre;

    @Builder.Default
    private String tipoRetorno = "void";

    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PUBLIC;

    private String parametros;
    private Long claseId;
}
