import { apiClient } from '../../../services/api';
import {
  ApplyImageUMLRequestDTO,
  ImageUploadResponseDTO,
  ImageUMLDetectedDTO,
  ModeloUML,
} from '../models/imageuml.types';

export const imageUmlService = {
  /**
   * Sube una imagen de diagrama UML (PNG, JPG, JPEG) y obtiene el modelo UML
   * detectado mediante visión computacional, OCR (Tesseract) e IA.
   * POST /api/imageuml/upload
   */
  async uploadImage(
    file: File,
    modeloId?: number,
    ocrText?: string
  ): Promise<ImageUploadResponseDTO> {
    const formData = new FormData();
    formData.append('file', file);
    if (modeloId !== undefined && modeloId !== null) {
      formData.append('modeloId', modeloId.toString());
    }
    if (ocrText && ocrText.trim()) {
      formData.append('ocrText', ocrText.trim());
    }

    const response = await apiClient.post<ImageUploadResponseDTO>(
      '/api/imageuml/upload',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        timeout: 60000, // Permitir hasta 60s para procesamiento de visión artificial
      }
    );
    return response.data;
  },

  /**
   * Parsea texto o especificación UML directamente (PlantUML, Mermaid o formato estructurado)
   * sin requerir imagen.
   * POST /api/imageuml/parse-text
   */
  async parseText(text: string): Promise<ImageUMLDetectedDTO> {
    const response = await apiClient.post<ImageUMLDetectedDTO>(
      '/api/imageuml/parse-text',
      { text }
    );
    return response.data;
  },

  /**
   * Aplica el modelo UML detectado (o editado en la previsualización) al diagrama UML activo.
   * POST /api/imageuml/models/{modeloId}/apply
   */
  async applyDetectedModel(
    modeloId: number,
    request: ApplyImageUMLRequestDTO
  ): Promise<ModeloUML> {
    const response = await apiClient.post<ModeloUML>(
      `/api/imageuml/models/${modeloId}/apply`,
      request
    );
    return response.data;
  },

  /**
   * Flujo directo de un solo paso: sube, analiza y aplica directamente al modelo UML.
   * POST /api/imageuml/models/{modeloId}/convert
   */
  async convertAndApplyDirectly(modeloId: number, file: File): Promise<ModeloUML> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post<ModeloUML>(
      `/api/imageuml/models/${modeloId}/convert`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        timeout: 60000,
      }
    );
    return response.data;
  },

  /**
   * Obtiene la información y estado de procesamiento de una imagen previamente procesada.
   * GET /api/imageuml/{imagenId}
   */
  async getImageDetail(imagenId: number): Promise<ImageUploadResponseDTO> {
    const response = await apiClient.get<ImageUploadResponseDTO>(
      `/api/imageuml/${imagenId}`
    );
    return response.data;
  },

  /**
   * Retorna la URL para visualizar la imagen original descargada desde el backend.
   * GET /api/imageuml/{imagenId}/file
   */
  getImageFileUrl(imagenId: number): string {
    const baseUrl = apiClient.defaults.baseURL || '';
    return `${baseUrl}/api/imageuml/${imagenId}/file`;
  },
};
