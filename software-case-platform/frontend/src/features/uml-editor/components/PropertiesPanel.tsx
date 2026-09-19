import React, { useState } from 'react';
import { useUMLStore } from '../store/umlStore';
import {
  TIPOS_DATO_DISPONIBLES,
  VisibilidadUML,
} from '../models/uml.types';
import { Plus, Trash2, X } from 'lucide-react';

export const PropertiesPanel: React.FC = () => {
  const {
    modelo,
    selectedClassId,
    selectedRelationId,
    selectClass,
    selectRelation,
    updateClass,
    deleteClass,
    addAttribute,
    deleteAttribute,
    addMethod,
    deleteMethod,
    deleteRelation,
    lockedElements,
  } = useUMLStore();

  // Estados para nuevo atributo
  const [attrNombre, setAttrNombre] = useState('');
  const [attrTipo, setAttrTipo] = useState('String');
  const [attrVis, setAttrVis] = useState<VisibilidadUML>('PRIVATE');

  // Estados para nuevo método
  const [metNombre, setMetNombre] = useState('');
  const [metTipo, setMetTipo] = useState('void');
  const [metVis, setMetVis] = useState<VisibilidadUML>('PUBLIC');
  const [metParams, setMetParams] = useState('');

  if (!selectedClassId && !selectedRelationId) {
    return (
      <div className="uml-properties-panel">
        <div className="uml-properties-header">
          <span className="uml-properties-title">Propiedades</span>
        </div>
        <div className="uml-properties-body" style={{ color: '#94a3b8', fontSize: '13px', textAlign: 'center', marginTop: '40px' }}>
          Selecciona una clase o una relación en la pizarra para editar sus atributos, métodos y cardinalidades.
        </div>
      </div>
    );
  }

  // -------------------------------------------------------------------------
  // PANEL PARA RELACIÓN SELECCIONADA
  // -------------------------------------------------------------------------
  if (selectedRelationId) {
    const relacion = modelo?.relaciones.find((r) => r.id === selectedRelationId);
    if (!relacion) return null;

    return (
      <div className="uml-properties-panel">
        <div className="uml-properties-header">
          <span className="uml-properties-title">Propiedades de Relación</span>
          <button className="uml-del-btn" onClick={() => selectRelation(null)}>
            <X size={16} />
          </button>
        </div>
        <div className="uml-properties-body">
          <div className="uml-field-group">
            <span className="uml-field-label">Tipo de Relación</span>
            <span style={{ fontWeight: 600, color: '#38bdf8' }}>{relacion.tipoRelacion}</span>
          </div>

          <div className="uml-field-group">
            <span className="uml-field-label">Conexión</span>
            <span style={{ fontSize: '13px' }}>
              {relacion.claseOrigenNombre || `Clase #${relacion.claseOrigenId}`} &rarr;{' '}
              {relacion.claseDestinoNombre || `Clase #${relacion.claseDestinoId}`}
            </span>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div className="uml-field-group">
              <span className="uml-field-label">Cardinalidad Origen</span>
              <span style={{ fontWeight: 600 }}>{relacion.cardinalidadOrigen || '1'}</span>
            </div>
            <div className="uml-field-group">
              <span className="uml-field-label">Cardinalidad Destino</span>
              <span style={{ fontWeight: 600 }}>{relacion.cardinalidadDestino || '1'}</span>
            </div>
          </div>

          {relacion.descripcion && (
            <div className="uml-field-group">
              <span className="uml-field-label">Descripción</span>
              <span style={{ fontSize: '13px', color: '#cbd5e1' }}>{relacion.descripcion}</span>
            </div>
          )}

          <button
            className="uml-toolbar-btn danger"
            style={{ marginTop: '16px', justifyContent: 'center' }}
            onClick={() => deleteRelation(relacion.id!)}
          >
            <Trash2 size={14} /> Eliminar Relación
          </button>
        </div>
      </div>
    );
  }

  // -------------------------------------------------------------------------
  // PANEL PARA CLASE SELECCIONADA
  // -------------------------------------------------------------------------
  const clase = modelo?.clases.find((c) => c.id === selectedClassId);
  if (!clase) return null;

  const currentUser = (() => {
    if (typeof window === 'undefined' || !window.localStorage) return '';
    const stored = window.localStorage.getItem('userEmail');
    if (stored) return stored;
    try {
      const userObj = JSON.parse(window.localStorage.getItem('user') || '{}');
      return userObj.email || '';
    } catch {
      return '';
    }
  })();
  const classLock = lockedElements.find((l) => l.elementoId === selectedClassId?.toString());
  const isLockedByOther = !!classLock && classLock.usuario !== currentUser;

  const handleAddAttribute = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!attrNombre.trim() || isLockedByOther) return;
    await addAttribute(clase.id!, {
      nombre: attrNombre.trim(),
      tipoDato: attrTipo,
      visibilidad: attrVis,
    });
    setAttrNombre('');
  };

  const handleAddMethod = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!metNombre.trim() || isLockedByOther) return;
    await addMethod(clase.id!, {
      nombre: metNombre.trim(),
      tipoRetorno: metTipo,
      visibilidad: metVis,
      parametros: metParams.trim(),
    });
    setMetNombre('');
    setMetParams('');
  };

  return (
    <div className="uml-properties-panel">
      <div className="uml-properties-header">
        <span className="uml-properties-title">Clase: {clase.nombre}</span>
        <button className="uml-del-btn" onClick={() => selectClass(null)}>
          <X size={16} />
        </button>
      </div>

      <div className="uml-properties-body">
        {/* Aviso de Conflicto de Concurrencia */}
        {isLockedByOther && (
          <div
            style={{
              padding: '8px 10px',
              marginBottom: '14px',
              borderRadius: '6px',
              backgroundColor: 'rgba(239, 68, 68, 0.15)',
              border: '1px solid #ef4444',
              color: '#fca5a5',
              fontSize: '12px',
              lineHeight: '1.4',
            }}
          >
            🔒 <strong>Elemento bloqueado:</strong> Modificado por{' '}
            <strong>{classLock.usuario}</strong>. Edición deshabilitada temporalmente para evitar inconsistencias.
          </div>
        )}

        {/* Nombre de Clase */}
        <div className="uml-field-group">
          <label className="uml-field-label">Nombre</label>
          <input
            type="text"
            className="uml-field-input"
            value={clase.nombre}
            disabled={isLockedByOther}
            onChange={(e) => updateClass(clase.id!, { nombre: e.target.value })}
          />
        </div>

        {/* Visibilidad de Clase */}
        <div className="uml-field-group">
          <label className="uml-field-label">Visibilidad</label>
          <select
            className="uml-field-select"
            value={clase.visibilidad}
            disabled={isLockedByOther}
            onChange={(e) =>
              updateClass(clase.id!, { visibilidad: e.target.value as VisibilidadUML })
            }
          >
            <option value="PUBLIC">Pública (+)</option>
            <option value="PACKAGE">Paquete (~)</option>
            <option value="PROTECTED">Protegida (#)</option>
            <option value="PRIVATE">Privada (-)</option>
          </select>
        </div>

        {/* SECCIÓN ATRIBUTOS */}
        <div className="uml-section-header">
          <span className="uml-section-title">Atributos ({clase.atributos?.length || 0})</span>
        </div>

        <div className="uml-item-list">
          {clase.atributos &&
            clase.atributos.map((attr) => (
              <div key={attr.id} className="uml-item-row">
                <div>
                  <span className={`uml-item-vis ${attr.visibilidad}`}>
                    {attr.visibilidad === 'PUBLIC'
                      ? '+'
                      : attr.visibilidad === 'PRIVATE'
                      ? '-'
                      : '#'}
                  </span>{' '}
                  <strong>{attr.nombre}</strong>: {attr.tipoDato}
                </div>
                <button
                  className="uml-del-btn"
                  onClick={() => deleteAttribute(attr.id!)}
                  disabled={isLockedByOther}
                  title="Eliminar atributo"
                >
                  <Trash2 size={12} />
                </button>
              </div>
            ))}
        </div>

        {/* Formulario para agregar atributo */}
        <form onSubmit={handleAddAttribute} style={{ display: 'flex', flexDirection: 'column', gap: '8px', background: '#0f172a', padding: '10px', borderRadius: '6px' }}>
          <div style={{ display: 'flex', gap: '6px' }}>
            <select
              className="uml-field-select"
              style={{ width: '45px', padding: '4px 2px', textAlign: 'center' }}
              value={attrVis}
              disabled={isLockedByOther}
              onChange={(e) => setAttrVis(e.target.value as VisibilidadUML)}
              title="Visibilidad"
            >
              <option value="PRIVATE">-</option>
              <option value="PUBLIC">+</option>
              <option value="PROTECTED">#</option>
              <option value="PACKAGE">~</option>
            </select>
            <input
              type="text"
              placeholder="nombre"
              className="uml-field-input"
              style={{ flex: 1, padding: '4px 8px' }}
              value={attrNombre}
              disabled={isLockedByOther}
              onChange={(e) => setAttrNombre(e.target.value)}
            />
            <select
              className="uml-field-select"
              style={{ width: '90px', padding: '4px 6px' }}
              value={attrTipo}
              disabled={isLockedByOther}
              onChange={(e) => setAttrTipo(e.target.value)}
            >
              {TIPOS_DATO_DISPONIBLES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </div>
          <button type="submit" className="uml-small-btn" disabled={isLockedByOther} style={{ alignSelf: 'flex-start' }}>
            <Plus size={12} /> Agregar Atributo
          </button>
        </form>

        {/* SECCIÓN MÉTODOS */}
        <div className="uml-section-header">
          <span className="uml-section-title">Métodos ({clase.metodos?.length || 0})</span>
        </div>

        <div className="uml-item-list">
          {clase.metodos &&
            clase.metodos.map((met) => (
              <div key={met.id} className="uml-item-row">
                <div>
                  <span className={`uml-item-vis ${met.visibilidad}`}>
                    {met.visibilidad === 'PUBLIC' ? '+' : met.visibilidad === 'PRIVATE' ? '-' : '#'}
                  </span>{' '}
                  <strong>{met.nombre}</strong>({met.parametros || ''}): {met.tipoRetorno}
                </div>
                <button
                  className="uml-del-btn"
                  onClick={() => deleteMethod(met.id!)}
                  disabled={isLockedByOther}
                  title="Eliminar método"
                >
                  <Trash2 size={12} />
                </button>
              </div>
            ))}
        </div>

        {/* Formulario para agregar método */}
        <form onSubmit={handleAddMethod} style={{ display: 'flex', flexDirection: 'column', gap: '8px', background: '#0f172a', padding: '10px', borderRadius: '6px' }}>
          <div style={{ display: 'flex', gap: '6px' }}>
            <select
              className="uml-field-select"
              style={{ width: '45px', padding: '4px 2px', textAlign: 'center' }}
              value={metVis}
              disabled={isLockedByOther}
              onChange={(e) => setMetVis(e.target.value as VisibilidadUML)}
              title="Visibilidad"
            >
              <option value="PUBLIC">+</option>
              <option value="PRIVATE">-</option>
              <option value="PROTECTED">#</option>
              <option value="PACKAGE">~</option>
            </select>
            <input
              type="text"
              placeholder="nombreMétodo"
              className="uml-field-input"
              style={{ flex: 1, padding: '4px 8px' }}
              value={metNombre}
              disabled={isLockedByOther}
              onChange={(e) => setMetNombre(e.target.value)}
            />
            <select
              className="uml-field-select"
              style={{ width: '80px', padding: '4px 6px' }}
              value={metTipo}
              disabled={isLockedByOther}
              onChange={(e) => setMetTipo(e.target.value)}
            >
              <option value="void">void</option>
              <option value="String">String</option>
              <option value="Integer">Integer</option>
              <option value="Boolean">Boolean</option>
              <option value="Double">Double</option>
              <option value="Object">Object</option>
            </select>
          </div>
          <button type="submit" className="uml-small-btn" disabled={isLockedByOther} style={{ alignSelf: 'flex-start' }}>
            <Plus size={12} /> Agregar Método
          </button>
        </form>

        {/* Eliminar Clase */}
        <button
          className="uml-toolbar-btn danger"
          style={{ marginTop: '20px', justifyContent: 'center' }}
          disabled={isLockedByOther}
          onClick={() => deleteClass(clase.id!)}
        >
          <Trash2 size={14} /> Eliminar Clase
        </button>
      </div>
    </div>
  );
};
