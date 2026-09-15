export type VisibilidadUML = 'PUBLIC' | 'PRIVATE' | 'PROTECTED' | 'PACKAGE';

export const VISIBILIDAD_SIMBOLOS: Record<VisibilidadUML, string> = {
  PUBLIC: '+',
  PRIVATE: '-',
  PROTECTED: '#',
  PACKAGE: '~',
};

export type TipoRelacionUML = 'ASOCIACION' | 'HERENCIA' | 'DEPENDENCIA' | 'AGREGACION' | 'COMPOSICION';

export type CardinalidadUML = '1' | '0..1' | '*' | '1..*' | '0..*';

export const CARDINALIDADES_DISPONIBLES: CardinalidadUML[] = ['1', '0..1', '*', '1..*', '0..*'];

export const TIPOS_DATO_DISPONIBLES = [
  'String',
  'Integer',
  'Long',
  'Double',
  'Boolean',
  'Date',
  'void',
  'Object',
  'List',
] as const;

export interface AtributoUML {
  id?: number;
  nombre: string;
  tipoDato: string;
  visibilidad: VisibilidadUML;
  valorInicial?: string;
  claseId?: number;
}

export interface MetodoUML {
  id?: number;
  nombre: string;
  tipoRetorno: string;
  visibilidad: VisibilidadUML;
  parametros?: string;
  claseId?: number;
}

export interface ClaseUML {
  id?: number;
  nombre: string;
  visibilidad: VisibilidadUML;
  descripcion?: string;
  posicionX: number;
  posicionY: number;
  modeloId?: number;
  atributos: AtributoUML[];
  metodos: MetodoUML[];
}

export interface RelacionUML {
  id?: number;
  tipoRelacion: TipoRelacionUML;
  claseOrigenId: number;
  claseOrigenNombre?: string;
  claseDestinoId: number;
  claseDestinoNombre?: string;
  cardinalidadOrigen: string;
  cardinalidadDestino: string;
  descripcion?: string;
  modeloId?: number;
}

export interface ModeloUML {
  id: number;
  nombre: string;
  version: string;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  proyectoId?: number;
  clases: ClaseUML[];
  relaciones: RelacionUML[];
}

export interface ProyectoUML {
  id: number;
  nombre: string;
  descripcion?: string;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  usuarioPropietarioId?: number;
  usuarioPropietarioEmail?: string;
  estado: string;
  modelos: ModeloUML[];
}
