import React from 'react';
import { AICommandHistory } from '../models/ai.types';
import { CheckCircle2, AlertCircle, Clock, Terminal } from 'lucide-react';

interface CommandHistoryProps {
  history: AICommandHistory[];
  isLoading: boolean;
  onSelectPrompt?: (prompt: string) => void;
}

export const CommandHistory: React.FC<CommandHistoryProps> = ({
  history,
  isLoading,
  onSelectPrompt,
}) => {
  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: '24px 12px', color: '#94a3b8', fontSize: '13px' }}>
        Cargando historial de comandos...
      </div>
    );
  }

  if (history.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '32px 12px', color: '#64748b' }}>
        <Terminal size={28} style={{ margin: '0 auto 8px', opacity: 0.5 }} />
        <p style={{ margin: 0, fontSize: '13px' }}>No hay comandos ejecutados todavía.</p>
        <p style={{ margin: '4px 0 0', fontSize: '11px' }}>
          Pruebe dictar o escribir: <em>"Crear clase Cliente"</em>
        </p>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
      {history.map((cmd) => (
        <div
          key={cmd.id}
          style={{
            backgroundColor: '#1e293b',
            border: '1px solid #334155',
            borderRadius: '6px',
            padding: '10px 12px',
            cursor: onSelectPrompt ? 'pointer' : 'default',
            transition: 'border-color 0.15s ease',
          }}
          onClick={() => onSelectPrompt && onSelectPrompt(cmd.promptOriginal || cmd.prompt || '')}
          title={onSelectPrompt ? 'Haga clic para reutilizar este comando' : undefined}
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              {(cmd.exitoso ?? cmd.ejecutadoConExito) ? (
                <CheckCircle2 size={14} style={{ color: '#10b981' }} />
              ) : (
                <AlertCircle size={14} style={{ color: '#ef4444' }} />
              )}
              <span
                style={{
                  fontSize: '10px',
                  fontWeight: 700,
                  backgroundColor: 'rgba(59, 130, 246, 0.15)',
                  color: '#60a5fa',
                  padding: '1px 5px',
                  borderRadius: '3px',
                }}
              >
                {cmd.tipoOperacion || cmd.operacion}
              </span>
            </div>
            <span style={{ fontSize: '10px', color: '#64748b', display: 'flex', alignItems: 'center', gap: '3px' }}>
              <Clock size={10} />
              {new Date(cmd.fecha || Date.now()).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
          </div>

          <div style={{ fontSize: '12px', color: '#f8fafc', fontWeight: 500, wordBreak: 'break-word' }}>
            "{cmd.promptOriginal || cmd.prompt}"
          </div>

          {cmd.respuestaGenerada && (
            <div style={{ fontSize: '11px', color: '#94a3b8', marginTop: '4px', borderTop: '1px solid #334155', paddingTop: '4px' }}>
              {cmd.respuestaGenerada}
            </div>
          )}
        </div>
      ))}
    </div>
  );
};
