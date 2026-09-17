import React from 'react';
import { Loader2, CheckCircle2, Circle, Eye, Cpu, Database } from 'lucide-react';

interface ProcessingStatusProps {
  currentStepMessage: string;
}

interface StepItem {
  id: number;
  label: string;
  icon: React.ReactNode;
}

const STEPS: StepItem[] = [
  {
    id: 1,
    label: 'Procesando imagen...',
    icon: <Eye size={18} />,
  },
  {
    id: 2,
    label: 'Detectando clases...',
    icon: <Cpu size={18} />,
  },
  {
    id: 3,
    label: 'Generando modelo UML...',
    icon: <Database size={18} />,
  },
];

export const ProcessingStatus: React.FC<ProcessingStatusProps> = ({ currentStepMessage }) => {
  // Determinar en qué fase se encuentra según el mensaje actual
  const getStepStatus = (stepId: number) => {
    if (currentStepMessage.toLowerCase().includes('procesando')) {
      if (stepId === 1) return 'active';
      return 'pending';
    }
    if (currentStepMessage.toLowerCase().includes('detectando')) {
      if (stepId === 1) return 'completed';
      if (stepId === 2) return 'active';
      return 'pending';
    }
    if (currentStepMessage.toLowerCase().includes('generando')) {
      if (stepId <= 2) return 'completed';
      if (stepId === 3) return 'active';
      return 'pending';
    }
    return stepId === 1 ? 'active' : 'pending';
  };

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        padding: '36px 20px',
        backgroundColor: '#0f172a',
        borderRadius: '12px',
        border: '1px solid #334155',
        textAlign: 'center',
      }}
    >
      {/* Spinner principal animado */}
      <div style={{ position: 'relative', marginBottom: '24px' }}>
        <Loader2
          size={56}
          style={{
            animation: 'spin 1.5s linear infinite',
            color: '#38bdf8',
          }}
        />
      </div>

      <h3 style={{ margin: '0 0 8px', color: '#f8fafc', fontSize: '18px', fontWeight: 600 }}>
        Visión Computacional e Inteligencia Artificial
      </h3>
      <p style={{ margin: '0 0 28px', color: '#94a3b8', fontSize: '14px', maxWidth: '440px' }}>
        Analizando bordes, morfología, conectores, texto y cardinalidades para reconstruir el modelo conceptual.
      </p>

      {/* Lista vertical de pasos obligatorios según especificación */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '16px',
          width: '100%',
          maxWidth: '380px',
          textAlign: 'left',
        }}
      >
        {STEPS.map((step) => {
          const status = getStepStatus(step.id);
          const isCompleted = status === 'completed';
          const isActive = status === 'active';

          return (
            <div
              key={step.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '14px',
                padding: '12px 16px',
                borderRadius: '8px',
                backgroundColor: isActive
                  ? 'rgba(56, 189, 248, 0.12)'
                  : isCompleted
                  ? 'rgba(16, 185, 129, 0.08)'
                  : 'rgba(30, 41, 59, 0.4)',
                border: isActive
                  ? '1px solid #38bdf8'
                  : isCompleted
                  ? '1px solid #10b981'
                  : '1px solid #334155',
                transition: 'all 0.3s ease',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', color: isActive ? '#38bdf8' : isCompleted ? '#10b981' : '#64748b' }}>
                {isCompleted ? (
                  <CheckCircle2 size={20} />
                ) : isActive ? (
                  <Loader2 size={20} style={{ animation: 'spin 1.5s linear infinite' }} />
                ) : (
                  <Circle size={20} />
                )}
              </div>

              <div style={{ flex: 1 }}>
                <span
                  style={{
                    fontSize: '14px',
                    fontWeight: isActive || isCompleted ? 600 : 400,
                    color: isActive ? '#f8fafc' : isCompleted ? '#e2e8f0' : '#64748b',
                  }}
                >
                  {step.label}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};
