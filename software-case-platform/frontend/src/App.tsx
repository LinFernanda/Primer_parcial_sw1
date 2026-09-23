import React from 'react';
import { Header } from './components/Header';
import { AppRouter } from './routes';
import { useHealthCheck } from './hooks/useHealthCheck';

export const App: React.FC = () => {
  const { health, ping, isLoading, refetch } = useHealthCheck(15000);

  const isBackendOnline = health?.status === 'UP' || ping === 'pong';
  const isDbOnline = health?.databaseStatus?.includes('CONNECTED') ?? false;

  return (
    <div className="app-container">
      <Header
        backendOnline={isBackendOnline}
        dbOnline={isDbOnline}
        onRefresh={refetch}
        isLoading={isLoading}
      />
      <div className="main-viewport">
        <AppRouter />
      </div>
      <footer className="footer">
        <div className="footer-content">
          <span>CASE Platform © 2026 - Plataforma CASE Colaborativa Inteligente</span>
          <span>Arquitectura Base y Modelado Conceptual UML</span>
        </div>
      </footer>
    </div>
  );
};

export default App;
