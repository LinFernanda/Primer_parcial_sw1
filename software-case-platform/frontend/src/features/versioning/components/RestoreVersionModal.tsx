import React, { useState } from 'react';
import { AlertTriangle, RotateCcw, X } from 'lucide-react';
import { VersionModeloDTO } from '../models/version.types';

interface RestoreVersionModalProps {
  isOpen: boolean;
  version: VersionModeloDTO | null;
  onClose: () => void;
  onConfirm: (versionId: number, comentario: string) => Promise<void>;
}

export const RestoreVersionModal: React.FC<RestoreVersionModalProps> = ({
  isOpen,
  version,
  onClose,
  onConfirm,
}) => {
  const [comentario, setComentario] = useState('');
  const [isRestoring, setIsRestoring] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen || !version) return null;

  const handleRestore = async () => {
    setIsRestoring(true);
    setError(null);
    try {
      await onConfirm(version.id, comentario.trim());
      setComentario('');
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Error al restaurar la versión');
    } finally {
      setIsRestoring(false);
    }
  };

  return (
    <div className="uml-modal-overlay">
      <div className="uml-modal-content" style={{ maxWidth: '460px' }}>
        <div className="uml-modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <AlertTriangle size={20} style={{ color: '#f59e0b' }} />
            <h3 style={{ margin: 0, fontSize: '18px', color: '#f8fafc' }}>Restaurar Versión</h3>
          </div>
          <button className="uml-modal-close" onClick={onClose} disabled={isRestoring}>
            <X size={18} />
          </button>
        </div>

        <div className="uml-modal-body">
          {error && <div className="uml-error-alert" style={{ marginBottom: '12px' }}>{error}</div>}

          <div
            style={{
              backgroundColor: 'rgba(245, 158, 11, 0.1)',
              border: '1px solid rgba(245, 158, 11, 0.3)',
              borderRadius: '6px',
              padding: '12px',
              marginBottom: '16px',
            }}
          >
            <p style={{ margin: 0, fontSize: '13px', color: '#fde68a', lineHeight: '1.4' }}>
              ¿Está seguro de restaurar el modelo al estado de <strong>{version.nombreVersion} ({version.numeroVersion})</strong>?
            </p>
            <p style={{ margin: '6px 0 0 0', fontSize: '12px', color: '#cbd5e1' }}>
              El estado actual del diagrama será reemplazado íntegramente por este snapshot. Esta operación quedará registrada en la trazabilidad del sistema.
            </p>
          </div>

          <div style={{ fontSize: '12px', color: '#94a3b8', marginBottom: '14px', lineHeight: '1.6' }}>
            <div>&bull; <strong>Fecha de Creación:</strong> {new Date(version.fechaCreacion).toLocaleString()}</div>
            <div>&bull; <strong>Creado por:</strong> {version.usuarioCreadorNombre || version.usuarioCreadorEmail || 'Usuario'}</div>
            {version.descripcion && <div>&bull; <strong>Nota:</strong> {version.descripcion}</div>}
          </div>

          <div className="uml-form-group">
            <label className="uml-label">Motivo o Comentario de Restauración (Opcional)</label>
            <input
              type="text"
              className="uml-input"
              placeholder="Ej. Reversión de cambios tras revisión con el cliente"
              value={comentario}
              onChange={(e) => setComentario(e.target.value)}
              disabled={isRestoring}
            />
          </div>
        </div>

        <div className="uml-modal-footer">
          <button
            type="button"
            className="uml-toolbar-btn"
            onClick={onClose}
            disabled={isRestoring}
          >
            Cancelar
          </button>
          <button
            type="button"
            className="uml-toolbar-btn danger"
            onClick={handleRestore}
            disabled={isRestoring}
            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
          >
            <RotateCcw size={14} />
            {isRestoring ? 'Restaurando...' : 'Confirmar Restauración'}
          </button>
        </div>
      </div>
    </div>
  );
};
