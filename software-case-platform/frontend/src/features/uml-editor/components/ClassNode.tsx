import React, { memo } from 'react';
import { Handle, Position, NodeProps } from 'reactflow';
import { ClaseUML, VISIBILIDAD_SIMBOLOS } from '../models/uml.types';
import { useUMLStore } from '../store/umlStore';

interface ClassNodeData {
  clase: ClaseUML;
}

export const ClassNode: React.FC<NodeProps<ClassNodeData>> = memo(({ data, selected }) => {
  const { clase } = data;
  const lockedElements = useUMLStore((s) => s.lockedElements);
  const currentUser =
    typeof window !== 'undefined' && window.localStorage
      ? window.localStorage.getItem('userEmail') || ''
      : '';

  const lock = lockedElements.find((l) => l.elementoId === clase.id?.toString());
  const isLockedByOther = lock && lock.usuario !== currentUser;
  const isLockedBySelf = lock && lock.usuario === currentUser;

  return (
    <div
      className={`uml-class-node ${selected ? 'selected' : ''} ${
        isLockedByOther ? 'locked-by-other' : isLockedBySelf ? 'locked-by-self' : ''
      }`}
    >
      {/* Handles para conexiones React Flow en los 4 extremos */}
      <Handle type="target" position={Position.Top} id="t" style={{ background: '#3b82f6' }} />
      <Handle type="source" position={Position.Top} id="ts" style={{ background: '#3b82f6' }} />

      <Handle type="target" position={Position.Left} id="l" style={{ background: '#3b82f6' }} />
      <Handle type="source" position={Position.Left} id="ls" style={{ background: '#3b82f6' }} />

      {/* Cabecera de la Clase */}
      <div className="uml-class-header">
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}>
          <span className="uml-class-stereotype">&laquo;class&raquo;</span>
          {lock && (
            <span
              style={{
                fontSize: '10px',
                padding: '1px 5px',
                borderRadius: '4px',
                backgroundColor: isLockedByOther ? 'rgba(239, 68, 68, 0.2)' : 'rgba(59, 130, 246, 0.2)',
                color: isLockedByOther ? '#f87171' : '#60a5fa',
                border: `1px solid ${isLockedByOther ? '#ef4444' : '#3b82f6'}`,
              }}
              title={isLockedByOther ? `Bloqueado por ${lock.usuario}` : 'Editando actualmente'}
            >
              {isLockedByOther ? `🔒 ${lock.usuario.split('@')[0]}` : '✏️ Editando'}
            </span>
          )}
        </div>
        <div className="uml-class-title">{clase.nombre}</div>
      </div>

      {/* Compartimento de Atributos */}
      <div className="uml-class-compartment">
        {clase.atributos && clase.atributos.length > 0 ? (
          clase.atributos.map((attr, index) => (
            <div key={attr.id ?? index} className="uml-class-item">
              <span className={`uml-item-vis ${attr.visibilidad}`}>
                {VISIBILIDAD_SIMBOLOS[attr.visibilidad] || '+'}
              </span>
              <span className="uml-item-name">{attr.nombre}</span>:
              <span className="uml-item-type">{attr.tipoDato}</span>
              {attr.valorInicial && <span className="uml-item-val"> = {attr.valorInicial}</span>}
            </div>
          ))
        ) : (
          <div className="uml-class-empty-hint">+ sin atributos</div>
        )}
      </div>

      {/* Compartimento de Métodos */}
      <div className="uml-class-compartment">
        {clase.metodos && clase.metodos.length > 0 ? (
          clase.metodos.map((met, index) => (
            <div key={met.id ?? index} className="uml-class-item">
              <span className={`uml-item-vis ${met.visibilidad}`}>
                {VISIBILIDAD_SIMBOLOS[met.visibilidad] || '+'}
              </span>
              <span className="uml-item-name">{met.nombre}</span>
              <span>({met.parametros || ''})</span>:
              <span className="uml-item-type">{met.tipoRetorno || 'void'}</span>
            </div>
          ))
        ) : (
          <div className="uml-class-empty-hint">+ sin métodos</div>
        )}
      </div>

      <Handle type="target" position={Position.Right} id="r" style={{ background: '#3b82f6' }} />
      <Handle type="source" position={Position.Right} id="rs" style={{ background: '#3b82f6' }} />

      <Handle type="target" position={Position.Bottom} id="b" style={{ background: '#3b82f6' }} />
      <Handle type="source" position={Position.Bottom} id="bs" style={{ background: '#3b82f6' }} />
    </div>
  );
});

ClassNode.displayName = 'ClassNode';
