import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useUMLStore } from '../../uml-editor/store/umlStore';
import { ModeloUML } from '../../uml-editor/models/uml.types';
import { aiService } from '../services/aiService';
import {
  AICommandResponse,
  AICommandHistory,
  TipoOperacionAI,
} from '../models/ai.types';
import { apiClient } from '../../../services/api';

vi.mock('../../../services/api', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('Agente Inteligente de Edición UML Asistida por IA (Fase 7)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useUMLStore.setState({
      modelo: {
        id: 1,
        nombre: 'Sistema E-Commerce',
        version: '1.0',
        clases: [
          {
            id: 10,
            nombre: 'Cliente',
            visibilidad: 'PUBLIC',
            posicionX: 100,
            posicionY: 100,
            atributos: [
              {
                id: 1,
                nombre: 'nombre',
                tipoDato: 'String',
                visibilidad: 'PRIVATE',
                claseId: 10,
              },
            ],
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
  // 1. SERVICIO DE COMANDOS DE IA
  // ---------------------------------------------------------------------------

  it('aiService.enviarComando debe enviar POST a /api/modelos/{id}/ai/command y procesar acción exitosa', async () => {
    const mockUpdatedModelo: ModeloUML = {
      id: 1,
      nombre: 'Sistema E-Commerce',
      version: '1.0',
      clases: [
        {
          id: 10,
          nombre: 'Cliente',
          visibilidad: 'PUBLIC',
          posicionX: 100,
          posicionY: 100,
          atributos: [
            {
              id: 1,
              nombre: 'nombre',
              tipoDato: 'String',
              visibilidad: 'PRIVATE',
              claseId: 10,
            },
          ],
          metodos: [],
        },
        {
          id: 20,
          nombre: 'Factura',
          visibilidad: 'PUBLIC',
          posicionX: 350,
          posicionY: 150,
          atributos: [],
          metodos: [],
        },
      ],
      relaciones: [],
    };

    const mockResponse: AICommandResponse = {
      success: true,
      message: 'Clase "Factura" creada exitosamente.',
      confirmationRequired: false,
      parsedAction: {
        tipoOperacion: TipoOperacionAI.CREATE_CLASS,
        nombreClase: 'Factura',
      },
      updatedModelo: mockUpdatedModelo,
      commandHistoryId: 101,
    };

    (apiClient.post as any).mockResolvedValueOnce({ data: mockResponse });

    const result = await aiService.enviarComando(1, {
      prompt: 'Crear clase Factura',
    });

    expect(apiClient.post).toHaveBeenCalledWith('/api/modelos/1/ai/command', {
      prompt: 'Crear clase Factura',
    });
    expect(result.success).toBe(true);
    expect(result.confirmationRequired).toBe(false);
    expect(result.updatedModelo?.clases).toHaveLength(2);
  });

  it('aiService.enviarComando debe manejar solicitudes ambiguas con confirmación requerida', async () => {
    const mockResponse: AICommandResponse = {
      success: false,
      message: '¿Desea crear una clase llamada "Pedido"?',
      confirmationRequired: true,
      confirmationQuestion: '¿Desea crear una clase llamada "Pedido"?',
      parsedAction: {
        tipoOperacion: TipoOperacionAI.CREATE_CLASS,
        nombreClase: 'Pedido',
      },
    };

    (apiClient.post as any).mockResolvedValueOnce({ data: mockResponse });

    const result = await aiService.enviarComando(1, {
      prompt: 'Crear Pedido',
    });

    expect(result.confirmationRequired).toBe(true);
    expect(result.confirmationQuestion).toBe('¿Desea crear una clase llamada "Pedido"?');
  });

  it('aiService.enviarComando con confirmación explícita (confirmed: true) debe ejecutar la operación', async () => {
    const mockResponse: AICommandResponse = {
      success: true,
      message: 'Clase "Pedido" creada exitosamente.',
      confirmationRequired: false,
      parsedAction: {
        tipoOperacion: TipoOperacionAI.CREATE_CLASS,
        nombreClase: 'Pedido',
      },
    };

    (apiClient.post as any).mockResolvedValueOnce({ data: mockResponse });

    const result = await aiService.enviarComando(1, {
      prompt: 'Crear Pedido',
      confirmed: true,
      confirmedAction: {
        tipoOperacion: TipoOperacionAI.CREATE_CLASS,
        nombreClase: 'Pedido',
      },
    });

    expect(apiClient.post).toHaveBeenCalledWith('/api/modelos/1/ai/command', {
      prompt: 'Crear Pedido',
      confirmed: true,
      confirmedAction: {
        tipoOperacion: TipoOperacionAI.CREATE_CLASS,
        nombreClase: 'Pedido',
      },
    });
    expect(result.success).toBe(true);
  });

  // ---------------------------------------------------------------------------
  // 2. COMANDOS POR VOZ
  // ---------------------------------------------------------------------------

  it('aiService.enviarVoz debe enviar POST a /api/modelos/{id}/ai/voice', async () => {
    const mockResponse: AICommandResponse = {
      success: true,
      message: 'Atributo "email" agregado a la clase "Cliente".',
      confirmationRequired: false,
      parsedAction: {
        tipoOperacion: TipoOperacionAI.CREATE_ATTRIBUTE,
        nombreClase: 'Cliente',
        nombreAtributo: 'email',
        tipoDatoAtributo: 'String',
      },
    };

    (apiClient.post as any).mockResolvedValueOnce({ data: mockResponse });

    const result = await aiService.enviarVoz(
      1,
      'Agregar atributo email de tipo String a la clase Cliente'
    );

    expect(apiClient.post).toHaveBeenCalledWith('/api/modelos/1/ai/voice', {
      textoTranscrito: 'Agregar atributo email de tipo String a la clase Cliente',
      audioBase64: undefined,
    });
    expect(result.success).toBe(true);
    expect(result.parsedAction?.nombreAtributo).toBe('email');
  });

  // ---------------------------------------------------------------------------
  // 3. HISTORIAL DE COMANDOS
  // ---------------------------------------------------------------------------

  it('aiService.obtenerHistorial debe consultar GET /api/modelos/{id}/ai/history', async () => {
    const mockHistory: AICommandHistory[] = [
      {
        id: 1,
        prompt: 'Crear clase Factura',
        operacion: TipoOperacionAI.CREATE_CLASS,
        parametros: '{"targetClass":"Factura"}',
        respuestaGenerada: 'Clase "Factura" creada.',
        ejecutadoConExito: true,
        requirioConfirmacion: false,
        usuarioEmail: 'developer@case.com',
        fechaEjecucion: '2026-09-16T15:00:00',
        modeloId: 1,
      },
    ];

    (apiClient.get as any).mockResolvedValueOnce({ data: mockHistory });

    const history = await aiService.obtenerHistorial(1);

    expect(apiClient.get).toHaveBeenCalledWith('/api/modelos/1/ai/history');
    expect(history).toHaveLength(1);
    expect(history[0].prompt).toBe('Crear clase Factura');
    expect(history[0].ejecutadoConExito).toBe(true);
  });

  // ---------------------------------------------------------------------------
  // 4. ACTUALIZACIÓN INMEDIATA DEL CANVAS UML EN EL STORE
  // ---------------------------------------------------------------------------

  it('useUMLStore.restoreModeloState actualiza el lienzo tras una acción del agente IA', () => {
    const modeloActualizadoPorAI: ModeloUML = {
      id: 1,
      nombre: 'Sistema E-Commerce',
      version: '1.0',
      clases: [
        {
          id: 10,
          nombre: 'Cliente',
          visibilidad: 'PUBLIC',
          posicionX: 100,
          posicionY: 100,
          atributos: [
            {
              id: 1,
              nombre: 'nombre',
              tipoDato: 'String',
              visibilidad: 'PRIVATE',
              claseId: 10,
            },
            {
              id: 2,
              nombre: 'email',
              tipoDato: 'String',
              visibilidad: 'PRIVATE',
              claseId: 10,
            },
          ],
          metodos: [],
        },
        {
          id: 20,
          nombre: 'Factura',
          visibilidad: 'PUBLIC',
          posicionX: 400,
          posicionY: 120,
          atributos: [],
          metodos: [],
        },
      ],
      relaciones: [
        {
          id: 50,
          nombre: 'genera',
          tipo: 'ASOCIACION',
          claseOrigenId: 10,
          claseDestinoId: 20,
          multiplicidadOrigen: '1',
          multiplicidadDestino: '0..*',
        },
      ],
    };

    useUMLStore.getState().restoreModeloState(modeloActualizadoPorAI);

    const { modelo, nodes, edges } = useUMLStore.getState();

    expect(modelo?.clases).toHaveLength(2);
    expect(modelo?.clases[0].atributos).toHaveLength(2);
    expect(modelo?.clases[0].atributos[1].nombre).toBe('email');
    expect(nodes).toHaveLength(2);
    expect(nodes[0].data.clase.nombre).toBe('Cliente');
    expect(nodes[1].data.clase.nombre).toBe('Factura');
    expect(edges).toHaveLength(1);
    expect(edges[0].source).toBe('10');
    expect(edges[0].target).toBe('20');
  });

  // ---------------------------------------------------------------------------
  // 5. CONTROL Y RESTRICCIÓN DE ALCANCE (NO GENERACIÓN DE SISTEMAS COMPLETOS)
  // ---------------------------------------------------------------------------

  it('No debe permitir la creación de sistemas completos desde cero (debe rechazar generación integral)', async () => {
    const mockRejectionResponse: AICommandResponse = {
      success: false,
      message:
        'El agente IA está diseñado exclusivamente para asistir en la edición y manipulación inteligente del modelo UML existente. No genera sistemas completos desde cero.',
      confirmationRequired: false,
      parsedAction: {
        tipoOperacion: TipoOperacionAI.UNKNOWN,
        descripcion: 'blocked: system_generation_restriction',
      },
    };

    (apiClient.post as any).mockResolvedValueOnce({ data: mockRejectionResponse });

    const result = await aiService.enviarComando(1, {
      prompt: 'Generar sistema completo de facturación con todas sus clases y base de datos',
    });

    expect(result.success).toBe(false);
    expect(result.message).toContain('No genera sistemas completos desde cero');
  });
});
