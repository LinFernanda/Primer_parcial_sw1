import React from 'react';
import { ShieldCheck, Cpu, Database, RefreshCw } from 'lucide-react';

interface HeaderProps {
  backendOnline: boolean;
  dbOnline: boolean;
  onRefresh: () => void;
  isLoading: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  backendOnline,
  dbOnline,
  onRefresh,
  isLoading,
}) => {
  return (
    <header className="header">
      <div className="header-container">
        <div className="brand">
          <div className="brand-logo">CASE</div>
          <div>
            <h1 className="brand-title">CASE Platform</h1>
            <span className="brand-subtitle">
              Plataforma Colaborativa Inteligente de Diseño UML &amp; Arquitectura
            </span>
          </div>
        </div>

        <div className="header-actions">
          <div className="status-badges">
            <div className={`status-badge ${backendOnline ? 'online' : 'offline'}`}>
              <Cpu size={15} />
              <span>Backend: {backendOnline ? 'Activo' : 'Inactivo'}</span>
            </div>
            <div className={`status-badge ${dbOnline ? 'online' : 'offline'}`}>
              <Database size={15} />
              <span>PostgreSQL: {dbOnline ? 'Conectado' : 'Desconectado'}</span>
            </div>
            <div className="status-badge phase">
              <ShieldCheck size={15} />
              <span>Fase 1: Preparada</span>
            </div>
          </div>

          <button
            onClick={onRefresh}
            disabled={isLoading}
            className="refresh-button"
            title="Refrescar estado"
          >
            <RefreshCw size={16} className={isLoading ? 'spin' : ''} />
            <span>Refrescar</span>
          </button>
        </div>
      </div>
    </header>
  );
};
