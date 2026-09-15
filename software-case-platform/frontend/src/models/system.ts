export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface HealthStatus {
  status: string;
  application: string;
  version: string;
  environment: string;
  databaseStatus: string;
  systemDetails: {
    javaVersion?: string;
    javaVendor?: string;
    osName?: string;
    totalMemoryMB?: number;
    freeMemoryMB?: number;
  };
}

export interface SystemInfo {
  name: string;
  phase: string;
  status: string;
  capabilities: string[];
}
