package com.caseplatform.versioning.service;

import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.versioning.dto.CreateVersionDTO;
import com.caseplatform.versioning.dto.HistorialCambioDTO;
import com.caseplatform.versioning.dto.RestoreVersionDTO;
import com.caseplatform.versioning.dto.SnapshotModeloDTO;
import com.caseplatform.versioning.dto.VersionDTO;
import com.caseplatform.versioning.model.TipoOperacionHistorial;

import java.util.List;

/**
 * Servicio para la gestión de versiones, congelamiento de snapshots,
 * registro histórico y restauración de estados del modelo UML.
 */
public interface VersionService {

    /**
     * Crea un snapshot congelado y una nueva versión formal del modelo UML.
     */
    VersionDTO crearVersion(CreateVersionDTO dto, String usuarioEmail);

    /**
     * Lista todas las versiones registradas para un modelo UML ordenadas de la más reciente a la más antigua.
     */
    List<VersionDTO> listarVersionesPorModelo(Long modeloId);

    /**
     * Obtiene el detalle completo de una versión por su ID.
     */
    VersionDTO obtenerVersionPorId(Long versionId);

    /**
     * Obtiene el historial cronológico de cambios atómicos de un modelo UML.
     */
    List<HistorialCambioDTO> obtenerHistorialPorModelo(Long modeloId);

    /**
     * Restaura un modelo UML a un snapshot o versión previa manteniendo la integridad conceptual.
     */
    ModeloUMLDTO restaurarVersion(Long versionId, RestoreVersionDTO dto, String usuarioEmail);

    /**
     * Registra un cambio atómico en el historial de modificaciones del modelo UML.
     */
    void registrarCambio(
            Long modeloId,
            TipoOperacionHistorial tipo,
            String elementoModificado,
            String idElemento,
            String datosAnteriores,
            String datosNuevos,
            String usuarioEmail
    );

    /**
     * Genera un snapshot estructurado del estado actual del modelo UML.
     */
    SnapshotModeloDTO generarSnapshot(Long modeloId);
}
