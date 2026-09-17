package com.caseplatform.integration.enterprisearchitect.dto;

import com.caseplatform.integration.enterprisearchitect.model.UMLImportModel;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportValidationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de validación de un archivo XMI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XMIValidationResponseDTO {
    private boolean valido;
    private String mensaje;
    private UMLImportValidationResult validacion;
    private UMLImportModel preview;
}
