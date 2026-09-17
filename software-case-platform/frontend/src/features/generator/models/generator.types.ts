export interface GeneratorRequestDTO {
  groupId?: string;
  artifactId?: string;
  packageName?: string;
  projectName?: string;
  databaseName?: string;
  serverPort?: number;
  includeDocker?: boolean;
  includeSwagger?: boolean;
}

export interface GeneratedFileDTO {
  relativePath: string;
  content: string;
  category: 'ENTITY' | 'REPOSITORY' | 'SERVICE' | 'CONTROLLER' | 'DTO' | 'CONFIG' | 'BUILD' | 'DOCKER' | 'DOCS';
  sizeBytes: number;
}

export interface GeneratedProjectPreviewDTO {
  modeloId: number;
  projectName: string;
  packageName: string;
  totalFiles: number;
  totalEntities: number;
  files: GeneratedFileDTO[];
}

export interface GeneratorResponseDTO {
  modeloId: number;
  projectName: string;
  packageName: string;
  totalFiles: number;
  totalEntities: number;
  zipDownloadUrl: string;
  fechaGeneracion: string;
  mensaje: string;
}
