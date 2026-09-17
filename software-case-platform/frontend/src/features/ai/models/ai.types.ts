import { ModeloUML } from '../../uml-editor/models/uml.types';

export const TipoOperacionAI = {
  CREATE_CLASS: 'CREATE_CLASS',
  UPDATE_CLASS: 'UPDATE_CLASS',
  DELETE_CLASS: 'DELETE_CLASS',
  CREATE_ATTRIBUTE: 'CREATE_ATTRIBUTE',
  UPDATE_ATTRIBUTE: 'UPDATE_ATTRIBUTE',
  DELETE_ATTRIBUTE: 'DELETE_ATTRIBUTE',
  CREATE_RELATION: 'CREATE_RELATION',
  UPDATE_RELATION: 'UPDATE_RELATION',
  DELETE_RELATION: 'DELETE_RELATION',
  CONFIRMATION_REQUIRED: 'CONFIRMATION_REQUIRED',
  UNKNOWN: 'UNKNOWN',
} as const;

export type TipoOperacionAI =
  (typeof TipoOperacionAI)[keyof typeof TipoOperacionAI];

export interface AtributoSimpleAI {
  nombre: string;
  tipo: string;
  visibilidad?: string;
}

export interface ParsedAIAction {
  tipoOperacion: TipoOperacionAI;
  nombreClase?: string;
  nuevoNombreClase?: string;
  nombreAtributo?: string;
  nuevoNombreAtributo?: string;
  tipoDatoAtributo?: string;
  visibilidad?: string;
  claseOrigen?: string;
  claseDestino?: string;
  tipoRelacion?: string;
  cardinalidadOrigen?: string;
  cardinalidadDestino?: string;
  descripcion?: string;
  explicacion?: string;
  requiereConfirmacion?: boolean;
  preguntaConfirmacion?: string;
  atributos?: AtributoSimpleAI[];
}

export interface AICommandRequest {
  prompt: string;
  confirmado?: boolean;
  accionConfirmada?: ParsedAIAction;
  confirmed?: boolean;
  confirmedAction?: ParsedAIAction;
}

export interface AICommandResponse {
  mensaje?: string;
  exitoso?: boolean;
  requiereConfirmacion?: boolean;
  preguntaConfirmacion?: string;
  accion?: ParsedAIAction;
  modeloActualizado?: ModeloUML;
  fecha?: string;
  success?: boolean;
  message?: string;
  confirmationRequired?: boolean;
  confirmationQuestion?: string;
  parsedAction?: ParsedAIAction;
  updatedModelo?: ModeloUML;
  commandHistoryId?: number;
}

export interface AICommandHistory {
  id?: number;
  tipoOperacion?: TipoOperacionAI;
  operacion?: string;
  elementoObjetivo?: string;
  parametros?: string;
  promptOriginal?: string;
  respuestaGenerada?: string;
  exitoso?: boolean;
  requiereConfirmacion?: boolean;
  requirioConfirmacion?: boolean;
  usuarioEmail?: string;
  fecha?: string;
  fechaEjecucion?: string;
  modeloId?: number;
  prompt?: string;
  ejecutadoConExito?: boolean;
}

export interface ChatMessage {
  id: string;
  remitente: 'user' | 'assistant';
  texto: string;
  fecha: string;
  requiereConfirmacion?: boolean;
  accionPendiente?: ParsedAIAction;
  accionEjecutada?: ParsedAIAction;
  esExitoso?: boolean;
}
