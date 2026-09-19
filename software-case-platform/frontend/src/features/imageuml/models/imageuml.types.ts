import { TipoRelacionUML, VisibilidadUML, ModeloUML } from '../../uml-editor/models/uml.types';

export type EstadoProcesamientoImagen =
  | 'CARGADA'
  | 'PROCESANDO'
  | 'PROCESADA'
  | 'APLICADA'
  | 'ERROR';

export interface AtributoDetectadoDTO {
  nombre: string;
  tipoDato: string;
  visibilidad: VisibilidadUML;
  valorInicial?: string;
}

export interface MetodoDetectadoDTO {
  nombre: string;
  tipoRetorno: string;
  visibilidad: VisibilidadUML;
  parametros?: string;
}

export interface ClaseDetectadaDTO {
  nombre: string;
  visibilidad: VisibilidadUML;
  descripcion?: string;
  posicionX: number;
  posicionY: number;
  atributos: AtributoDetectadoDTO[];
  metodos: MetodoDetectadoDTO[];
}

export interface RelacionDetectadaDTO {
  claseOrigen: string;
  claseDestino: string;
  tipoRelacion: TipoRelacionUML;
  cardinalidadOrigen: string;
  cardinalidadDestino: string;
  descripcion?: string;
}

export interface ImageUMLDetectedDTO {
  clases: ClaseDetectadaDTO[];
  relaciones: RelacionDetectadaDTO[];
  nivelConfianza: number;
  advertencias: string[];
  motorUtilizado?: string;
  tiempoProcesamientoMs?: number;
}

export interface ImageUploadResponseDTO {
  idImagen: number;
  nombreArchivo: string;
  tamanioBytes: number;
  ancho: number;
  alto: number;
  estadoProcesamiento: EstadoProcesamientoImagen;
  mensaje: string;
  fechaCarga: string;
  resultadoUML: ImageUMLDetectedDTO;
}

export interface ApplyImageUMLRequestDTO {
  idImagen?: number;
  modeloAjustado: ImageUMLDetectedDTO;
  limpiarModeloExistente: boolean;
  comentario?: string;
}

export type { ModeloUML, VisibilidadUML, TipoRelacionUML };
