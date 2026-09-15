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
