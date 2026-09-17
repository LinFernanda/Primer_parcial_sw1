package com.caseplatform.integration.enterprisearchitect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de importación exitosa de un archivo XMI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XMIImportResponseDTO {
    private boolean exito;
    private String mensaje;
    private Long modeloId;
    private Long proyectoId;
    private String nombreModelo;
    private Long versionId;
    private String versionNumero;
    private int clasesImportadas;
    private int atributosImportados;
    private int metodosImportados;
    private int relacionesImportadas;
    private String archivoOrigen;
    private LocalDateTime fecha;
}
