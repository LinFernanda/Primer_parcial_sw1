import { apiClient } from '../../../services/api';
import {
  GeneratedProjectPreviewDTO,
  GeneratorRequestDTO,
  GeneratorResponseDTO,
} from '../models/generator.types';

export const generatorService = {
  /**
   * Obtiene la previsualización del árbol de archivos y código del proyecto generado.
   * GET /api/generator/project/{modeloId}/preview
   */
  async previewProject(
    modeloId: number,
    options?: GeneratorRequestDTO
  ): Promise<GeneratedProjectPreviewDTO> {
    const params = new URLSearchParams();
    if (options?.packageName) params.append('packageName', options.packageName);
    if (options?.projectName) params.append('projectName', options.projectName);

    const response = await apiClient.get<GeneratedProjectPreviewDTO>(
      `/api/generator/project/${modeloId}/preview?${params.toString()}`
    );
    return response.data;
  },

  /**
   * Genera el backend y registra el evento en el historial del modelo UML.
   * POST /api/generator/project/{modeloId}
   */
  async generateProject(
    modeloId: number,
    options?: GeneratorRequestDTO
  ): Promise<GeneratorResponseDTO> {
    const response = await apiClient.post<GeneratorResponseDTO>(
      `/api/generator/project/${modeloId}`,
      options || {}
    );
    return response.data;
  },

  /**
   * Descarga el archivo comprimido .ZIP con la solución Spring Boot completa.
   * GET /api/generator/project/{modeloId}/zip
   */
  async downloadZip(modeloId: number, options?: GeneratorRequestDTO): Promise<void> {
    const params = new URLSearchParams();
    if (options?.packageName) params.append('packageName', options.packageName);
    if (options?.projectName) params.append('projectName', options.projectName);

    const response = await apiClient.get(
      `/api/generator/project/${modeloId}/zip?${params.toString()}`,
      {
        responseType: 'blob',
      }
    );

    // Crear un blob y forzar la descarga en el navegador del usuario
    const blob = new Blob([response.data], { type: 'application/zip' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    const filename = (options?.projectName || 'spring-boot-backend') + '.zip';
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};
