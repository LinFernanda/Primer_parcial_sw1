import { describe, it, expect, beforeEach } from 'vitest';
import { useUMLStore } from '../store/umlStore';
import { UMLEvent, UsuarioConectado, BloqueoElemento } from '../models/uml.types';

describe('UML Real-time Collaboration (Fase 5)', () => {
  beforeEach(() => {
    useUMLStore.setState({
      modelo: {
        id: 1,
        nombre: 'Modelo Test',
        version: '1.0',
        clases: [],
        relaciones: [],
      },
      nodes: [],
      edges: [],
      connectedUsers: [],
      lockedElements: [],
      isWsConnected: false,
    });
  });

  it('debe actualizar la presencia de usuarios conectados al recibir setConnectedUsers', () => {
    const usuarios: UsuarioConectado[] = [
      { usuario: 'juan@case.com', nombre: 'Juan', color: '#3B82F6' },
      { usuario: 'maria@case.com', nombre: 'Maria', color: '#10B981' },
    ];

    useUMLStore.getState().setConnectedUsers(usuarios);

    const storeUsers = useUMLStore.getState().connectedUsers;
    expect(storeUsers).toHaveLength(2);
    expect(storeUsers[0].nombre).toBe('Juan');
    expect(storeUsers[1].nombre).toBe('Maria');
  });

  it('debe sincronizar bloqueos de concurrencia al recibir setLockedElements', () => {
    const bloqueos: BloqueoElemento[] = [
      { elementoId: '10', usuario: 'juan@case.com', elementoTipo: 'CLASE' },
    ];

    useUMLStore.getState().setLockedElements(bloqueos);

    const storeLocks = useUMLStore.getState().lockedElements;
    expect(storeLocks).toHaveLength(1);
    expect(storeLocks[0].elementoId).toBe('10');
    expect(storeLocks[0].usuario).toBe('juan@case.com');
  });

  it('debe agregar una clase al recibir un evento remoto CREATE', () => {
    const event: UMLEvent = {
      idEvento: 'ev-1',
      usuario: 'otro.ingeniero@case.com',
      modeloUMLId: 1,
      tipoOperacion: 'CREATE',
      elementoTipo: 'CLASE',
      elementoId: '101',
      datosCambio: {
        clase: {
          id: 101,
          nombre: 'FacturaRemota',
          visibilidad: 'PUBLIC',
          posicionX: 150,
          posicionY: 200,
          atributos: [],
          metodos: [],
        },
      },
    };

    useUMLStore.getState().applyRemoteEvent(event);

    const { modelo, nodes } = useUMLStore.getState();
    expect(modelo?.clases).toHaveLength(1);
    expect(modelo?.clases[0].nombre).toBe('FacturaRemota');
    expect(nodes).toHaveLength(1);
    expect(nodes[0].id).toBe('101');
  });

  it('debe actualizar una clase y su posición al recibir un evento remoto UPDATE', () => {
    // Inicializar con una clase existente
    useUMLStore.setState({
      modelo: {
        id: 1,
        nombre: 'Modelo Test',
        version: '1.0',
        clases: [
          {
            id: 101,
            nombre: 'Factura',
            visibilidad: 'PUBLIC',
            posicionX: 100,
            posicionY: 100,
            atributos: [],
            metodos: [],
          },
        ],
        relaciones: [],
      },
      nodes: [
        {
          id: '101',
          type: 'classNode',
          position: { x: 100, y: 100 },
          data: {
            clase: {
              id: 101,
              nombre: 'Factura',
              visibilidad: 'PUBLIC',
              posicionX: 100,
              posicionY: 100,
              atributos: [],
              metodos: [],
            },
          },
        },
      ],
    });

    const event: UMLEvent = {
      idEvento: 'ev-2',
      usuario: 'otro.ingeniero@case.com',
      modeloUMLId: 1,
      tipoOperacion: 'UPDATE',
      elementoTipo: 'CLASE',
      elementoId: '101',
      datosCambio: {
        clase: {
          id: 101,
          nombre: 'FacturaActualizada',
          posicionX: 250,
          posicionY: 300,
        },
      },
    };

    useUMLStore.getState().applyRemoteEvent(event);

    const { modelo, nodes } = useUMLStore.getState();
    expect(modelo?.clases[0].nombre).toBe('FacturaActualizada');
    expect(nodes[0].position.x).toBe(250);
    expect(nodes[0].position.y).toBe(300);
  });

  it('debe eliminar una clase al recibir un evento remoto DELETE', () => {
    useUMLStore.setState({
      modelo: {
        id: 1,
        nombre: 'Modelo Test',
        version: '1.0',
        clases: [
          {
            id: 101,
            nombre: 'Factura',
            visibilidad: 'PUBLIC',
            posicionX: 100,
            posicionY: 100,
            atributos: [],
            metodos: [],
          },
        ],
        relaciones: [],
      },
      nodes: [
        {
          id: '101',
          type: 'classNode',
          position: { x: 100, y: 100 },
          data: {},
        },
      ],
    });

    const event: UMLEvent = {
      idEvento: 'ev-3',
      usuario: 'otro.ingeniero@case.com',
      modeloUMLId: 1,
      tipoOperacion: 'DELETE',
      elementoTipo: 'CLASE',
      elementoId: '101',
    };

    useUMLStore.getState().applyRemoteEvent(event);

    const { modelo, nodes } = useUMLStore.getState();
    expect(modelo?.clases).toHaveLength(0);
    expect(nodes).toHaveLength(0);
  });
});
