import { useState, useCallback } from 'react';
import {
  GeneratedFileDTO,
  GeneratedProjectPreviewDTO,
  GeneratorRequestDTO,
} from '../models/generator.types';
import { generatorService } from '../services/generatorService';

export function useGenerator() {
  const [preview, setPreview] = useState<GeneratedProjectPreviewDTO | null>(null);
  const [selectedFile, setSelectedFile] = useState<GeneratedFileDTO | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isDownloading, setIsDownloading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const [options, setOptions] = useState<GeneratorRequestDTO>({
    packageName: 'com.caseplatform.generated',
    projectName: 'SpringBootBackend',
    databaseName: 'app_db',
    serverPort: 8081,
    includeDocker: true,
  });

  const updateOptions = useCallback((newOptions: Partial<GeneratorRequestDTO>) => {
    setOptions((prev) => ({ ...prev, ...newOptions }));
  }, []);

  const fetchPreview = useCallback(
    async (modeloId: number) => {
      setIsLoading(true);
      setError(null);
      setSuccessMessage(null);

      try {
        const data = await generatorService.previewProject(modeloId, options);
        setPreview(data);

        // Seleccionar automáticamente pom.xml o el primer archivo para vista previa
        if (data.files && data.files.length > 0) {
          const defaultFile =
            data.files.find((f) => f.relativePath === 'pom.xml') || data.files[0];
          setSelectedFile(defaultFile);
        }
      } catch (err: any) {
        const msg =
          err.response?.data?.message ||
          err.message ||
          'Error al obtener la previsualización del proyecto.';
        setError(msg);
      } finally {
        setIsLoading(false);
      }
    },
    [options]
  );

  const downloadZip = useCallback(
    async (modeloId: number) => {
      setIsDownloading(true);
      setError(null);
      setSuccessMessage(null);

      try {
        await generatorService.downloadZip(modeloId, options);
        setSuccessMessage('¡Proyecto descargado con éxito!');
      } catch (err: any) {
        const msg =
          err.response?.data?.message ||
          err.message ||
          'Error al descargar el archivo ZIP del proyecto.';
        setError(msg);
      } finally {
        setIsDownloading(false);
      }
    },
    [options]
  );

  return {
    preview,
    selectedFile,
    isLoading,
    isDownloading,
    error,
    successMessage,
    options,
    updateOptions,
    fetchPreview,
    downloadZip,
    setSelectedFile,
    setError,
    setSuccessMessage,
  };
}
