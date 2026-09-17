import React, { useState } from 'react';
import { BookmarkPlus, X } from 'lucide-react';

interface CreateVersionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (nombre: string, numeroVersion: string, descripcion: string) => Promise<void>;
  currentVersion?: string;
}

export const CreateVersionModal: React.FC<CreateVersionModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  currentVersion = '1.0',
}) => {
  const [nombre, setNombre] = useState('');
  const [numeroVersion, setNumeroVersion] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nombre.trim()) {
      setError('El nombre de la versión es obligatorio');
      return;
    }

    setIsSubmitting(true);
    setError(null);
    try {
      await onSubmit(nombre.trim(), numeroVersion.trim(), descripcion.trim());
      setNombre('');
      setNumeroVersion('');
      setDescripcion('');
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Error al crear la versión');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="uml-modal-overlay">
      <div className="uml-modal-content" style={{ maxWidth: '480px' }}>
        <div className="uml-modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <BookmarkPlus size={20} style={{ color: '#3b82f6' }} />
            <h3 style={{ margin: 0, fontSize: '18px', color: '#f8fafc' }}>Crear Nueva Versión (Snapshot)</h3>
          </div>
          <button className="uml-modal-close" onClick={onClose} disabled={isSubmitting}>
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="uml-modal-body">
            {error && <div className="uml-error-alert" style={{ marginBottom: '12px' }}>{error}</div>}

            <p style={{ fontSize: '13px', color: '#94a3b8', marginTop: 0, marginBottom: '14px' }}>
              Un snapshot congelará el estado actual del diagrama conceptual (todas las clases, atributos, métodos, relaciones y posiciones visuales).
            </p>

            <div className="uml-form-group">
              <label className="uml-label">Nombre de la Versión *</label>
              <input
                type="text"
                className="uml-input"
                placeholder="Ej. Modelo Conceptual Inicial, Sprint 1 MVP"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                autoFocus
                required
              />
            </div>

            <div className="uml-form-group">
              <label className="uml-label">Número de Versión (Opcional)</label>
              <input
                type="text"
                className="uml-input"
                placeholder={`Ej. v${currentVersion} o automático`}
                value={numeroVersion}
                onChange={(e) => setNumeroVersion(e.target.value)}
              />
              <span style={{ fontSize: '11px', color: '#64748b' }}>
                Si se deja en blanco, el sistema asignará correlativamente (v1.0, v2.0, etc.).
              </span>
            </div>

            <div className="uml-form-group">
              <label className="uml-label">Descripción o Notas de la Versión</label>
              <textarea
                className="uml-input"
                rows={3}
                placeholder="Detalle los objetivos del hito, cambios destacados o decisiones de diseño..."
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
              />
            </div>
          </div>

          <div className="uml-modal-footer">
            <button
              type="button"
              className="uml-toolbar-btn"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="uml-toolbar-btn primary"
              disabled={isSubmitting || !nombre.trim()}
            >
              {isSubmitting ? 'Guardando...' : 'Crear Versión'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
