import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useUMLStore } from '../../uml-editor/store/umlStore';
import { ModeloUML } from '../../uml-editor/models/uml.types';
import { versionService } from '../services/versionService';
import { VersionModeloDTO, HistorialCambioDTO } from '../models/version.types';
import { apiClient } from '../../../services/api';

vi.mock('../../../services/api', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('Control de Versiones, Historial y Recuperación UML (Fase 6)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useUMLStore.setState({
      modelo: {
        id: 1,
        nombre: 'Sistema Ventas',
        version: '1.0',
        clases: [
          {
            id: 10,
            nombre: 'Cliente',
            visibilidad: 'PUBLIC',
            posicionX: 100,
            posicionY: 100,
            atributos: [],
            metodos: [],
          },
          {
            id: 20,
            nombre: 'ProductoTemporal',
            visibilidad: 'PUBLIC',
            posicionX: 300,
            posicionY: 300,
            atributos: [],
            metodos: [],
          },
        ],
        relaciones: [],
      },
      nodes: [],
      edges: [],
      history: [],
      future: [],
    });
  });

  // ---------------------------------------------------------------------------
  // 1. SERVICIO DE VERSIONADO Y HISTORIAL
  // ---------------------------------------------------------------------------

  it('versionService.crearVersion debe enviar POST a /api/versiones y devolver VersionModeloDTO', async () => {
    const mockVersion: VersionModeloDTO = {
      id: 1,
      numeroVersion: 'v1.0',
      nombreVersion: 'Versión Inicial',
      descripcion: 'Snapshot base',
      modeloId: 1,
      fechaCreacion: '2026-09-16T12:00:00',
      estado: 'ACTIVA',
      usuarioCreadorEmail: 'ingeniero@case.com',
    };

    (apiClient.post as any).mockResolvedValueOnce({ data: mockVersion });

    const resultado = await versionService.crearVersion({
      modeloId: 1,
      nombreVersion: 'Versión Inicial',
      numeroVersion: 'v1.0',
    });

    expect(apiClient.post).toHaveBeenCalledWith('/api/versiones', {
      modeloId: 1,
      nombreVersion: 'Versión Inicial',
      numeroVersion: 'v1.0',
    });
    expect(resultado.id).toBe(1);
    expect(resultado.numeroVersion).toBe('v1.0');
    expect(resultado.nombreVersion).toBe('Versión Inicial');
  });

  it('versionService.getVersionesPorModelo debe consultar GET /api/modelos/{id}/versiones', async () => {
    const mockList: VersionModeloDTO[] = [
      {
        id: 2,
        numeroVersion: 'v2.0',
        nombreVersion: 'Sprint 2',
        modeloId: 1,
        fechaCreacion: '2026-09-16T14:00:00',
        estado: 'ACTIVA',
      },
      {
        id: 1,
        numeroVersion: 'v1.0',
        nombreVersion: 'Sprint 1',
        modeloId: 1,
        fechaCreacion: '2026-09-15T10:00:00',
        estado: 'ACTIVA',
      },
    ];

    (apiClient.get as any).mockResolvedValueOnce({ data: mockList });

    const resultado = await versionService.getVersionesPorModelo(1);

    expect(apiClient.get).toHaveBeenCalledWith('/api/modelos/1/versiones');
    expect(resultado).toHaveLength(2);
    expect(resultado[0].numeroVersion).toBe('v2.0');
  });

  it('versionService.getHistorialPorModelo debe consultar GET /api/modelos/{id}/historial', async () => {
    const mockHistorial: HistorialCambioDTO[] = [
      {
        id: 101,
        tipoOperacion: 'CREATE',
        elementoModificado: 'Clase UML',
        idElemento: '10',
        usuarioEmail: 'juan@case.com',
        usuarioNombre: 'Juan',
        fechaCambio: '2026-09-16T11:00:00',
        modeloId: 1,
        descripcionResumen: 'Juan creó Clase Cliente',
      },
      {
        id: 102,
        tipoOperacion: 'DELETE',
        elementoModificado: 'Atributo',
        idElemento: '5',
        usuarioEmail: 'carlos@case.com',
        usuarioNombre: 'Carlos',
        fechaCambio: '2026-09-16T11:30:00',
        modeloId: 1,
        descripcionResumen: 'Carlos eliminó atributo precio',
      },
    ];

    (apiClient.get as any).mockResolvedValueOnce({ data: mockHistorial });

    const resultado = await versionService.getHistorialPorModelo(1);

    expect(apiClient.get).toHaveBeenCalledWith('/api/modelos/1/historial');
    expect(resultado).toHaveLength(2);
    expect(resultado[0].descripcionResumen).toBe('Juan creó Clase Cliente');
    expect(resultado[1].descripcionResumen).toBe('Carlos eliminó atributo precio');
  });

  it('versionService.restaurarVersion debe enviar POST a /api/versiones/{id}/restore', async () => {
    const modeloRestaurado: ModeloUML = {
      id: 1,
      nombre: 'Sistema Ventas',
      version: 'v1.0',
      clases: [
        {
          id: 10,
          nombre: 'Cliente',
          visibilidad: 'PUBLIC',
          posicionX: 100,
          posicionY: 100,
          atributos: [],
          metodos: [],
        },
      ],
      relaciones: [],
    };

    (apiClient.post as any).mockResolvedValueOnce({ data: modeloRestaurado });

    const resultado = await versionService.restaurarVersion(1, {
      comentario: 'Revertir a v1.0',
    });

    expect(apiClient.post).toHaveBeenCalledWith('/api/versiones/1/restore', {
      comentario: 'Revertir a v1.0',
    });
    expect(resultado.version).toBe('v1.0');
    expect(resultado.clases).toHaveLength(1);
    expect(resultado.clases[0].nombre).toBe('Cliente');
  });

  // ---------------------------------------------------------------------------
  // 2. RECUPERACIÓN Y ACTUALIZACIÓN EN EL STORE DE LA PIZARRA UML
  // ---------------------------------------------------------------------------

  it('useUMLStore.restoreModeloState debe reemplazar el modelo y regenerar los nodos y aristas de React Flow', () => {
    const modeloRecuperado: ModeloUML = {
      id: 1,
      nombre: 'Sistema Ventas',
      version: 'v1.0',
      clases: [
        {
          id: 999,
          nombre: 'ClienteRecuperado',
          visibilidad: 'PUBLIC',
          posicionX: 250,
          posicionY: 180,
          atributos: [
            {
              id: 1,
              nombre: 'codigo',
              tipoDato: 'String',
              visibilidad: 'PRIVATE',
              claseId: 999,
            },
          ],
          metodos: [],
        },
      ],
      relaciones: [],
    };

    useUMLStore.getState().restoreModeloState(modeloRecuperado);

    const { modelo, nodes, edges } = useUMLStore.getState();

    // Comprueba que el estado anterior se reemplazó fielmente
    expect(modelo?.version).toBe('v1.0');
    expect(modelo?.clases).toHaveLength(1);
    expect(modelo?.clases[0].nombre).toBe('ClienteRecuperado');
    expect(nodes).toHaveLength(1);
    expect(nodes[0].id).toBe('999');
    expect(nodes[0].position.x).toBe(250);
    expect(nodes[0].position.y).toBe(180);
    expect(edges).toHaveLength(0);
  });
});
