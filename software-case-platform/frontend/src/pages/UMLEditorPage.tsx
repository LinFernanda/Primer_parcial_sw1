import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ReactFlowProvider } from 'reactflow';
import { useUMLStore } from '../features/uml-editor/store/umlStore';
import { umlService } from '../features/uml-editor/services/umlService';
import { Toolbar } from '../features/uml-editor/components/Toolbar';
import { UMLCanvas } from '../features/uml-editor/components/UMLCanvas';
import { PropertiesPanel } from '../features/uml-editor/components/PropertiesPanel';
import { CreateClassModal } from '../features/uml-editor/components/CreateClassModal';
import { CreateRelationModal } from '../features/uml-editor/components/CreateRelationModal';
import {
  TipoRelacionUML,
  VisibilidadUML,
  ProyectoUML,
} from '../features/uml-editor/models/uml.types';
import { websocketService } from '../features/uml-editor/services/websocketService';
import { VersionPanel } from '../features/versioning';
import { AIChatPanel } from '../features/ai';
import { ImageUMLModal } from '../features/imageuml';
import { CodeGeneratorModal } from '../features/generator';
import { EnterpriseArchitectModal } from '../features/enterprisearchitect';
import '../features/uml-editor/styles/uml-editor.css';
import { ArrowLeft, Layers } from 'lucide-react';

export const UMLEditorPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const proyectoId = parseInt(id || '0', 10);

  const [proyecto, setProyecto] = useState<ProyectoUML | null>(null);
  const [selectedModeloId, setSelectedModeloId] = useState<number | null>(null);
  const [isClassModalOpen, setIsClassModalOpen] = useState(false);
  const [isRelationModalOpen, setIsRelationModalOpen] = useState(false);
  const [isVersioningOpen, setIsVersioningOpen] = useState(false);
  const [isAIChatOpen, setIsAIChatOpen] = useState(false);
  const [isImageUMLOpen, setIsImageUMLOpen] = useState(false);
  const [isGeneratorOpen, setIsGeneratorOpen] = useState(false);
  const [isEAOpen, setIsEAOpen] = useState(false);

  const {
    modelo,
    loadModelo,
    createClass,
    createRelation,
    deleteSelected,
    restoreModeloState,
    undo,
    redo,
    error,
    clearError,
    applyRemoteEvent,
    setConnectedUsers,
    setLockedElements,
    setWsConnected,
  } = useUMLStore();

  // Cargar proyecto y su modelo inicial
  useEffect(() => {
    if (!proyectoId) return;

    const fetchProyecto = async () => {
      try {
        const p = await umlService.getProyectoById(proyectoId);
        setProyecto(p);
        if (p.modelos && p.modelos.length > 0) {
          const modId = p.modelos[0].id;
          setSelectedModeloId(modId);
          await loadModelo(modId);
        }
      } catch (err) {
        console.error('Error al cargar proyecto:', err);
      }
    };

    fetchProyecto();
  }, [proyectoId, loadModelo]);

  // Conexión y sincronización colaborativa en tiempo real por WebSocket
  useEffect(() => {
    if (!selectedModeloId) return;

    const userEmail = localStorage.getItem('userEmail') || 'ingeniero@caseplatform.com';
    const userName = localStorage.getItem('userName') || userEmail.split('@')[0];

    websocketService.connect(
      selectedModeloId,
      userEmail,
      userName,
      (event) => applyRemoteEvent(event),
      (usuarios) => setConnectedUsers(usuarios),
      (bloqueos) => setLockedElements(bloqueos),
      (status) => setWsConnected(status)
    );

    return () => {
      websocketService.disconnect();
    };
  }, [selectedModeloId, applyRemoteEvent, setConnectedUsers, setLockedElements, setWsConnected]);

  // Manejo de atajos de teclado (Ctrl+Z, Ctrl+Y, Delete)
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Ignorar si el usuario está escribiendo en un input o select
      const activeTag = document.activeElement?.tagName.toLowerCase();
      if (activeTag === 'input' || activeTag === 'textarea' || activeTag === 'select') {
        return;
      }

      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z') {
        e.preventDefault();
        undo();
      } else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'y') {
        e.preventDefault();
        redo();
      } else if (e.key === 'Delete' || e.key === 'Backspace') {
        e.preventDefault();
        deleteSelected();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [undo, redo, deleteSelected]);

  const handleCreateClass = async (
    nombre: string,
    visibilidad: VisibilidadUML,
    descripcion: string
  ) => {
    await createClass(nombre, 200, 150, visibilidad, descripcion);
  };

  const handleCreateRelation = async (
    tipoRelacion: TipoRelacionUML,
    origenId: number,
    destinoId: number,
    cardOrigen: string,
    cardDestino: string,
    descripcion: string
  ) => {
    await createRelation({
      tipoRelacion,
      claseOrigenId: origenId,
      claseDestinoId: destinoId,
      cardinalidadOrigen: cardOrigen,
      cardinalidadDestino: cardDestino,
      descripcion,
    });
  };

  return (
    <ReactFlowProvider>
      <div className="uml-editor-layout">
        {/* Barra superior contextual con nombre del proyecto y versión del modelo */}
        <div style={{ background: '#0f172a', borderBottom: '1px solid #334155', padding: '8px 16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <Link to="/projects" className="uml-toolbar-btn" style={{ textDecoration: 'none' }}>
              <ArrowLeft size={14} /> Proyectos
            </Link>
            <span style={{ fontSize: '15px', fontWeight: 600, color: '#f8fafc' }}>
              {proyecto?.nombre || 'Cargando Proyecto...'}
            </span>
            <span style={{ fontSize: '12px', color: '#94a3b8' }}>
              &bull; Modelo: <strong>{modelo?.nombre || 'Diagrama'}</strong> (v{modelo?.version || '1.0'})
            </span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            {proyecto?.modelos && proyecto.modelos.length > 1 && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px' }}>
                <Layers size={14} /> Versión:
                <select
                  className="uml-field-select"
                  style={{ padding: '2px 6px', fontSize: '12px' }}
                  value={selectedModeloId || ''}
                  onChange={(e) => {
                    const id = parseInt(e.target.value, 10);
                    setSelectedModeloId(id);
                    loadModelo(id);
                  }}
                >
                  {proyecto.modelos.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.nombre} (v{m.version})
                    </option>
                  ))}
                </select>
              </div>
            )}
            {error && (
              <div style={{ color: '#ef4444', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span>{error}</span>
                <button className="uml-del-btn" onClick={clearError}>&times;</button>
              </div>
            )}
          </div>
        </div>

        {/* Toolbar de Acciones */}
        <Toolbar
          onOpenCreateClass={() => setIsClassModalOpen(true)}
          onOpenCreateRelation={() => setIsRelationModalOpen(true)}
          onOpenVersioning={() => setIsVersioningOpen(true)}
          onOpenAIChat={() => setIsAIChatOpen((prev) => !prev)}
          onOpenImageUML={() => setIsImageUMLOpen(true)}
          onOpenGenerator={() => setIsGeneratorOpen(true)}
          onOpenEnterpriseArchitect={() => setIsEAOpen(true)}
        />

        {/* Área Central: Canvas + Panel Lateral */}
        <div className="uml-workspace">
          <UMLCanvas />
          <PropertiesPanel />
        </div>

        {/* Modales */}
        <CreateClassModal
          isOpen={isClassModalOpen}
          onClose={() => setIsClassModalOpen(false)}
          onSubmit={handleCreateClass}
        />

        <CreateRelationModal
          isOpen={isRelationModalOpen}
          clases={modelo?.clases || []}
          onClose={() => setIsRelationModalOpen(false)}
          onSubmit={handleCreateRelation}
        />

        {/* Panel de Control de Versiones, Historial y Trazabilidad (Fase 6) */}
        {selectedModeloId && (
          <VersionPanel
            isOpen={isVersioningOpen}
            onClose={() => setIsVersioningOpen(false)}
            modeloId={selectedModeloId}
            nombreModelo={modelo?.nombre || 'Diagrama'}
            versionActual={modelo?.version || '1.0'}
            onVersionRestored={(restored) => {
              restoreModeloState(restored);
            }}
          />
        )}

        {/* Asistente Inteligente de Edición UML por Texto y Voz (Fase 7) */}
        {selectedModeloId && (
          <AIChatPanel
            isOpen={isAIChatOpen}
            onClose={() => setIsAIChatOpen(false)}
            modeloId={selectedModeloId}
            nombreModelo={modelo?.nombre || 'Diagrama'}
            onModelUpdated={(actualizado) => {
              restoreModeloState(actualizado);
            }}
          />
        )}

        {/* Modal de Conversión de Imagen a UML con Visión Artificial (Fase 8) */}
        {selectedModeloId && (
          <ImageUMLModal
            isOpen={isImageUMLOpen}
            onClose={() => setIsImageUMLOpen(false)}
            modeloId={selectedModeloId}
            onModelApplied={(actualizado) => {
              restoreModeloState(actualizado);
            }}
          />
        )}

        {/* Modal de Generación Automática de Backend Spring Boot (Fase 9) */}
        {selectedModeloId && (
          <CodeGeneratorModal
            isOpen={isGeneratorOpen}
            onClose={() => setIsGeneratorOpen(false)}
            modeloId={selectedModeloId}
            nombreModelo={modelo?.nombre}
          />
        )}

        {/* Modal de Interoperabilidad con Enterprise Architect (Fase 10) */}
        {selectedModeloId && (
          <EnterpriseArchitectModal
            isOpen={isEAOpen}
            onClose={() => setIsEAOpen(false)}
            modeloId={selectedModeloId}
            onModelImported={() => {
              if (selectedModeloId) {
                loadModelo(selectedModeloId);
              }
            }}
          />
        )}
      </div>
    </ReactFlowProvider>
  );
};
