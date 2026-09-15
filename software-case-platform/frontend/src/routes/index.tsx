import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ProjectsPage } from '../pages/ProjectsPage';
import { UMLEditorPage } from '../pages/UMLEditorPage';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import { useHealthCheck } from '../hooks/useHealthCheck';
import { authService } from '../services/authService';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  if (!authService.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

export const AppRouter: React.FC = () => {
  const healthCheck = useHealthCheck(15000);

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/"
        element={
          authService.isAuthenticated() ? (
            <Navigate to="/projects" replace />
          ) : (
            <Navigate to="/login" replace />
          )
        }
      />

      <Route
        path="/projects"
        element={
          <ProtectedRoute>
            <ProjectsPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/projects/:id/editor"
        element={
          <ProtectedRoute>
            <UMLEditorPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/system-health"
        element={
          <DashboardPage
            health={healthCheck.health}
            info={healthCheck.info}
            ping={healthCheck.ping}
            isLoading={healthCheck.isLoading}
            error={healthCheck.error}
            lastChecked={healthCheck.lastChecked}
          />
        }
      />

      <Route path="*" element={<Navigate to="/projects" replace />} />
    </Routes>
  );
};
