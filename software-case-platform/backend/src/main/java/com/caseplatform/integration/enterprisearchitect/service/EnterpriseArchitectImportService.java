package com.caseplatform.integration.enterprisearchitect.service;

import com.caseplatform.integration.enterprisearchitect.dto.XMIImportResponseDTO;
import com.caseplatform.integration.enterprisearchitect.dto.XMIValidationResponseDTO;

import java.io.InputStream;

/**
 * Servicio de importación e interoperabilidad con Enterprise Architect.
 */
public interface EnterpriseArchitectImportService {

    /**
     * Valida un archivo XMI y retorna la vista previa de elementos sin persistir.
     *
     * @param xmiStream flujo de entrada del archivo XML/XMI
     * @return XMIValidationResponseDTO con estadísticas, clases detectadas y advertencias
     */
    XMIValidationResponseDTO validateXMI(InputStream xmiStream);

    /**
     * Importa el contenido de un archivo XMI sobre un modelo UML existente.
     *
     * @param modeloId identificador del modelo UML destino
     * @param xmiStream flujo del archivo XMI
     * @param fileName nombre original del archivo
     * @param limpiarExistente si es true, elimina las clases previas antes de importar; si es false, fusiona
     * @param usuarioEmail correo del usuario autenticado
     * @return XMIImportResponseDTO con detalles del modelo actualizado y versión registrada
     */
    XMIImportResponseDTO importToExistingModel(Long modeloId, InputStream xmiStream, String fileName, boolean limpiarExistente, String usuarioEmail);

    /**
     * Importa un archivo XMI creando un nuevo modelo UML dentro de un proyecto.
     *
     * @param proyectoId identificador del proyecto destino
     * @param xmiStream flujo del archivo XMI
     * @param fileName nombre original del archivo
     * @param nombreModelo nombre opcional para el nuevo modelo
     * @param usuarioEmail correo del usuario autenticado
     * @return XMIImportResponseDTO con los identificadores generados
     */
    XMIImportResponseDTO importAsNewModel(Long proyectoId, InputStream xmiStream, String fileName, String nombreModelo, String usuarioEmail);
}
