import { useState, useCallback } from 'react';
import {
  XMIExportResponseDTO,
  XMIImportResponseDTO,
  XMIValidationResponseDTO,
} from '../models/ea.types';
import { enterpriseArchitectService } from '../services/enterpriseArchitectService';

export const useEnterpriseArchitect = () => {
  const [activeTab, setActiveTab] = useState<'import' | 'export'>('import');
  const [file, setFile] = useState<File | null>(null);
  const [validation, setValidation] = useState<XMIValidationResponseDTO | null>(null);
  const [exportPreview, setExportPreview] = useState<XMIExportResponseDTO | null>(null);
  const [limpiarExistente, setLimpiarExistente] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isExporting, setIsExporting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  /**
   * Maneja la selección de un archivo XMI/XML y dispara su validación automática.
   */
  const handleSelectFile = useCallback(async (selectedFile: File) => {
    setFile(selectedFile);
    setError(null);
    setSuccess(null);
    setValidation(null);

    // Validación básica de extensión
    const ext = selectedFile.name.toLowerCase();
    if (!ext.endsWith('.xml') && !ext.endsWith('.xmi')) {
      setError('Por favor selecciona un archivo con extensión .xml o .xmi');
      return;
    }

    setIsLoading(true);
    try {
      const result = await enterpriseArchitectService.validateXMI(selectedFile);
      setValidation(result);
      if (!result.valido) {
        setError(result.mensaje || 'El archivo contiene errores de sintaxis XMI o no posee clases interpretables.');
      }
    } catch (err: any) {
      console.error('Error al validar archivo XMI:', err);
      const errMsg = err.response?.data?.message || err.message || 'Error al validar el archivo XMI';
      setError(errMsg);
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Ejecuta la importación del archivo validado al modelo actual.
   */
  const executeImport = useCallback(
    async (modeloId: number): Promise<XMIImportResponseDTO | null> => {
      if (!file) {
        setError('No se ha seleccionado ningún archivo XMI');
        return null;
      }

      setIsLoading(true);
      setError(null);
      setSuccess(null);

      try {
        const response = await enterpriseArchitectService.importToExistingModel(
          modeloId,
          file,
          limpiarExistente
        );
        setSuccess(
          `¡Importación exitosa! Se importaron ${response.clasesImportadas} clases y ${response.relacionesImportadas} relaciones. Versión registrada: ${response.versionNumero || 'v1.0'}.`
        );
        return response;
      } catch (err: any) {
        console.error('Error al importar archivo XMI:', err);
        const errMsg = err.response?.data?.message || err.message || 'Error al importar archivo XMI';
        setError(errMsg);
        return null;
      } finally {
        setIsLoading(false);
      }
    },
    [file, limpiarExistente]
  );

  /**
   * Carga la vista previa de exportación para el modelo actual.
   */
  const loadExportPreview = useCallback(async (modeloId: number) => {
    setIsExporting(true);
    setError(null);
    try {
      const data = await enterpriseArchitectService.previewExport(modeloId);
      setExportPreview(data);
    } catch (err: any) {
      console.error('Error al previsualizar exportación XMI:', err);
      const errMsg = err.response?.data?.message || err.message || 'Error al preparar exportación XMI';
      setError(errMsg);
    } finally {
      setIsExporting(false);
    }
  }, []);

  /**
   * Descarga el archivo XMI (.xml) generado.
   */
  const executeExport = useCallback(
    async (modeloId: number, nombreArchivo?: string) => {
      setIsExporting(true);
      setError(null);
      try {
        await enterpriseArchitectService.downloadXMI(modeloId, nombreArchivo);
        setSuccess('¡Archivo XMI descargado exitosamente para Enterprise Architect!');
      } catch (err: any) {
        console.error('Error al descargar archivo XMI:', err);
        const errMsg = err.response?.data?.message || err.message || 'Error al descargar archivo XMI';
        setError(errMsg);
      } finally {
        setIsExporting(false);
      }
    },
    []
  );

  const reset = useCallback(() => {
    setFile(null);
    setValidation(null);
    setExportPreview(null);
    setError(null);
    setSuccess(null);
    setIsLoading(false);
    setIsExporting(false);
  }, []);

  return {
    activeTab,
    setActiveTab,
    file,
    validation,
    exportPreview,
    limpiarExistente,
    setLimpiarExistente,
    isLoading,
    isExporting,
    error,
    success,
    handleSelectFile,
    executeImport,
    executeExport,
    loadExportPreview,
    reset,
  };
};
