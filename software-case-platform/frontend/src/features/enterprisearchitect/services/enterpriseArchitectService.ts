import { apiClient } from '../../../services/api';
import {
  XMIExportResponseDTO,
  XMIImportResponseDTO,
  XMIValidationResponseDTO,
} from '../models/ea.types';

export const enterpriseArchitectService = {
  /**
   * Valida un archivo XMI y previsualiza sus elementos sin persistir.
   * POST /api/integration/ea/validate
   */
  async validateXMI(file: File): Promise<XMIValidationResponseDTO> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post<XMIValidationResponseDTO>(
      '/api/integration/ea/validate',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  },

  /**
   * Importa un archivo XMI sobre un modelo UML existente.
   * POST /api/integration/ea/models/{modeloId}/import
   */
  async importToExistingModel(
    modeloId: number,
    file: File,
    limpiarExistente = false
  ): Promise<XMIImportResponseDTO> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post<XMIImportResponseDTO>(
      `/api/integration/ea/models/${modeloId}/import?limpiarExistente=${limpiarExistente}`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  },

  /**
   * Importa un archivo XMI creando un nuevo modelo UML en el proyecto.
   * POST /api/integration/ea/projects/{proyectoId}/import
   */
  async importAsNewModel(
    proyectoId: number,
    file: File,
    nombreModelo?: string
  ): Promise<XMIImportResponseDTO> {
    const formData = new FormData();
    formData.append('file', file);

    const url = nombreModelo
      ? `/api/integration/ea/projects/${proyectoId}/import?nombreModelo=${encodeURIComponent(nombreModelo)}`
      : `/api/integration/ea/projects/${proyectoId}/import`;

    const response = await apiClient.post<XMIImportResponseDTO>(
      url,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  },

  /**
   * Obtiene la previsualización del contenido XML XMI a exportar.
   * GET /api/integration/ea/models/{modeloId}/export/preview
   */
  async previewExport(modeloId: number): Promise<XMIExportResponseDTO> {
    const response = await apiClient.get<XMIExportResponseDTO>(
      `/api/integration/ea/models/${modeloId}/export/preview`
    );
    return response.data;
  },

  /**
   * Descarga directamente el archivo XMI (.xml) generado para Enterprise Architect.
   * GET /api/integration/ea/models/{modeloId}/export
   */
  async downloadXMI(modeloId: number, fileName?: string): Promise<void> {
    const response = await apiClient.get(
      `/api/integration/ea/models/${modeloId}/export`,
      {
        responseType: 'blob',
        headers: {
          Accept: 'application/xml, text/xml, application/octet-stream, */*',
        },
      }
    );

    const blob = new Blob([response.data], { type: 'application/xml' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName || `modelo_${modeloId}_ea_xmi21.xml`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },
};
