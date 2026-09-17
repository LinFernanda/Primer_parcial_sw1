package com.caseplatform.versioning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solicitud para la creación o snapshot de una nueva versión del modelo UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVersionDTO {

    @NotNull(message = "El identificador del modelo UML es obligatorio")
    private Long modeloId;

    private String numeroVersion;

    @NotBlank(message = "El nombre de la versión es obligatorio")
    private String nombreVersion;

    private String descripcion;
}
