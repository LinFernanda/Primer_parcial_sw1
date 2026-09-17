/**
 * Modelos de datos para la interoperabilidad con Enterprise Architect (Fase 10).
 */

export interface UMLImportAttribute {
  id?: string;
  nombre: string;
  tipoDato?: string;
  visibilidad?: 'PUBLIC' | 'PRIVATE' | 'PROTECTED' | 'PACKAGE';
  valorInicial?: string;
}

export interface UMLImportMethod {
  id?: string;
  nombre: string;
  tipoRetorno?: string;
  visibilidad?: 'PUBLIC' | 'PRIVATE' | 'PROTECTED' | 'PACKAGE';
  parametros?: string;
}

export interface UMLImportClass {
  xmiId?: string;
  nombre: string;
  visibilidad?: 'PUBLIC' | 'PRIVATE' | 'PROTECTED' | 'PACKAGE';
  descripcion?: string;
  isAbstract?: boolean;
  posicionX?: number;
  posicionY?: number;
  atributos?: UMLImportAttribute[];
  metodos?: UMLImportMethod[];
}

export interface UMLImportRelation {
  xmiId?: string;
  nombre?: string;
  tipoRelacion?: 'ASOCIACION' | 'HERENCIA' | 'AGREGACION' | 'COMPOSICION' | 'DEPENDENCIA';
  claseOrigenId?: string;
  claseOrigenNombre?: string;
  claseDestinoId?: string;
  claseDestinoNombre?: string;
  cardinalidadOrigen?: string;
  cardinalidadDestino?: string;
  descripcion?: string;
}

export interface UMLImportModel {
  nombre?: string;
  xmiVersion?: string;
  exporter?: string;
  exporterVersion?: string;
  clases: UMLImportClass[];
  relaciones: UMLImportRelation[];
  advertencias?: string[];
}

export interface UMLImportValidationResult {
  valido: boolean;
  errores: string[];
  advertencias: string[];
  totalClases: number;
  totalAtributos: number;
  totalMetodos: number;
  totalRelaciones: number;
  nombreModelo?: string;
  xmiVersion?: string;
  exportador?: string;
}

export interface XMIValidationResponseDTO {
  valido: boolean;
  mensaje: string;
  validacion: UMLImportValidationResult;
  preview: UMLImportModel;
}

export interface XMIImportRequestDTO {
  modeloId?: number;
  proyectoId?: number;
  nombreModelo?: string;
  limpiarExistente?: boolean;
  preservarPosiciones?: boolean;
}

export interface XMIImportResponseDTO {
  exito: boolean;
  mensaje: string;
  modeloId: number;
  proyectoId?: number;
  nombreModelo: string;
  versionId?: number;
  versionNumero?: string;
  clasesImportadas: number;
  atributosImportados: number;
  metodosImportados: number;
  relacionesImportadas: number;
  archivoOrigen?: string;
  fecha: string;
}

export interface XMIExportResponseDTO {
  nombreArchivo: string;
  versionXMI: string;
  totalClases: number;
  totalRelaciones: number;
  tamanoBytes: number;
  contenidoXML: string;
}
