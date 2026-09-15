import React, { useState } from 'react';
import {
  CARDINALIDADES_DISPONIBLES,
  ClaseUML,
  TipoRelacionUML,
} from '../models/uml.types';
import { X } from 'lucide-react';

interface CreateRelationModalProps {
  isOpen: boolean;
  clases: ClaseUML[];
  onClose: () => void;
  onSubmit: (
    tipoRelacion: TipoRelacionUML,
    origenId: number,
    destinoId: number,
    cardOrigen: string,
    cardDestino: string,
    descripcion: string
  ) => Promise<void>;
}

export const CreateRelationModal: React.FC<CreateRelationModalProps> = ({
  isOpen,
  clases,
  onClose,
  onSubmit,
}) => {
  const [tipo, setTipo] = useState<TipoRelacionUML>('ASOCIACION');
  const [origenId, setOrigenId] = useState<number>(clases[0]?.id || 0);
  const [destinoId, setDestinoId] = useState<number>(clases[1]?.id || clases[0]?.id || 0);
  const [cardOrigen, setCardOrigen] = useState('1');
  const [cardDestino, setCardDestino] = useState('1');
  const [descripcion, setDescripcion] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!origenId || !destinoId) {
      setError('Debes seleccionar una clase de origen y una de destino');
      return;
    }
    if (tipo === 'HERENCIA' && origenId === destinoId) {
      setError('Una clase no puede heredar de sí misma');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await onSubmit(tipo, origenId, destinoId, cardOrigen, cardDestino, descripcion.trim());
      onClose();
    } catch (err: any) {
      setError(err.message || 'Error al crear la relación UML');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="uml-modal-overlay">
      <div className="uml-modal">
        <div className="uml-modal-header">
          <span className="uml-modal-title">Nueva Relación UML</span>
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
              <label className="uml-field-label">Tipo de Relación</label>
              <select
                className="uml-field-select"
                value={tipo}
                onChange={(e) => setTipo(e.target.value as TipoRelacionUML)}
              >
                <option value="ASOCIACION">Asociación (----)</option>
                <option value="HERENCIA">Herencia / Generalización (—▷)</option>
                <option value="DEPENDENCIA">Dependencia (- - - &gt;)</option>
                <option value="AGREGACION">Agregación (◇----)</option>
                <option value="COMPOSICION">Composición (◆----)</option>
              </select>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div className="uml-field-group">
                <label className="uml-field-label">Clase Origen *</label>
                <select
                  className="uml-field-select"
                  value={origenId}
                  onChange={(e) => setOrigenId(parseInt(e.target.value, 10))}
                >
                  {clases.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div className="uml-field-group">
                <label className="uml-field-label">Clase Destino *</label>
                <select
                  className="uml-field-select"
                  value={destinoId}
                  onChange={(e) => setDestinoId(parseInt(e.target.value, 10))}
                >
                  {clases.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.nombre}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {tipo !== 'HERENCIA' && tipo !== 'DEPENDENCIA' && (
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="uml-field-group">
                  <label className="uml-field-label">Cardinalidad Origen</label>
                  <select
                    className="uml-field-select"
                    value={cardOrigen}
                    onChange={(e) => setCardOrigen(e.target.value)}
                  >
                    {CARDINALIDADES_DISPONIBLES.map((card) => (
                      <option key={card} value={card}>
                        {card}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="uml-field-group">
                  <label className="uml-field-label">Cardinalidad Destino</label>
                  <select
                    className="uml-field-select"
                    value={cardDestino}
                    onChange={(e) => setCardDestino(e.target.value)}
                  >
                    {CARDINALIDADES_DISPONIBLES.map((card) => (
                      <option key={card} value={card}>
                        {card}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            )}

            <div className="uml-field-group">
              <label className="uml-field-label">Descripción / Rol (opcional)</label>
              <input
                type="text"
                className="uml-field-input"
                placeholder="Ej. posee, administra, genera"
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
              {submitting ? 'Conectando...' : 'Crear Relación'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
