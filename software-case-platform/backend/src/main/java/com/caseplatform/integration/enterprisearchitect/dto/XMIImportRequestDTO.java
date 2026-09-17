package com.caseplatform.integration.enterprisearchitect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parámetros de configuración para importar un archivo XMI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XMIImportRequestDTO {
    private Long modeloId;
    private Long proyectoId;
    private String nombreModelo;
    @Builder.Default
    private boolean limpiarExistente = false;
    @Builder.Default
    private boolean preservarPosiciones = true;
}
