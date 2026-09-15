import { apiClient } from '../../../services/api';
import {
  AtributoUML,
  ClaseUML,
  MetodoUML,
  ModeloUML,
  ProyectoUML,
  RelacionUML,
} from '../models/uml.types';

export const umlService = {
  // Proyectos
  async getProyectos(): Promise<ProyectoUML[]> {
    const response = await apiClient.get<ProyectoUML[]>('/api/proyectos');
    return response.data;
  },

  async getProyectoById(id: number): Promise<ProyectoUML> {
    const response = await apiClient.get<ProyectoUML>(`/api/proyectos/${id}`);
    return response.data;
  },

  async createProyecto(data: Partial<ProyectoUML>): Promise<ProyectoUML> {
    const response = await apiClient.post<ProyectoUML>('/api/proyectos', data);
    return response.data;
  },

  async updateProyecto(id: number, data: Partial<ProyectoUML>): Promise<ProyectoUML> {
    const response = await apiClient.put<ProyectoUML>(`/api/proyectos/${id}`, data);
    return response.data;
  },

  async deleteProyecto(id: number): Promise<void> {
    await apiClient.delete(`/api/proyectos/${id}`);
  },

  // Modelos
  async getModeloById(id: number): Promise<ModeloUML> {
    const response = await apiClient.get<ModeloUML>(`/api/modelos/${id}`);
    return response.data;
  },

  async createModelo(proyectoId: number, data: Partial<ModeloUML>): Promise<ModeloUML> {
    const response = await apiClient.post<ModeloUML>(`/api/proyectos/${proyectoId}/modelos`, data);
    return response.data;
  },

  async deleteModelo(id: number): Promise<void> {
    await apiClient.delete(`/api/modelos/${id}`);
  },

  // Clases
  async getClasesByModelo(modeloId: number): Promise<ClaseUML[]> {
    const response = await apiClient.get<ClaseUML[]>(`/api/modelos/${modeloId}/clases`);
    return response.data;
  },

  async createClase(modeloId: number, data: Partial<ClaseUML>): Promise<ClaseUML> {
    const response = await apiClient.post<ClaseUML>(`/api/modelos/${modeloId}/clases`, data);
    return response.data;
  },

  async updateClase(id: number, data: Partial<ClaseUML>): Promise<ClaseUML> {
    const response = await apiClient.put<ClaseUML>(`/api/clases/${id}`, data);
    return response.data;
  },

  async deleteClase(id: number): Promise<void> {
    await apiClient.delete(`/api/clases/${id}`);
  },

  // Atributos
  async addAtributo(claseId: number, data: Partial<AtributoUML>): Promise<AtributoUML> {
    const response = await apiClient.post<AtributoUML>(`/api/clases/${claseId}/atributos`, data);
    return response.data;
  },

  async deleteAtributo(id: number): Promise<void> {
    await apiClient.delete(`/api/atributos/${id}`);
  },

  // Métodos
  async addMetodo(claseId: number, data: Partial<MetodoUML>): Promise<MetodoUML> {
    const response = await apiClient.post<MetodoUML>(`/api/clases/${claseId}/metodos`, data);
    return response.data;
  },

  async deleteMetodo(id: number): Promise<void> {
    await apiClient.delete(`/api/metodos/${id}`);
  },

  // Relaciones
  async getRelacionesByModelo(modeloId: number): Promise<RelacionUML[]> {
    const response = await apiClient.get<RelacionUML[]>(`/api/modelos/${modeloId}/relaciones`);
    return response.data;
  },

  async createRelacion(data: Partial<RelacionUML>): Promise<RelacionUML> {
    const response = await apiClient.post<RelacionUML>('/api/relaciones', data);
    return response.data;
  },

  async deleteRelacion(id: number): Promise<void> {
    await apiClient.delete(`/api/relaciones/${id}`);
  },
};
