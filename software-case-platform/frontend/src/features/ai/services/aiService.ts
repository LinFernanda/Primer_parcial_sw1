import { apiClient } from '../../../services/api';
import {
  AICommandHistory,
  AICommandRequest,
  AICommandResponse,
} from '../models/ai.types';

export const aiService = {
  /**
   * Envía una instrucción en lenguaje natural al Agente IA para modificar el modelo UML.
   */
  async enviarComando(modeloId: number, request: AICommandRequest): Promise<AICommandResponse> {
    const response = await apiClient.post<AICommandResponse>(
      `/api/modelos/${modeloId}/ai/command`,
      request
    );
    return response.data;
  },

  /**
   * Envía una instrucción por voz (texto transcrito o audio) para su procesamiento por el Agente IA.
   */
  async enviarVoz(
    modeloId: number,
    textoTranscrito: string,
    audioBase64?: string
  ): Promise<AICommandResponse> {
    const response = await apiClient.post<AICommandResponse>(
      `/api/modelos/${modeloId}/ai/voice`,
      { textoTranscrito, audioBase64 }
    );
    return response.data;
  },

  /**
   * Obtiene la lista histórica de comandos y acciones ejecutadas por la IA para un modelo.
   */
  async obtenerHistorial(modeloId: number): Promise<AICommandHistory[]> {
    const response = await apiClient.get<AICommandHistory[]>(
      `/api/modelos/${modeloId}/ai/history`
    );
    return response.data;
  },
};
