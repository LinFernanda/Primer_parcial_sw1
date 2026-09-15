import React, { useState } from 'react';
import { VisibilidadUML } from '../models/uml.types';
import { X } from 'lucide-react';

interface CreateClassModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (nombre: string, visibilidad: VisibilidadUML, descripcion: string) => Promise<void>;
}

export const CreateClassModal: React.FC<CreateClassModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
}) => {
  const [nombre, setNombre] = useState('');
  const [visibilidad, setVisibilidad] = useState<VisibilidadUML>('PUBLIC');
  const [descripcion, setDescripcion] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nombre.trim()) {
      setError('El nombre de la clase es obligatorio');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await onSubmit(nombre.trim(), visibilidad, descripcion.trim());
      setNombre('');
      setDescripcion('');
      onClose();
    } catch (err: any) {
      setError(err.message || 'Error al crear la clase');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="uml-modal-overlay">
      <div className="uml-modal">
        <div className="uml-modal-header">
          <span className="uml-modal-title">Nueva Clase UML</span>
          <button className="uml-del-btn" onClick={onClose}>
            <X size={18} />
          </button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="uml-modal-body">
            {error && (
              <div style={{ color: '#ef4444', fontSize: '12px', background: '#450a0a', padding: '8px', borderRadius: '4px' }}>
                {error}
              </div>
            )}
            <div className="uml-field-group">
              <label className="uml-field-label">Nombre de la Clase *</label>
              <input
                type="text"
                className="uml-field-input"
                placeholder="Ej. Cliente, Factura, Pedido"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                autoFocus
              />
            </div>
            <div className="uml-field-group">
              <label className="uml-field-label">Visibilidad</label>
              <select
                className="uml-field-select"
                value={visibilidad}
                onChange={(e) => setVisibilidad(e.target.value as VisibilidadUML)}
              >
                <option value="PUBLIC">Pública (+)</option>
                <option value="PRIVATE">Privada (-)</option>
                <option value="PROTECTED">Protegida (#)</option>
                <option value="PACKAGE">Paquete (~)</option>
              </select>
            </div>
            <div className="uml-field-group">
              <label className="uml-field-label">Descripción (opcional)</label>
              <input
                type="text"
                className="uml-field-input"
                placeholder="Descripción conceptual de la entidad"
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
              disabled={submitting}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="uml-toolbar-btn primary"
              disabled={submitting}
            >
              {submitting ? 'Creando...' : 'Crear Clase'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
