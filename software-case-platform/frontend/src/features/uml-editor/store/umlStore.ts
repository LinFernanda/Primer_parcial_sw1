import { create } from 'zustand';
import {
  Connection,
  Edge,
  EdgeChange,
  Node,
  NodeChange,
  OnConnect,
  OnEdgesChange,
  OnNodesChange,
  applyEdgeChanges,
  applyNodeChanges,
} from 'reactflow';
import {
  AtributoUML,
  BloqueoElemento,
  ClaseUML,
  MetodoUML,
  ModeloUML,
  RelacionUML,
  UMLEvent,
  UsuarioConectado,
  VisibilidadUML,
} from '../models/uml.types';
import { umlService } from '../services/umlService';
import { websocketService } from '../services/websocketService';

interface HistorySnapshot {
  nodes: Node[];
  edges: Edge[];
  modelo: ModeloUML | null;
}

interface UMLStoreState {
  modelo: ModeloUML | null;
  selectedClassId: number | null;
  selectedRelationId: number | null;
  nodes: Node[];
  edges: Edge[];
  history: HistorySnapshot[];
  future: HistorySnapshot[];
  isLoading: boolean;
  isSaving: boolean;
  error: string | null;

  // Estado colaborativo en tiempo real
  connectedUsers: UsuarioConectado[];
  lockedElements: BloqueoElemento[];
  isWsConnected: boolean;

  // Acciones colaborativas
  setWsConnected: (connected: boolean) => void;
  setConnectedUsers: (usuarios: UsuarioConectado[]) => void;
  setLockedElements: (bloqueos: BloqueoElemento[]) => void;
  applyRemoteEvent: (event: UMLEvent) => void;
  broadcastNodePosition: (claseId: number, x: number, y: number) => void;

  // Acciones
  loadModelo: (modeloId: number) => Promise<void>;
  onNodesChange: OnNodesChange;
  onEdgesChange: OnEdgesChange;
  onConnect: OnConnect;
  selectClass: (id: number | null) => void;
  selectRelation: (id: number | null) => void;
  createClass: (
    nombre: string,
    x?: number,
    y?: number,
    visibilidad?: VisibilidadUML,
    descripcion?: string
  ) => Promise<void>;
  updateClass: (id: number, data: Partial<ClaseUML>) => Promise<void>;
  deleteClass: (id: number) => Promise<void>;
  addAttribute: (claseId: number, attr: Partial<AtributoUML>) => Promise<void>;
  deleteAttribute: (attrId: number) => Promise<void>;
  addMethod: (claseId: number, metodo: Partial<MetodoUML>) => Promise<void>;
  deleteMethod: (metodoId: number) => Promise<void>;
  createRelation: (data: Partial<RelacionUML>) => Promise<void>;
  deleteRelation: (id: number) => Promise<void>;
  deleteSelected: () => Promise<void>;
  savePositionChanges: () => Promise<void>;
  undo: () => void;
  redo: () => void;
  clearError: () => void;
}

export const useUMLStore = create<UMLStoreState>((set, get) => ({
  modelo: null,
  selectedClassId: null,
  selectedRelationId: null,
  nodes: [],
  edges: [],
  history: [],
  future: [],
  isLoading: false,
  isSaving: false,
  error: null,

  // Estado colaborativo
  connectedUsers: [],
  lockedElements: [],
  isWsConnected: false,

  setWsConnected: (connected: boolean) => set({ isWsConnected: connected }),
  setConnectedUsers: (usuarios: UsuarioConectado[]) => set({ connectedUsers: usuarios }),
  setLockedElements: (bloqueos: BloqueoElemento[]) => set({ lockedElements: bloqueos }),

  broadcastNodePosition: (claseId: number, x: number, y: number) => {
    const { modelo } = get();
    if (!modelo) return;
    const userEmail = localStorage.getItem('userEmail') || 'ingeniero@caseplatform.com';
    websocketService.sendEvent(modelo.id, {
      usuario: userEmail,
      tipoOperacion: 'UPDATE',
      elementoTipo: 'CLASE',
      elementoId: claseId.toString(),
      datosCambio: {
        clase: { id: claseId, posicionX: x, posicionY: y },
      },
    });
  },

  applyRemoteEvent: (event: UMLEvent) => {
    const { modelo } = get();
    if (!modelo || modelo.id !== event.modeloUMLId) return;

    const currentUser = localStorage.getItem('userEmail');
    if (event.usuario === currentUser) return; // Evitar duplicar modificaciones ya reflejadas localmente

    if (event.tipoOperacion === 'CREATE' && event.elementoTipo === 'CLASE' && event.datosCambio?.clase) {
      const incomingClase: ClaseUML = event.datosCambio.clase;
      if (!modelo.clases.some((c) => c.id === incomingClase.id)) {
        const updatedClases = [...modelo.clases, incomingClase];
        const updatedModelo = { ...modelo, clases: updatedClases };
        const graph = mapModeloToGraph(updatedModelo);
        set({ modelo: updatedModelo, nodes: graph.nodes, edges: graph.edges });
      }
    } else if (event.tipoOperacion === 'UPDATE' && event.elementoTipo === 'CLASE' && event.datosCambio?.clase) {
      const updatedClase: Partial<ClaseUML> = event.datosCambio.clase;
      const updatedClases = modelo.clases.map((c) =>
        c.id === updatedClase.id ? { ...c, ...updatedClase } : c
      );
      const updatedModelo = { ...modelo, clases: updatedClases };
      const graph = mapModeloToGraph(updatedModelo);
      set({ modelo: updatedModelo, nodes: graph.nodes, edges: graph.edges });
    } else if (event.tipoOperacion === 'DELETE' && event.elementoTipo === 'CLASE' && event.elementoId) {
      const targetId = parseInt(event.elementoId, 10);
      const updatedClases = modelo.clases.filter((c) => c.id !== targetId);
      const updatedRelaciones = modelo.relaciones.filter(
        (r) => r.claseOrigenId !== targetId && r.claseDestinoId !== targetId
      );
      const updatedModelo = { ...modelo, clases: updatedClases, relaciones: updatedRelaciones };
      const graph = mapModeloToGraph(updatedModelo);
      set({ modelo: updatedModelo, nodes: graph.nodes, edges: graph.edges });
    } else if (event.tipoOperacion === 'CREATE' && event.elementoTipo === 'RELACION' && event.datosCambio?.relacion) {
      const incomingRelacion: RelacionUML = event.datosCambio.relacion;
      if (!modelo.relaciones.some((r) => r.id === incomingRelacion.id)) {
        const updatedRelaciones = [...modelo.relaciones, incomingRelacion];
        const updatedModelo = { ...modelo, relaciones: updatedRelaciones };
        const graph = mapModeloToGraph(updatedModelo);
        set({ modelo: updatedModelo, nodes: graph.nodes, edges: graph.edges });
      }
    } else if (event.tipoOperacion === 'DELETE' && event.elementoTipo === 'RELACION' && event.elementoId) {
      const targetId = parseInt(event.elementoId, 10);
      const updatedRelaciones = modelo.relaciones.filter((r) => r.id !== targetId);
      const updatedModelo = { ...modelo, relaciones: updatedRelaciones };
      const graph = mapModeloToGraph(updatedModelo);
      set({ modelo: updatedModelo, nodes: graph.nodes, edges: graph.edges });
    } else if (event.tipoOperacion === 'LOCK' && event.elementoId && event.usuario) {
      const newLock: BloqueoElemento = {
        elementoId: event.elementoId,
        usuario: event.usuario,
        elementoTipo: (event.elementoTipo as string) || 'CLASE',
        fechaBloqueo: event.fecha,
      };
      set((state) => ({
        lockedElements: [
          ...state.lockedElements.filter((l) => l.elementoId !== event.elementoId),
          newLock,
        ],
      }));
    } else if (event.tipoOperacion === 'UNLOCK' && event.elementoId) {
      set((state) => ({
        lockedElements: state.lockedElements.filter((l) => l.elementoId !== event.elementoId),
      }));
    }
  },

  loadModelo: async (modeloId: number) => {
    set({ isLoading: true, error: null });
    try {
      const modelo = await umlService.getModeloById(modeloId);
      const { nodes, edges } = mapModeloToGraph(modelo);
      set({
        modelo,
        nodes,
        edges,
        selectedClassId: null,
        selectedRelationId: null,
        history: [],
        future: [],
        isLoading: false,
      });
    } catch (err: any) {
      set({
        error: err.response?.data?.message || 'Error al cargar el modelo UML',
        isLoading: false,
      });
    }
  },

  onNodesChange: (changes: NodeChange[]) => {
    set((state) => ({
      nodes: applyNodeChanges(changes, state.nodes),
    }));
  },

  onEdgesChange: (changes: EdgeChange[]) => {
    set((state) => ({
      edges: applyEdgeChanges(changes, state.edges),
    }));
  },

  onConnect: async (connection: Connection) => {
    const { modelo, createRelation } = get();
    if (!modelo || !connection.source || !connection.target) return;

    const sourceId = parseInt(connection.source, 10);
    const targetId = parseInt(connection.target, 10);

    await createRelation({
      tipoRelacion: 'ASOCIACION',
      claseOrigenId: sourceId,
      claseDestinoId: targetId,
      cardinalidadOrigen: '1',
      cardinalidadDestino: '1',
      modeloId: modelo.id,
    });
  },

  selectClass: (id: number | null) => {
    const { modelo, selectedClassId } = get();
    if (modelo) {
      if (selectedClassId && selectedClassId !== id) {
        websocketService.unlockElement(modelo.id, selectedClassId.toString());
      }
      if (id !== null) {
        websocketService.lockElement(modelo.id, id.toString(), 'CLASE');
      }
    }
    set({
      selectedClassId: id,
      selectedRelationId: null,
    });
  },

  selectRelation: (id: number | null) => {
    const { modelo, selectedClassId } = get();
    if (modelo && selectedClassId) {
      websocketService.unlockElement(modelo.id, selectedClassId.toString());
    }
    set({
      selectedRelationId: id,
      selectedClassId: null,
    });
  },

  createClass: async (
    nombre: string,
    x = 100,
    y = 100,
    visibilidad: VisibilidadUML = 'PUBLIC',
    descripcion?: string
  ) => {
    const { modelo, nodes, edges, history } = get();
    if (!modelo) return;

    set({ isSaving: true, error: null });
    try {
      // Guardar snapshot para Undo
      const newHistory = [...history, { nodes, edges, modelo }];

      const nuevaClase = await umlService.createClase(modelo.id, {
        nombre: nombre.trim(),
        posicionX: x,
        posicionY: y,
        visibilidad,
        descripcion,
      });

      const updatedClases = [...modelo.clases, nuevaClase];
      const updatedModelo = { ...modelo, clases: updatedClases };
      const graph = mapModeloToGraph(updatedModelo);

      // Difundir evento de creación colaborativa en tiempo real
      websocketService.sendEvent(modelo.id, {
        tipoOperacion: 'CREATE',
        elementoTipo: 'CLASE',
        elementoId: nuevaClase.id!.toString(),
        datosCambio: { clase: nuevaClase },
      });

      set({
        modelo: updatedModelo,
        nodes: graph.nodes,
        edges: graph.edges,
        selectedClassId: nuevaClase.id,
        history: newHistory,
        future: [],
        isSaving: false,
      });
    } catch (err: any) {
      set({
        error: err.response?.data?.message || 'Error al crear la clase UML',
        isSaving: false,
      });
    }
  },

  updateClass: async (id: number, data: Partial<ClaseUML>) => {
    const { modelo } = get();
    if (!modelo) return;

    set({ isSaving: true, error: null });
    try {
      const claseActualizada = await umlService.updateClase(id, data);
      const updatedClases = modelo.clases.map((c) =>
        c.id === id ? { ...c, ...claseActualizada } : c
      );
      const updatedModelo = { ...modelo, clases: updatedClases };
      const graph = mapModeloToGraph(updatedModelo);

      // Difundir actualización en tiempo real
      websocketService.sendEvent(modelo.id, {
        tipoOperacion: 'UPDATE',
        elementoTipo: 'CLASE',
        elementoId: id.toString(),
        datosCambio: { clase: claseActualizada },
      });

      set({
        modelo: updatedModelo,
        nodes: graph.nodes,
        edges: graph.edges,
        isSaving: false,
      });
    } catch (err: any) {
      set({
        error: err.response?.data?.message || 'Error al actualizar la clase UML',
        isSaving: false,
      });
    }
  },

  deleteClass: async (id: number) => {
    const { modelo, nodes, edges, history } = get();
    if (!modelo) return;

    set({ isSaving: true, error: null });
    try {
      const newHistory = [...history, { nodes, edges, modelo }];
      await umlService.deleteClase(id);

      const updatedClases = modelo.clases.filter((c) => c.id !== id);
      const updatedRelaciones = modelo.relaciones.filter(
        (r) => r.claseOrigenId !== id && r.claseDestinoId !== id
      );
      const updatedModelo = {
        ...modelo,
        clases: updatedClases,
        relaciones: updatedRelaciones,
      };
      const graph = mapModeloToGraph(updatedModelo);

      // Difundir eliminación en tiempo real
      websocketService.sendEvent(modelo.id, {
        tipoOperacion: 'DELETE',
        elementoTipo: 'CLASE',
        elementoId: id.toString(),
      });

      set({
        modelo: updatedModelo,
        nodes: graph.nodes,
        edges: graph.edges,
        selectedClassId: null,
        history: newHistory,
        future: [],
        isSaving: false,
      });
    } catch (err: any) {
      set({
        error: err.response?.data?.message || 'Error al eliminar la clase UML',
        isSaving: false,
      });
    }
  },

  addAttribute: async (claseId: number, attr: Partial<AtributoUML>) => {
    const { modelo } = get();
    if (!modelo) return;

    set({ isSaving: true, error: null });
    try {
      const nuevoAtributo = await umlService.addAtributo(claseId, attr);
      const updatedClases = modelo.clases.map((c) => {
        if (c.id === claseId) {
          return {
            ...c,
            atributos: [...c.atributos, nuevoAtributo],
          };
        }
        return c;
      });

      const updatedModelo = { ...modelo, clases: updatedClases };
      const graph = mapModeloToGraph(updatedModelo);

      set({
        modelo: updatedModelo,
        nodes: graph.nodes,
        edges: graph.edges,
        isSaving: false,
      });
    } catch (err: any) {
      set({
        error: err.response?.data?.message || 'Error al agregar atributo',
        isSaving: false,
      });
    }
  },

  deleteAttribute: async (attrId: number) => {
    const { modelo } = get();
    if (!modelo) return;

    set({ isSaving: true, error: null });
    try {
      await umlService.deleteAtributo(attrId);
      const updatedClases = modelo.clases.map((c) => ({
        ...c,
        atributos: c.atributos.filter((a) => a.id !== attrId),
      }));

      const updatedModelo = { ...modelo, clases: updatedClases };
      const graph = mapModeloToGraph(updatedModelo);

      set({
        modelo: updatedModelo,
        nodes: graph.nodes,
        edges: graph.edges,
        isSaving: false,
      });
    } catch (err: any) {
      set({
        error: err.response?.data?.message || 'Error al eliminar atributo',
        isSaving: false,
      });
    }
  },

  addMethod: async (claseId: number, metodo: Partial<MetodoUML>) => {
    const { modelo } = get();
    if (!modelo) return;

    set({ isSaving: true, error: null });
    try {
      const nuevoMetodo = await umlService.addMetodo(claseId, metodo);
      const updatedClases = modelo.clases.map((c) => {
        if (c.id === claseId) {
          return {
            ...c,
            metodos: [...c.metodos, nuevoMetodo],
          };
        }
        return c;
      });

      const updatedModelo = { ...modelo, clases: updatedClases };
      const graph = mapModeloToGraph(updatedModelo);

      set({
        modelo: updatedModelo,
        nodes: graph.nodes,
        edges: graph.edges,
        isSaving: false,
      });
    } catch (err: any) {
      set({
        error: err.response?.data?.message || 'Error al agregar método',
        isSaving: false,
      });
    }
  },

  deleteMethod: async (metodoId: number) => {
    const { modelo } = get();
    if (!modelo) return;

    set({ isSaving: true, error: null });
    try {
      await umlService.deleteMetodo(metodoId);
      const updatedClases = modelo.clases.map((c) => ({
        ...c,
        metodos: c.metodos.filter((m) => m.id !== metodoId),
      }));

      const updatedModelo = { ...modelo, clases: updatedClases };
      const graph = mapModeloToGraph(updatedModelo);

      set({
        modelo: updatedModelo,
        nodes: graph.nodes,
        edges: graph.edges,
        isSaving: false,
      });
    } catch (err: any) {
      set({
        error: err.response?.data?.message || 'Error al eliminar método',
        isSaving: false,
      });
    }
  },

  createRelation: async (data: Partial<RelacionUML>) => {
    const { modelo, nodes, edges, history } = get();
    if (!modelo) return;

    set({ isSaving: true, error: null });
    try {
      const newHistory = [...history, { nodes, edges, modelo }];
      const nuevaRelacion = await umlService.createRelacion({
        ...data,
        modeloId: modelo.id,
      });

      const updatedRelaciones = [...modelo.relaciones, nuevaRelacion];
      const updatedModelo = { ...modelo, relaciones: updatedRelaciones };
      const graph = mapModeloToGraph(updatedModelo);

      // Difundir creación de relación en tiempo real
      websocketService.sendEvent(modelo.id, {
        tipoOperacion: 'CREATE',
        elementoTipo: 'RELACION',
        elementoId: nuevaRelacion.id!.toString(),
        datosCambio: { relacion: nuevaRelacion },
      });

      set({
        modelo: updatedModelo,
        nodes: graph.nodes,
        edges: graph.edges,
        selectedRelationId: nuevaRelacion.id,
        history: newHistory,
        future: [],
        isSaving: false,
      });
    } catch (err: any) {
      set({
        error: err.response?.data?.message || 'Error al crear relación UML',
        isSaving: false,
      });
    }
  },

  deleteRelation: async (id: number) => {
    const { modelo, nodes, edges, history } = get();
    if (!modelo) return;

    set({ isSaving: true, error: null });
    try {
      const newHistory = [...history, { nodes, edges, modelo }];
      await umlService.deleteRelacion(id);

      const updatedRelaciones = modelo.relaciones.filter((r) => r.id !== id);
      const updatedModelo = { ...modelo, relaciones: updatedRelaciones };
      const graph = mapModeloToGraph(updatedModelo);

      // Difundir eliminación de relación en tiempo real
      websocketService.sendEvent(modelo.id, {
        tipoOperacion: 'DELETE',
        elementoTipo: 'RELACION',
        elementoId: id.toString(),
      });

      set({
        modelo: updatedModelo,
        nodes: graph.nodes,
        edges: graph.edges,
        selectedRelationId: null,
        history: newHistory,
        future: [],
        isSaving: false,
      });
    } catch (err: any) {
      set({
        error: err.response?.data?.message || 'Error al eliminar relación',
        isSaving: false,
      });
    }
  },

  deleteSelected: async () => {
    const { selectedClassId, selectedRelationId, deleteClass, deleteRelation } = get();
    if (selectedClassId) {
      await deleteClass(selectedClassId);
    } else if (selectedRelationId) {
      await deleteRelation(selectedRelationId);
    }
  },

  savePositionChanges: async () => {
    const { modelo, nodes } = get();
    if (!modelo) return;

    // Actualiza coordenadas persistentes de cada clase modificada
    set({ isSaving: true });
    try {
      for (const node of nodes) {
        const claseId = parseInt(node.id, 10);
        const clase = modelo.clases.find((c) => c.id === claseId);
        if (clase && (clase.posicionX !== node.position.x || clase.posicionY !== node.position.y)) {
          await umlService.updateClase(claseId, {
            posicionX: node.position.x,
            posicionY: node.position.y,
          });
        }
      }
      set({ isSaving: false });
    } catch (err) {
      set({ isSaving: false });
    }
  },

  undo: () => {
    const { history, future, nodes, edges, modelo } = get();
    if (history.length === 0) return;

    const previous = history[history.length - 1];
    const newHistory = history.slice(0, history.length - 1);
    const newFuture = [{ nodes, edges, modelo }, ...future];

    set({
      nodes: previous.nodes,
      edges: previous.edges,
      modelo: previous.modelo,
      history: newHistory,
      future: newFuture,
      selectedClassId: null,
      selectedRelationId: null,
    });
  },

  redo: () => {
    const { history, future, nodes, edges, modelo } = get();
    if (future.length === 0) return;

    const next = future[0];
    const newFuture = future.slice(1);
    const newHistory = [...history, { nodes, edges, modelo }];

    set({
      nodes: next.nodes,
      edges: next.edges,
      modelo: next.modelo,
      history: newHistory,
      future: newFuture,
      selectedClassId: null,
      selectedRelationId: null,
    });
  },

  clearError: () => set({ error: null }),
}));

/**
 * Función auxiliar para transformar el modelo conceptual UML en nodos y aristas de React Flow
 */
function mapModeloToGraph(modelo: ModeloUML): { nodes: Node[]; edges: Edge[] } {
  const nodes: Node[] = (modelo.clases || []).map((clase) => ({
    id: clase.id!.toString(),
    type: 'classNode',
    position: {
      x: clase.posicionX ?? 100,
      y: clase.posicionY ?? 100,
    },
    data: {
      clase,
    },
  }));

  const edges: Edge[] = (modelo.relaciones || []).map((rel) => {
    const label = `${rel.cardinalidadOrigen || ''} ... ${rel.cardinalidadDestino || ''}`;
    const isInheritance = rel.tipoRelacion === 'HERENCIA';
    const isDependency = rel.tipoRelacion === 'DEPENDENCIA';

    return {
      id: `rel-${rel.id}`,
      source: rel.claseOrigenId.toString(),
      target: rel.claseDestinoId.toString(),
      label: rel.descripcion ? `${rel.tipoRelacion}: ${rel.descripcion}` : `${rel.tipoRelacion} (${label})`,
      style: {
        stroke: isInheritance ? '#3b82f6' : isDependency ? '#9ca3af' : '#10b981',
        strokeWidth: 2,
        strokeDasharray: isDependency ? '5 5' : undefined,
      },
      animated: isDependency,
      data: {
        relacion: rel,
      },
    };
  });

  return { nodes, edges };
}
