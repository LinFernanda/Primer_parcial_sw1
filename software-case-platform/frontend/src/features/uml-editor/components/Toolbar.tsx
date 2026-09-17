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
  History,
  Sparkles,
  Camera,
  Code2,
  FolderSync,
} from 'lucide-react';

interface ToolbarProps {
  onOpenCreateClass: () => void;
  onOpenCreateRelation: () => void;
  onOpenVersioning?: () => void;
  onOpenAIChat?: () => void;
  onOpenImageUML?: () => void;
  onOpenGenerator?: () => void;
  onOpenEnterpriseArchitect?: () => void;
}

export const Toolbar: React.FC<ToolbarProps> = ({
  onOpenCreateClass,
  onOpenCreateRelation,
  onOpenVersioning,
  onOpenAIChat,
  onOpenImageUML,
  onOpenGenerator,
  onOpenEnterpriseArchitect,
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

        <div className="uml-toolbar-divider" />

        <button
          className="uml-toolbar-btn"
          onClick={onOpenVersioning}
          title="Control de versiones, snapshots e historial de cambios (Fase 6)"
        >
          <History size={16} style={{ color: '#38bdf8' }} /> Versiones
        </button>

        <div className="uml-toolbar-divider" />

        <button
          className="uml-toolbar-btn"
          onClick={onOpenAIChat}
          title="Abrir Asistente IA para edición inteligente del diagrama UML (Fase 7)"
          style={{
            background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(139, 92, 246, 0.2) 100%)',
            border: '1px solid rgba(139, 92, 246, 0.5)',
            color: '#c084fc',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            fontWeight: 600,
          }}
        >
          <Sparkles size={16} style={{ color: '#a855f7' }} /> Asistente IA
        </button>

        <div className="uml-toolbar-divider" />

        <button
          className="uml-toolbar-btn"
          onClick={onOpenImageUML}
          title="Convertir imagen de diagrama a modelo UML editable con Visión Artificial (Fase 8)"
          style={{
            background: 'linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(56, 189, 248, 0.15) 100%)',
            border: '1px solid rgba(56, 189, 248, 0.5)',
            color: '#38bdf8',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            fontWeight: 600,
          }}
        >
          <Camera size={16} style={{ color: '#38bdf8' }} /> Visión UML
        </button>

        <div className="uml-toolbar-divider" />

        <button
          className="uml-toolbar-btn"
          onClick={onOpenGenerator}
          title="Generar proyecto backend Spring Boot ejecutable desde el modelo UML (Fase 9)"
          style={{
            background: 'linear-gradient(135deg, rgba(37, 99, 235, 0.15) 0%, rgba(16, 185, 129, 0.15) 100%)',
            border: '1px solid rgba(37, 99, 235, 0.5)',
            color: '#60a5fa',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            fontWeight: 600,
          }}
        >
          <Code2 size={16} style={{ color: '#60a5fa' }} /> Generar Backend
        </button>

        <div className="uml-toolbar-divider" />

        <button
          className="uml-toolbar-btn"
          onClick={onOpenEnterpriseArchitect}
          title="Interoperabilidad con Enterprise Architect: Importar y Exportar modelos en formato XMI (Fase 10)"
          style={{
            background: 'linear-gradient(135deg, rgba(245, 158, 11, 0.15) 0%, rgba(217, 119, 6, 0.15) 100%)',
            border: '1px solid rgba(245, 158, 11, 0.5)',
            color: '#f59e0b',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            fontWeight: 600,
          }}
        >
          <FolderSync size={16} style={{ color: '#f59e0b' }} /> Enterprise Architect
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
