import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, renderHook, act } from '@testing-library/react';
import { imageUmlService } from '../services/imageUmlService';
import { useImageUML } from '../hooks/useImageUML';
import { ImageUploader } from '../components/ImageUploader';
import { ProcessingStatus } from '../components/ProcessingStatus';
import { ResultPreview } from '../components/ResultPreview';
import {
  ImageUploadResponseDTO,
  ImageUMLDetectedDTO,
} from '../models/imageuml.types';
import { apiClient } from '../../../services/api';

vi.mock('../../../services/api', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    defaults: { baseURL: 'http://localhost:8080' },
  },
}));

// Mock URL.createObjectURL y URL.revokeObjectURL para entorno jsdom
if (typeof window !== 'undefined') {
  window.URL.createObjectURL = vi.fn(() => 'blob:http://localhost/test-uuid');
  window.URL.revokeObjectURL = vi.fn();
}

describe('Fase 8: Conversión de imagen a modelo UML mediante visión artificial', () => {
  const mockDetectedUml: ImageUMLDetectedDTO = {
    clases: [
      {
        nombre: 'Cliente',
        visibilidad: 'PUBLIC',
        posicionX: 100,
        posicionY: 100,
        atributos: [
          { nombre: 'nombre', tipoDato: 'String', visibilidad: 'PRIVATE' },
          { nombre: 'edad', tipoDato: 'int', visibilidad: 'PRIVATE' },
        ],
        metodos: [
          { nombre: 'calcularTotal', tipoRetorno: 'double', visibilidad: 'PUBLIC', parametros: '' },
        ],
      },
      {
        nombre: 'Venta',
        visibilidad: 'PUBLIC',
        posicionX: 450,
        posicionY: 100,
        atributos: [
          { nombre: 'codigo', tipoDato: 'String', visibilidad: 'PRIVATE' },
          { nombre: 'total', tipoDato: 'double', visibilidad: 'PRIVATE' },
        ],
        metodos: [],
      },
    ],
    relaciones: [
      {
        claseOrigen: 'Cliente',
        claseDestino: 'Venta',
        tipoRelacion: 'ASOCIACION',
        cardinalidadOrigen: '1',
        cardinalidadDestino: '*',
        descripcion: 'Realiza',
      },
    ],
    nivelConfianza: 0.96,
    advertencias: ['Borde de clase detectado con ligera rotación corregida'],
    motorUtilizado: 'COMPUTER_VISION_OCR_ENGINE',
    tiempoProcesamientoMs: 340,
  };

  const mockUploadResponse: ImageUploadResponseDTO = {
    idImagen: 42,
    nombreArchivo: 'diagrama_conceptual.png',
    tamanioBytes: 154200,
    ancho: 800,
    alto: 600,
    estadoProcesamiento: 'PROCESADA',
    mensaje: 'Imagen procesada y entidades UML detectadas con éxito',
    fechaCarga: '2026-09-17T10:00:00',
    resultadoUML: mockDetectedUml,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ---------------------------------------------------------------------------
  // 1. PRUEBAS DEL SERVICIO imageUmlService
  // ---------------------------------------------------------------------------
  describe('imageUmlService', () => {
    it('uploadImage envía archivo multipart y retorna resultado detectado', async () => {
      vi.mocked(apiClient.post).mockResolvedValueOnce({ data: mockUploadResponse });

      const file = new File(['dummy-content'], 'diagrama.png', { type: 'image/png' });
      const response = await imageUmlService.uploadImage(file, 1);

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/imageuml/upload',
        expect.any(FormData),
        expect.objectContaining({
          headers: { 'Content-Type': 'multipart/form-data' },
        })
      );
      expect(response.idImagen).toBe(42);
      expect(response.resultadoUML.clases).toHaveLength(2);
      expect(response.resultadoUML.relaciones[0].claseOrigen).toBe('Cliente');
    });

    it('applyDetectedModel envía el payload del modelo detectado', async () => {
      const mockUpdatedModel = {
        id: 1,
        nombre: 'Modelo Actualizado',
        version: '1.1',
        clases: [],
        relaciones: [],
      };
      vi.mocked(apiClient.post).mockResolvedValueOnce({ data: mockUpdatedModel });

      const result = await imageUmlService.applyDetectedModel(1, {
        idImagen: 42,
        modeloAjustado: mockDetectedUml,
        limpiarModeloExistente: false,
        comentario: 'Test importación',
      });

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/imageuml/models/1/apply',
        expect.objectContaining({
          idImagen: 42,
          limpiarModeloExistente: false,
        })
      );
      expect(result.id).toBe(1);
    });

    it('getImageDetail obtiene el detalle de la imagen', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockUploadResponse });

      const result = await imageUmlService.getImageDetail(42);
      expect(apiClient.get).toHaveBeenCalledWith('/api/imageuml/42');
      expect(result.nombreArchivo).toBe('diagrama_conceptual.png');
    });

    it('getImageFileUrl retorna la URL adecuada', () => {
      const url = imageUmlService.getImageFileUrl(42);
      expect(url).toContain('/api/imageuml/42/file');
    });
  });

  // ---------------------------------------------------------------------------
  // 2. PRUEBAS DEL HOOK useImageUML
  // ---------------------------------------------------------------------------
  describe('useImageUML Hook', () => {
    it('valida formato permitido (PNG, JPG, JPEG) y rechaza otros', () => {
      const { result } = renderHook(() => useImageUML());

      const invalidFile = new File(['text'], 'doc.pdf', { type: 'application/pdf' });
      act(() => {
        const accepted = result.current.handleSelectFile(invalidFile);
        expect(accepted).toBe(false);
      });
      expect(result.current.error).toContain('Formato no soportado');

      const validFile = new File(['image-data'], 'diagram.png', { type: 'image/png' });
      act(() => {
        const accepted = result.current.handleSelectFile(validFile);
        expect(accepted).toBe(true);
      });
      expect(result.current.file).toBe(validFile);
      expect(result.current.error).toBeNull();
    });

    it('rechaza archivos que exceden el tamaño de 10MB', () => {
      const { result } = renderHook(() => useImageUML());
      // Archivo simulado de 11MB
      const hugeFile = new File(['a'], 'huge.png', { type: 'image/png' });
      Object.defineProperty(hugeFile, 'size', { value: 11 * 1024 * 1024 });

      act(() => {
        const accepted = result.current.handleSelectFile(hugeFile);
        expect(accepted).toBe(false);
      });
      expect(result.current.error).toContain('supera el tamaño máximo permitido');
    });

    it('permite modificar clases, atributos y relaciones detectadas', async () => {
      vi.mocked(apiClient.post).mockResolvedValueOnce({ data: mockUploadResponse });

      const { result } = renderHook(() => useImageUML());
      const file = new File(['image'], 'diag.png', { type: 'image/png' });

      act(() => {
        result.current.handleSelectFile(file);
      });

      await act(async () => {
        await result.current.processImage(1);
      });

      expect(result.current.step).toBe('preview');
      expect(result.current.previewUml?.clases).toHaveLength(2);

      // Editar nombre de clase
      act(() => {
        result.current.updateDetectedClass(0, { nombre: 'ClientePremium' });
      });
      expect(result.current.previewUml?.clases[0].nombre).toBe('ClientePremium');

      // Editar atributo
      act(() => {
        result.current.updateDetectedAttribute(0, 0, { nombre: 'nombreCompleto' });
      });
      expect(result.current.previewUml?.clases[0].atributos[0].nombre).toBe('nombreCompleto');

      // Editar relación y cardinalidad
      act(() => {
        result.current.updateDetectedRelation(0, { cardinalidadOrigen: '0..1' });
      });
      expect(result.current.previewUml?.relaciones[0].cardinalidadOrigen).toBe('0..1');
    });
  });

  // ---------------------------------------------------------------------------
  // 3. PRUEBAS DE COMPONENTES DE VISIÓN Y UI
  // ---------------------------------------------------------------------------
  describe('Componentes UI de Visión UML', () => {
    it('ImageUploader muestra zona de carga y advertencia si hay error', () => {
      const handleSelect = vi.fn();
      const handleProcess = vi.fn();

      render(
        <ImageUploader
          file={null}
          filePreviewUrl={null}
          onSelectFile={handleSelect}
          onProcess={handleProcess}
          isLoading={false}
          error="Formato no soportado"
        />
      );

      expect(screen.getByText(/Arrastra una imagen de diagrama aquí/i)).toBeDefined();
      expect(screen.getByText(/Formato no soportado/i)).toBeDefined();
    });

    it('ProcessingStatus muestra los 3 pasos obligatorios', () => {
      render(<ProcessingStatus currentStepMessage="Detectando clases..." />);

      expect(screen.getByText(/Procesando imagen\.\.\./i)).toBeDefined();
      expect(screen.getByText(/Detectando clases\.\.\./i)).toBeDefined();
      expect(screen.getByText(/Generando modelo UML\.\.\./i)).toBeDefined();
    });

    it('ResultPreview permite visualizar y corregir clases y relaciones', () => {
      const handleUpdateClass = vi.fn();
      const handleDeleteClass = vi.fn();
      const handleAddClass = vi.fn();
      const handleApply = vi.fn();
      const handleCancel = vi.fn();

      render(
        <ResultPreview
          previewUml={mockDetectedUml}
          filePreviewUrl="blob:http://localhost/test"
          limpiarModeloExistente={false}
          setLimpiarModeloExistente={vi.fn()}
          onUpdateClass={handleUpdateClass}
          onDeleteClass={handleDeleteClass}
          onAddClass={handleAddClass}
          onUpdateAttribute={vi.fn()}
          onDeleteAttribute={vi.fn()}
          onAddAttribute={vi.fn()}
          onUpdateMethod={vi.fn()}
          onDeleteMethod={vi.fn()}
          onAddMethod={vi.fn()}
          onUpdateRelation={vi.fn()}
          onDeleteRelation={vi.fn()}
          onAddRelation={vi.fn()}
          onApply={handleApply}
          onCancel={handleCancel}
          isLoading={false}
        />
      );

      expect(screen.getByText(/Clases Detectadas \(2\)/i)).toBeDefined();
      expect(screen.getByText(/Relaciones y Cardinalidades \(1\)/i)).toBeDefined();
      expect(screen.getAllByDisplayValue('Cliente').length).toBeGreaterThanOrEqual(1);
      expect(screen.getAllByDisplayValue('Venta').length).toBeGreaterThanOrEqual(1);

      const applyBtn = screen.getByRole('button', { name: /Aplicar al Diagrama UML/i });
      fireEvent.click(applyBtn);
      expect(handleApply).toHaveBeenCalledTimes(1);
    });
  });
});
