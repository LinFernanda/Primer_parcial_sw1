import { useState, useEffect, useCallback } from 'react';
import { healthService } from '../services/healthService';
import { HealthStatus, SystemInfo } from '../models/system';

interface HealthCheckState {
  health: HealthStatus | null;
  info: SystemInfo | null;
  ping: string | null;
  isLoading: boolean;
  error: string | null;
  lastChecked: Date | null;
}

export function useHealthCheck(pollIntervalMs = 15000) {
  const [state, setState] = useState<HealthCheckState>({
    health: null,
    info: null,
    ping: null,
    isLoading: true,
    error: null,
    lastChecked: null,
  });

  const checkStatus = useCallback(async () => {
    setState((prev) => ({ ...prev, isLoading: true, error: null }));
    try {
      const [pingResult, healthResult, infoResult] = await Promise.all([
        healthService.ping().catch((e) => `Ping failed: ${e.message}`),
        healthService.getHealth().catch(() => null),
        healthService.getInfo().catch(() => null),
      ]);

      setState({
        ping: pingResult,
        health: healthResult,
        info: infoResult,
        isLoading: false,
        error: null,
        lastChecked: new Date(),
      });
    } catch (err: unknown) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to connect to backend';
      setState((prev) => ({
        ...prev,
        isLoading: false,
        error: errorMessage,
        lastChecked: new Date(),
      }));
    }
  }, []);

  useEffect(() => {
    checkStatus();
    const timer = setInterval(checkStatus, pollIntervalMs);
    return () => clearInterval(timer);
  }, [checkStatus, pollIntervalMs]);

  return {
    ...state,
    refetch: checkStatus,
  };
}
