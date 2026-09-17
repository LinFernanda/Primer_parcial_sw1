import { useState, useCallback, useEffect } from 'react';
import {
  AtributoDetectadoDTO,
  ClaseDetectadaDTO,
  ImageUMLDetectedDTO,
  ImageUploadResponseDTO,
  MetodoDetectadoDTO,
  ModeloUML,
  RelacionDetectadaDTO,
} from '../models/imageuml.types';
import { imageUmlService } from '../services/imageUmlService';

export type ImageUMLStep = 'upload' | 'processing' | 'preview' | 'applied';

const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
const ALLOWED_EXTENSIONS = ['png', 'jpg', 'jpeg'];
const ALLOWED_MIME_TYPES = ['image/png', 'image/jpeg', 'image/pjpeg'];

export function useImageUML() {
  const [step, setStep] = useState<ImageUMLStep>('upload');
  const [file, setFile] = useState<File | null>(null);
  const [filePreviewUrl, setFilePreviewUrl] = useState<string | null>(null);
  const [uploadResponse, setUploadResponse] = useState<ImageUploadResponseDTO | null>(null);
  const [previewUml, setPreviewUml] = useState<ImageUMLDetectedDTO | null>(null);
  const [currentProcessingStep, setCurrentProcessingStep] = useState<string>('Procesando imagen...');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [limpiarModeloExistente, setLimpiarModeloExistente] = useState<boolean>(false);

  // Limpiar URL temporal al desmontar o cambiar de archivo
  useEffect(() => {
    return () => {
      if (filePreviewUrl && filePreviewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(filePreviewUrl);
      }
    };
  }, [filePreviewUrl]);

  /**
   * Validación y selección de imagen desde input o dropzone
   */
  const handleSelectFile = useCallback((selectedFile: File) => {
    setError(null);

    // Validar tamaño
    if (selectedFile.size > MAX_FILE_SIZE_BYTES) {
      setError(`El archivo supera el tamaño máximo permitido de 10 MB (${(selectedFile.size / (1024 * 1024)).toFixed(1)} MB).`);
      return false;
    }

    if (selectedFile.size === 0) {
      setError('El archivo seleccionado está vacío.');
      return false;
    }

    // Validar extensión y mime type
    const extension = selectedFile.name.split('.').pop()?.toLowerCase() || '';
    const isExtensionValid = ALLOWED_EXTENSIONS.includes(extension);
    const isMimeValid = ALLOWED_MIME_TYPES.includes(selectedFile.type) || isExtensionValid;

    if (!isExtensionValid && !isMimeValid) {
      setError('Formato no soportado. Solo se permiten imágenes PNG, JPG o JPEG.');
      return false;
    }

    // Revocar url anterior si existía
    if (filePreviewUrl && filePreviewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(filePreviewUrl);
    }

    const previewUrl = URL.createObjectURL(selectedFile);
    setFile(selectedFile);
    setFilePreviewUrl(previewUrl);
    return true;
  }, [filePreviewUrl]);

  /**
   * Ejecuta el pipeline de procesamiento visual, OCR e IA
   */
  const processImage = useCallback(async (modeloId?: number) => {
    if (!file) {
      setError('Por favor selecciona una imagen para procesar.');
      return;
    }

    setIsLoading(true);
    setError(null);
    setStep('processing');
    setCurrentProcessingStep('Procesando imagen...');

    // Temporizadores para feedback visual interactivo mientras el backend analiza
    const timer1 = setTimeout(() => {
      setCurrentProcessingStep('Detectando clases y entidades UML...');
    }, 1200);

    const timer2 = setTimeout(() => {
      setCurrentProcessingStep('Generando modelo UML conceptual y relaciones...');
    }, 2500);

    try {
      const response = await imageUmlService.uploadImage(file, modeloId);
      clearTimeout(timer1);
      clearTimeout(timer2);

      setUploadResponse(response);
      // Clonar estructura para permitir edición manual sin mutar el original
      setPreviewUml(JSON.parse(JSON.stringify(response.resultadoUML)));
      setStep('preview');
    } catch (err: any) {
      clearTimeout(timer1);
      clearTimeout(timer2);
      const msg =
        err.response?.data?.message ||
        err.message ||
        'Error durante el procesamiento visual de la imagen.';
      setError(msg);
      setStep('upload');
    } finally {
      setIsLoading(false);
    }
  }, [file]);

  /**
   * Modificación manual de clases detectadas
   */
  const updateDetectedClass = useCallback((index: number, updated: Partial<ClaseDetectadaDTO>) => {
    setPreviewUml((prev) => {
      if (!prev) return prev;
      const nuevasClases = [...prev.clases];
      nuevasClases[index] = { ...nuevasClases[index], ...updated };
      return { ...prev, clases: nuevasClases };
    });
  }, []);

  const deleteDetectedClass = useCallback((index: number) => {
    setPreviewUml((prev) => {
      if (!prev) return prev;
      const className = prev.clases[index]?.nombre;
      const nuevasClases = prev.clases.filter((_, i) => i !== index);
      // Eliminar también relaciones que apunten a esta clase
      const nuevasRelaciones = prev.relaciones.filter(
        (r) => r.claseOrigen !== className && r.claseDestino !== className
      );
      return { ...prev, clases: nuevasClases, relaciones: nuevasRelaciones };
    });
  }, []);

  const addDetectedClass = useCallback((clase: ClaseDetectadaDTO) => {
    setPreviewUml((prev) => {
      if (!prev) return prev;
      return { ...prev, clases: [...prev.clases, clase] };
    });
  }, []);

  /**
   * Modificación manual de atributos de una clase
   */
  const updateDetectedAttribute = useCallback(
    (claseIndex: number, attrIndex: number, updated: Partial<AtributoDetectadoDTO>) => {
      setPreviewUml((prev) => {
        if (!prev) return prev;
        const nuevasClases = [...prev.clases];
        const clase = { ...nuevasClases[claseIndex] };
        const nuevosAtributos = [...clase.atributos];
        nuevosAtributos[attrIndex] = { ...nuevosAtributos[attrIndex], ...updated };
        clase.atributos = nuevosAtributos;
        nuevasClases[claseIndex] = clase;
        return { ...prev, clases: nuevasClases };
      });
    },
    []
  );

  const deleteDetectedAttribute = useCallback((claseIndex: number, attrIndex: number) => {
    setPreviewUml((prev) => {
      if (!prev) return prev;
      const nuevasClases = [...prev.clases];
      const clase = { ...nuevasClases[claseIndex] };
      clase.atributos = clase.atributos.filter((_, i) => i !== attrIndex);
      nuevasClases[claseIndex] = clase;
      return { ...prev, clases: nuevasClases };
    });
  }, []);

  const addDetectedAttribute = useCallback((claseIndex: number, attr: AtributoDetectadoDTO) => {
    setPreviewUml((prev) => {
      if (!prev) return prev;
      const nuevasClases = [...prev.clases];
      const clase = { ...nuevasClases[claseIndex] };
      clase.atributos = [...clase.atributos, attr];
      nuevasClases[claseIndex] = clase;
      return { ...prev, clases: nuevasClases };
    });
  }, []);

  /**
   * Modificación manual de métodos de una clase
   */
  const updateDetectedMethod = useCallback(
    (claseIndex: number, metodoIndex: number, updated: Partial<MetodoDetectadoDTO>) => {
      setPreviewUml((prev) => {
        if (!prev) return prev;
        const nuevasClases = [...prev.clases];
        const clase = { ...nuevasClases[claseIndex] };
        const nuevosMetodos = [...clase.metodos];
        nuevosMetodos[metodoIndex] = { ...nuevosMetodos[metodoIndex], ...updated };
        clase.metodos = nuevosMetodos;
        nuevasClases[claseIndex] = clase;
        return { ...prev, clases: nuevasClases };
      });
    },
    []
  );

  const deleteDetectedMethod = useCallback((claseIndex: number, metodoIndex: number) => {
    setPreviewUml((prev) => {
      if (!prev) return prev;
      const nuevasClases = [...prev.clases];
      const clase = { ...nuevasClases[claseIndex] };
      clase.metodos = clase.metodos.filter((_, i) => i !== metodoIndex);
      nuevasClases[claseIndex] = clase;
      return { ...prev, clases: nuevasClases };
    });
  }, []);

  const addDetectedMethod = useCallback((claseIndex: number, metodo: MetodoDetectadoDTO) => {
    setPreviewUml((prev) => {
      if (!prev) return prev;
      const nuevasClases = [...prev.clases];
      const clase = { ...nuevasClases[claseIndex] };
      clase.metodos = [...clase.metodos, metodo];
      nuevasClases[claseIndex] = clase;
      return { ...prev, clases: nuevasClases };
    });
  }, []);

  /**
   * Modificación manual de relaciones detectadas
   */
  const updateDetectedRelation = useCallback(
    (index: number, updated: Partial<RelacionDetectadaDTO>) => {
      setPreviewUml((prev) => {
        if (!prev) return prev;
        const nuevasRelaciones = [...prev.relaciones];
        nuevasRelaciones[index] = { ...nuevasRelaciones[index], ...updated };
        return { ...prev, relaciones: nuevasRelaciones };
      });
    },
    []
  );

  const deleteDetectedRelation = useCallback((index: number) => {
    setPreviewUml((prev) => {
      if (!prev) return prev;
      const nuevasRelaciones = prev.relaciones.filter((_, i) => i !== index);
      return { ...prev, relaciones: nuevasRelaciones };
    });
  }, []);

  const addDetectedRelation = useCallback((relacion: RelacionDetectadaDTO) => {
    setPreviewUml((prev) => {
      if (!prev) return prev;
      return { ...prev, relaciones: [...prev.relaciones, relacion] };
    });
  }, []);

  /**
   * Aplica el modelo detectado y corregido al modelo en el canvas UML
   */
  const applyToModel = useCallback(
    async (modeloId: number): Promise<ModeloUML> => {
      if (!uploadResponse || !previewUml) {
        throw new Error('No hay un modelo detectado listo para aplicar.');
      }

      setIsLoading(true);
      setError(null);

      try {
        const modeloActualizado = await imageUmlService.applyDetectedModel(modeloId, {
          idImagen: uploadResponse.idImagen,
          modeloAjustado: previewUml,
          limpiarModeloExistente,
          comentario: `Importado desde imagen: ${uploadResponse.nombreArchivo}`,
        });

        setStep('applied');
        return modeloActualizado;
      } catch (err: any) {
        const msg =
          err.response?.data?.message ||
          err.message ||
          'Error al aplicar el modelo detectado al editor.';
        setError(msg);
        throw err;
      } finally {
        setIsLoading(false);
      }
    },
    [uploadResponse, previewUml, limpiarModeloExistente]
  );

  /**
   * Reinicia el estado del hook para permitir una nueva carga
   */
  const reset = useCallback(() => {
    if (filePreviewUrl && filePreviewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(filePreviewUrl);
    }
    setStep('upload');
    setFile(null);
    setFilePreviewUrl(null);
    setUploadResponse(null);
    setPreviewUml(null);
    setError(null);
    setIsLoading(false);
    setLimpiarModeloExistente(false);
  }, [filePreviewUrl]);

  return {
    step,
    setStep,
    file,
    filePreviewUrl,
    uploadResponse,
    previewUml,
    currentProcessingStep,
    isLoading,
    error,
    limpiarModeloExistente,
    setLimpiarModeloExistente,
    handleSelectFile,
    processImage,
    updateDetectedClass,
    deleteDetectedClass,
    addDetectedClass,
    updateDetectedAttribute,
    deleteDetectedAttribute,
    addDetectedAttribute,
    updateDetectedMethod,
    deleteDetectedMethod,
    addDetectedMethod,
    updateDetectedRelation,
    deleteDetectedRelation,
    addDetectedRelation,
    applyToModel,
    reset,
  };
}
