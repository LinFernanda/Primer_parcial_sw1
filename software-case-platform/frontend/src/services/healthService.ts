import apiClient from './api';
import { ApiResponse, HealthStatus, SystemInfo } from '../models/system';

export const healthService = {
  async ping(): Promise<string> {
    const response = await apiClient.get<ApiResponse<string>>('/ping');
    return response.data.data;
  },

  async getHealth(): Promise<HealthStatus> {
    const response = await apiClient.get<ApiResponse<HealthStatus>>('/health');
    return response.data.data;
  },

  async getInfo(): Promise<SystemInfo> {
    const response = await apiClient.get<ApiResponse<SystemInfo>>('/info');
    return response.data.data;
  },
};
