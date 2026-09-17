package com.caseplatform.versioning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de restauración de una versión previa del modelo UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreVersionDTO {

    private String comentario;
}
