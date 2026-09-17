import apiClient from './api';
import { ApiResponse, HealthStatus, SystemInfo } from '../models/system';

export const healthService = {
  async ping(): Promise<string> {
    const response = await apiClient.get<ApiResponse<string>>('/api/v1/ping');
    return response.data.data;
  },

  async getHealth(): Promise<HealthStatus> {
    const response = await apiClient.get<ApiResponse<HealthStatus>>('/api/v1/health');
    return response.data.data;
  },

  async getInfo(): Promise<SystemInfo> {
    const response = await apiClient.get<ApiResponse<SystemInfo>>('/api/v1/info');
    return response.data.data;
  },
};
