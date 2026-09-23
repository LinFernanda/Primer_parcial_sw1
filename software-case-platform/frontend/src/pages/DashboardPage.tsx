import React from 'react';
import {
  Server,
  Database,
  Smartphone,
  Layers,
  CheckCircle2,
  AlertCircle,
  FileCode2,
  Terminal,
  Cpu,
} from 'lucide-react';
import { StatusCard } from '../components/StatusCard';
import { HealthStatus, SystemInfo } from '../models/system';

interface DashboardPageProps {
  health: HealthStatus | null;
  info: SystemInfo | null;
  ping: string | null;
  isLoading: boolean;
  error: string | null;
  lastChecked: Date | null;
}

export const DashboardPage: React.FC<DashboardPageProps> = ({
  health,
  info,
  ping,
  isLoading,
  error,
  lastChecked,
}) => {
  const isBackendUp = health?.status === 'UP' || ping === 'pong';
  const isDbUp = health?.databaseStatus?.includes('CONNECTED') ?? false;

  const phases = [
    {
      id: 'Arquitectura',
      name: 'Arquitectura Base y Persistencia',
      status: 'Operativo',
      ready: true,
      desc: 'Monorepo organizado, Spring Boot 3 + Java 21, React + Vite + TypeScript, Flutter estructura, PostgreSQL 16.',
    },
    {
      id: 'Seguridad',
      name: 'Sistema de Usuarios, Autenticación y Seguridad',
      status: 'Operativo',
      ready: true,
      desc: 'JWT Stateless, RBAC, registro, login, refresh token, protección de endpoints.',
    },
    {
      id: 'Metamodelo',
      name: 'Núcleo UML 2.5 y Modelo Conceptual',
      status: 'Operativo',
      ready: true,
      desc: 'Modelo metamodelo UML 2.5, clases, atributos, métodos, relaciones, cardinalidades.',
    },
    {
      id: 'Editor UML',
      name: 'Editor Visual UML 2.5',
      status: 'Operativo',
      ready: true,
      desc: 'Canvas interactivo, drag and drop, renderizado SVG/Canvas, zoom y exportación.',
    },
    {
      id: 'Colaboración',
      name: 'Colaboración en Tiempo Real (WebSocket)',
      status: 'Operativo',
      ready: true,
      desc: 'STOMP / WebSocket, presencia de usuarios, cursores remotos, locking optimista.',
    },
  ];

  return (
    <main className="dashboard-content">
      {/* Banner / Hero */}
      <section className="hero-banner">
        <div className="hero-content">
          <div className="hero-badge">ARQUITECTURA &amp; MODELADO UML OPERATIVO</div>
          <h2 className="hero-title">
            {info?.name || 'Plataforma CASE Colaborativa Inteligente'}
          </h2>
          <p className="hero-description">
            Entorno de ingeniería preparado con separación estricta de capas, backend en
            Spring Boot 3 (Java 21 LTS), frontend en React + TypeScript + Vite, aplicación
            móvil en Flutter y persistencia en PostgreSQL 16.
          </p>
          {info?.capabilities && (
            <div
              style={{
                marginTop: '0.75rem',
                display: 'flex',
                gap: '0.5rem',
                flexWrap: 'wrap',
              }}
            >
              {info.capabilities.map((cap) => (
                <span key={cap} className="layer-badge" style={{ fontSize: '0.7rem' }}>
                  {cap}
                </span>
              ))}
            </div>
          )}
          {lastChecked && (
            <p className="hero-last-check">
              Última comprobación: {lastChecked.toLocaleTimeString()}{' '}
              {isLoading && '(actualizando...)'}
            </p>
          )}
        </div>
      </section>

      {/* Error alert if backend unreachable */}
      {error && (
        <div className="alert-banner">
          <AlertCircle size={20} />
          <div>
            <strong>Aviso de conectividad backend:</strong> {error}. (Asegúrate de iniciar
            el backend con <code>mvn spring-boot:run</code> en{' '}
            <code>software-case-platform/backend</code>).
          </div>
        </div>
      )}

      {/* Status Cards Grid */}
      <section className="cards-grid">
        <StatusCard
          title="Backend Spring Boot"
          status={isBackendUp ? 'success' : 'warning'}
          icon={<Server size={22} />}
          value={isBackendUp ? 'Activo & Operativo' : 'Iniciando / Esperando'}
          description="Spring Boot 3.3.4 ejecutándose sobre Java 21 LTS con arquitectura por capas."
          meta={{
            Puerto: '8080',
            Perfil: health?.environment || 'dev',
            Java: health?.systemDetails?.javaVersion || '21.0.12.1',
            VM: health?.systemDetails?.javaVendor || 'Eclipse Adoptium',
          }}
        />

        <StatusCard
          title="Base de Datos PostgreSQL"
          status={isDbUp ? 'success' : isBackendUp ? 'warning' : 'info'}
          icon={<Database size={22} />}
          value={isDbUp ? 'Conectado (case_platform_db)' : 'Configurado (.env)'}
          description="RDBMS relacional con soporte UUID v4 y pool de conexiones HikariCP."
          meta={{
            Host: 'localhost:5432',
            Base: 'case_platform_db',
            Estado: health?.databaseStatus || 'PostgreSQL 16.14 Activo',
          }}
        />

        <StatusCard
          title="Frontend Web React"
          status="success"
          icon={<Layers size={22} />}
          value="Activo (Vite + TypeScript)"
          description="SPA modular con Axios, ESLint, Prettier y soporte para APIs REST."
          meta={{
            Tecnología: 'React 18 + TS',
            Herramienta: 'Vite 5',
            Puerto: '5173',
          }}
        />

        <StatusCard
          title="Aplicación Móvil"
          status="info"
          icon={<Smartphone size={22} />}
          value="Estructura Flutter Preparada"
          description="Arquitectura limpia con capas screens, widgets, services, models y providers."
          meta={{
            Lenguaje: 'Dart 3+',
            Framework: 'Flutter',
            Estado: 'Estructura lista',
          }}
        />
      </section>

      {/* Layer Architecture Overview */}
      <section className="architecture-section">
        <div className="section-header">
          <Cpu size={20} />
          <h3>Arquitectura por Capas del Backend</h3>
        </div>
        <div className="layers-diagram">
          <div className="layer-item">
            <span className="layer-badge">Controller</span>
            <h4>Controladores REST</h4>
            <p>
              Recepción de peticiones HTTP, validación inicial de parámetros y exposición
              de endpoints.
            </p>
          </div>
          <div className="layer-arrow">→</div>
          <div className="layer-item">
            <span className="layer-badge">Service</span>
            <h4>Servicios de Negocio</h4>
            <p>Lógica de dominio, orquestación de operaciones y control transaccional.</p>
          </div>
          <div className="layer-arrow">→</div>
          <div className="layer-item">
            <span className="layer-badge">Repository</span>
            <h4>Repositorios JPA</h4>
            <p>
              Acceso a datos con Spring Data JPA y consultas optimizadas sobre PostgreSQL.
            </p>
          </div>
          <div className="layer-arrow">→</div>
          <div className="layer-item">
            <span className="layer-badge">Model / DTO</span>
            <h4>Entidades &amp; DTOs</h4>
            <p>
              Modelado del dominio desacoplado de la representación expuesta al cliente.
            </p>
          </div>
        </div>
      </section>

      {/* Project Modules Overview */}
      <section className="roadmap-section">
        <div className="section-header">
          <FileCode2 size={20} />
          <h3>Módulos Principales de CASE Platform</h3>
        </div>
        <div className="phases-list">
          {phases.map((phase) => (
            <div
              key={phase.id}
              className={`phase-card ${phase.ready ? 'completed' : ''}`}
            >
              <div className="phase-card-header">
                <div className="phase-tag">
                  {phase.ready ? (
                    <CheckCircle2 size={16} className="text-green" />
                  ) : (
                    <Terminal size={16} />
                  )}
                  <span>{phase.id}</span>
                </div>
                <span
                  className={`phase-status-pill ${phase.ready ? 'ready' : 'upcoming'}`}
                >
                  {phase.status}
                </span>
              </div>
              <h4 className="phase-name">{phase.name}</h4>
              <p className="phase-desc">{phase.desc}</p>
            </div>
          ))}
        </div>
      </section>
    </main>
  );
};
