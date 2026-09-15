import React from 'react';
import { useUMLStore } from '../store/umlStore';
import { useReactFlow } from 'reactflow';
import {
  Maximize,
  Plus,
  Redo,
  Save,
  Trash2,
  Undo,
  ZoomIn,
  ZoomOut,
  Link2,
  Users,
} from 'lucide-react';

interface ToolbarProps {
  onOpenCreateClass: () => void;
  onOpenCreateRelation: () => void;
}

export const Toolbar: React.FC<ToolbarProps> = ({
  onOpenCreateClass,
  onOpenCreateRelation,
}) => {
  const {
    modelo,
    selectedClassId,
    selectedRelationId,
    deleteSelected,
    savePositionChanges,
    isSaving,
    undo,
    redo,
    history,
    future,
    connectedUsers,
    isWsConnected,
  } = useUMLStore();

  const { zoomIn, zoomOut, fitView } = useReactFlow();

  const hasSelection = !!selectedClassId || !!selectedRelationId;

  return (
    <div className="uml-toolbar">
      {/* Grupo Izquierdo: Creación y Edición */}
      <div className="uml-toolbar-group">
        <button
          className="uml-toolbar-btn primary"
          onClick={onOpenCreateClass}
          title="Crear nueva clase UML en la pizarra"
        >
          <Plus size={16} /> Nueva Clase
        </button>

        <button
          className="uml-toolbar-btn"
          onClick={onOpenCreateRelation}
          disabled={!modelo?.clases || modelo.clases.length < 2}
          title="Conectar dos clases con una relación UML"
        >
          <Link2 size={16} /> Conectar Relación
        </button>

        <div className="uml-toolbar-divider" />

        <button
          className="uml-toolbar-btn danger"
          onClick={deleteSelected}
          disabled={!hasSelection}
          title="Eliminar elemento seleccionado (Delete)"
        >
          <Trash2 size={16} /> Eliminar
        </button>
      </div>

      {/* Grupo Centro: Historial y Guardado */}
      <div className="uml-toolbar-group">
        <button
          className="uml-toolbar-btn"
          onClick={undo}
          disabled={history.length === 0}
          title="Deshacer (Ctrl + Z)"
        >
          <Undo size={16} />
        </button>

        <button
          className="uml-toolbar-btn"
          onClick={redo}
          disabled={future.length === 0}
          title="Rehacer (Ctrl + Y)"
        >
          <Redo size={16} />
        </button>

        <div className="uml-toolbar-divider" />

        <button
          className="uml-toolbar-btn"
          onClick={savePositionChanges}
          disabled={isSaving}
          title="Guardar posiciones y modelo en el servidor"
        >
          <Save size={16} /> {isSaving ? 'Guardando...' : 'Guardar'}
        </button>
      </div>

      {/* Grupo Colaborativo: Presencia y Estado WebSocket */}
      <div className="uml-toolbar-group" style={{ marginLeft: 'auto', marginRight: '12px' }}>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            fontSize: '12px',
            padding: '3px 8px',
            borderRadius: '12px',
            backgroundColor: isWsConnected ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
            border: `1px solid ${isWsConnected ? '#10b981' : '#ef4444'}`,
            color: isWsConnected ? '#34d399' : '#f87171',
          }}
          title={isWsConnected ? 'Servidor colaborativo en tiempo real conectado' : 'Desconectado'}
        >
          <span
            style={{
              width: 8,
              height: 8,
              borderRadius: '50%',
              backgroundColor: isWsConnected ? '#10b981' : '#ef4444',
            }}
          />
          <span>{isWsConnected ? 'En Vivo' : 'Desconectado'}</span>
        </div>

        {/* Lista de Usuarios Conectados */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <Users size={14} style={{ color: '#94a3b8', marginRight: '2px' }} />
          {connectedUsers.map((u) => (
            <span
              key={u.usuario}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: '24px',
                height: '24px',
                borderRadius: '50%',
                backgroundColor: u.color || '#3b82f6',
                color: '#ffffff',
                fontSize: '11px',
                fontWeight: 600,
                border: '2px solid #1e293b',
                cursor: 'default',
              }}
              title={`● ${u.nombre || u.usuario} (${u.usuario})`}
            >
              {(u.nombre || u.usuario).charAt(0).toUpperCase()}
            </span>
          ))}
          {connectedUsers.length > 0 && (
            <span style={{ fontSize: '11px', color: '#94a3b8', marginLeft: '4px' }}>
              {connectedUsers.length} en línea
            </span>
          )}
        </div>
      </div>

      {/* Grupo Derecho: Controles de Vista y Zoom */}
      <div className="uml-toolbar-group">
        <button className="uml-toolbar-btn" onClick={() => zoomIn()} title="Acercar (+)">
          <ZoomIn size={16} />
        </button>
        <button className="uml-toolbar-btn" onClick={() => zoomOut()} title="Alejar (-)">
          <ZoomOut size={16} />
        </button>
        <button className="uml-toolbar-btn" onClick={() => fitView({ padding: 0.2 })} title="Ajustar al lienzo">
          <Maximize size={16} />
        </button>
      </div>
    </div>
  );
};
