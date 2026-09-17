export type TipoOperacionHistorial = 'CREATE' | 'UPDATE' | 'DELETE' | 'RESTORE';

export interface SnapshotAtributo {
  id?: number;
  nombre: string;
  tipoDato: string;
  visibilidad: string;
  valorInicial?: string;
}

export interface SnapshotMetodo {
  id?: number;
  nombre: string;
  tipoRetorno: string;
  visibilidad: string;
  parametros?: string;
}

export interface SnapshotClase {
  id?: number;
  nombre: string;
  visibilidad: string;
  descripcion?: string;
  posicionX: number;
  posicionY: number;
  atributos: SnapshotAtributo[];
  metodos: SnapshotMetodo[];
}

export interface SnapshotRelacion {
  id?: number;
  tipoRelacion: string;
  claseOrigenId?: number;
  claseOrigenNombre?: string;
  claseDestinoId?: number;
  claseDestinoNombre?: string;
  cardinalidadOrigen?: string;
  cardinalidadDestino?: string;
  descripcion?: string;
}

export interface SnapshotModelo {
  modeloId: number;
  nombreModelo: string;
  version: string;
  clases: SnapshotClase[];
  relaciones: SnapshotRelacion[];
}

export interface VersionModeloDTO {
  id: number;
  numeroVersion: string;
  nombreVersion: string;
  descripcion?: string;
  modeloId: number;
  modeloNombre?: string;
  usuarioCreadorId?: number;
  usuarioCreadorNombre?: string;
  usuarioCreadorEmail?: string;
  fechaCreacion: string;
  estado: string;
  snapshotJson?: string;
  snapshot?: SnapshotModelo;
}

export interface CreateVersionDTO {
  modeloId: number;
  numeroVersion?: string;
  nombreVersion: string;
  descripcion?: string;
}

export interface HistorialCambioDTO {
  id: number;
  tipoOperacion: TipoOperacionHistorial;
  elementoModificado: string;
  idElemento?: string;
  datosAnteriores?: string;
  datosNuevos?: string;
  usuarioId?: number;
  usuarioEmail: string;
  usuarioNombre: string;
  fechaCambio: string;
  versionModeloId?: number;
  versionNumero?: string;
  modeloId: number;
  descripcionResumen: string;
}

export interface RestoreVersionDTO {
  comentario?: string;
}
