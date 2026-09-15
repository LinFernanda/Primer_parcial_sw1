import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useUMLStore } from '../store/umlStore';
import { umlService } from '../services/umlService';
import { ModeloUML } from '../models/uml.types';

vi.mock('../services/umlService', () => ({
  umlService: {
    getModeloById: vi.fn(),
    createClase: vi.fn(),
    updateClase: vi.fn(),
    deleteClase: vi.fn(),
    addAtributo: vi.fn(),
    deleteAtributo: vi.fn(),
    addMetodo: vi.fn(),
    deleteMetodo: vi.fn(),
    createRelacion: vi.fn(),
    deleteRelacion: vi.fn(),
  },
}));

describe('useUMLStore State Management', () => {
  const mockModelo: ModeloUML = {
    id: 100,
    nombre: 'Modelo Principal',
    version: '1.0',
    clases: [
      {
        id: 1,
        nombre: 'Usuario',
        visibilidad: 'PUBLIC',
        posicionX: 100,
        posicionY: 100,
        atributos: [],
        metodos: [],
      },
    ],
    relaciones: [],
  };

  beforeEach(() => {
    vi.clearAllMocks();
    useUMLStore.setState({
      modelo: mockModelo,
      nodes: [],
      edges: [],
      history: [],
      future: [],
      selectedClassId: null,
      selectedRelationId: null,
    });
  });

  it('debe seleccionar una clase correctamente', () => {
    useUMLStore.getState().selectClass(1);
    expect(useUMLStore.getState().selectedClassId).toBe(1);
    expect(useUMLStore.getState().selectedRelationId).toBeNull();
  });

  it('debe agregar una nueva clase y actualizar el estado', async () => {
    const nuevaClase = {
      id: 2,
      nombre: 'Cuenta',
      visibilidad: 'PUBLIC' as const,
      posicionX: 200,
      posicionY: 150,
      atributos: [],
      metodos: [],
    };

    vi.mocked(umlService.createClase).mockResolvedValueOnce(nuevaClase);

    await useUMLStore.getState().createClass('Cuenta', 200, 150);

    const state = useUMLStore.getState();
    expect(state.modelo?.clases).toHaveLength(2);
    expect(state.modelo?.clases[1].nombre).toBe('Cuenta');
    expect(state.selectedClassId).toBe(2);
    expect(state.history).toHaveLength(1);
  });

  it('debe soportar Undo para revertir cambios', async () => {
    const nuevaClase = {
      id: 3,
      nombre: 'Factura',
      visibilidad: 'PUBLIC' as const,
      posicionX: 300,
      posicionY: 300,
      atributos: [],
      metodos: [],
    };

    vi.mocked(umlService.createClase).mockResolvedValueOnce(nuevaClase);

    await useUMLStore.getState().createClass('Factura', 300, 300);
    expect(useUMLStore.getState().modelo?.clases).toHaveLength(2);

    // Ejecutar Undo
    useUMLStore.getState().undo();

    expect(useUMLStore.getState().modelo?.clases).toHaveLength(1);
    expect(useUMLStore.getState().future).toHaveLength(1);
  });
});
