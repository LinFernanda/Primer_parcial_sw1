package com.caseplatform.integration.enterprisearchitect.service;

import com.caseplatform.integration.enterprisearchitect.dto.XMIExportResponseDTO;

/**
 * Servicio de exportación de modelos UML a formato XMI para Enterprise Architect.
 */
public interface EnterpriseArchitectExportService {

    /**
     * Exporta el modelo UML a una cadena XML con formato XMI 2.1 estándar de Enterprise Architect.
     *
     * @param modeloId identificador del modelo UML
     * @param usuarioEmail correo del usuario solicitante
     * @return XMIExportResponseDTO con metadatos y contenido XML
     */
    XMIExportResponseDTO exportModel(Long modeloId, String usuarioEmail);

    /**
     * Exporta el modelo UML directamente a bytes XML para descarga HTTP.
     *
     * @param modeloId identificador del modelo UML
     * @param usuarioEmail correo del usuario solicitante
     * @return arreglo de bytes con el contenido XML/XMI
     */
    byte[] exportModelAsBytes(Long modeloId, String usuarioEmail);
}
