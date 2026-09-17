package com.caseplatform.integration.enterprisearchitect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para metadatos o resumen de exportación XMI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XMIExportResponseDTO {
    private String nombreArchivo;
    private String versionXMI;
    private int totalClases;
    private int totalRelaciones;
    private long tamanoBytes;
    private String contenidoXML;
}
