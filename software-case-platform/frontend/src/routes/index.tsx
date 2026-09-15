import React from 'react';
import { DashboardPage } from '../pages/DashboardPage';
import { useHealthCheck } from '../hooks/useHealthCheck';

export const AppRouter: React.FC = () => {
  const healthCheck = useHealthCheck(15000);

  return (
    <DashboardPage
      health={healthCheck.health}
      info={healthCheck.info}
      ping={healthCheck.ping}
      isLoading={healthCheck.isLoading}
      error={healthCheck.error}
      lastChecked={healthCheck.lastChecked}
    />
  );
};
