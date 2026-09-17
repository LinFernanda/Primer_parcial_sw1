import { apiClient } from '../../../services/api';
import { ModeloUML } from '../../uml-editor/models/uml.types';
import {
  CreateVersionDTO,
  HistorialCambioDTO,
  RestoreVersionDTO,
  VersionModeloDTO,
} from '../models/version.types';

export const versionService = {
  /**
   * Obtiene todas las versiones registradas para un modelo UML.
   */
  async getVersionesPorModelo(modeloId: number): Promise<VersionModeloDTO[]> {
    const response = await apiClient.get<VersionModeloDTO[]>(`/api/modelos/${modeloId}/versiones`);
    return response.data;
  },

  /**
   * Obtiene el detalle completo y snapshot de una versión específica.
   */
  async getVersionPorId(versionId: number): Promise<VersionModeloDTO> {
    const response = await apiClient.get<VersionModeloDTO>(`/api/versiones/${versionId}`);
    return response.data;
  },

  /**
   * Crea un nuevo snapshot y versión congelada del modelo UML.
   */
  async crearVersion(data: CreateVersionDTO): Promise<VersionModeloDTO> {
    const response = await apiClient.post<VersionModeloDTO>('/api/versiones', data);
    return response.data;
  },

  /**
   * Obtiene la cronología completa de modificaciones atómicas realizadas sobre el modelo UML.
   */
  async getHistorialPorModelo(modeloId: number): Promise<HistorialCambioDTO[]> {
    const response = await apiClient.get<HistorialCambioDTO[]>(`/api/modelos/${modeloId}/historial`);
    return response.data;
  },

  /**
   * Restaura el modelo UML al estado congelado en una versión previa.
   */
  async restaurarVersion(versionId: number, data?: RestoreVersionDTO): Promise<ModeloUML> {
    const response = await apiClient.post<ModeloUML>(`/api/versiones/${versionId}/restore`, data || {});
    return response.data;
  },
};
