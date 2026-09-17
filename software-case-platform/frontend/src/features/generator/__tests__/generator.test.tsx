import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, renderHook, act } from '@testing-library/react';
import { generatorService } from '../services/generatorService';
import { useGenerator } from '../hooks/useGenerator';
import { CodeGeneratorModal } from '../components/CodeGeneratorModal';
import { GeneratedProjectPreviewDTO } from '../models/generator.types';
import { apiClient } from '../../../services/api';

vi.mock('../../../services/api', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    defaults: { baseURL: 'http://localhost:8080' },
  },
}));

if (typeof window !== 'undefined') {
  window.URL.createObjectURL = vi.fn(() => 'blob:http://localhost/test-zip');
  window.URL.revokeObjectURL = vi.fn();
}

describe('Fase 9: Generador automático de Backend Spring Boot desde modelos UML', () => {
  const mockPreview: GeneratedProjectPreviewDTO = {
    modeloId: 1,
    projectName: 'TiendaBackend',
    packageName: 'com.tienda.backend',
    totalFiles: 4,
    totalEntities: 1,
    files: [
      {
        relativePath: 'pom.xml',
        content: '<project><artifactId>tienda-backend</artifactId></project>',
        category: 'BUILD',
        sizeBytes: 60,
      },
      {
        relativePath: 'src/main/resources/application.yml',
        content: 'server:\n  port: 8081',
        category: 'CONFIG',
        sizeBytes: 25,
      },
      {
        relativePath: 'src/main/java/com/tienda/backend/entity/Producto.java',
        content: 'package com.tienda.backend.entity;\n@Entity\npublic class Producto {}',
        category: 'ENTITY',
        sizeBytes: 70,
      },
      {
        relativePath: 'src/main/java/com/tienda/backend/controller/ProductoController.java',
        content: 'package com.tienda.backend.controller;\n@RestController\npublic class ProductoController {}',
        category: 'CONTROLLER',
        sizeBytes: 95,
      },
    ],
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ---------------------------------------------------------------------------
  // 1. PRUEBAS DEL SERVICIO generatorService
  // ---------------------------------------------------------------------------
  describe('generatorService', () => {
    it('previewProject consulta el endpoint de previsualización', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockPreview });

      const result = await generatorService.previewProject(1, {
        packageName: 'com.tienda.backend',
        projectName: 'TiendaBackend',
      });

      expect(apiClient.get).toHaveBeenCalledWith(
        expect.stringContaining('/api/generator/project/1/preview')
      );
      expect(result.projectName).toBe('TiendaBackend');
      expect(result.totalFiles).toBe(4);
    });

    it('generateProject solicita la generación del proyecto', async () => {
      const mockResponse = {
        modeloId: 1,
        projectName: 'TiendaBackend',
        packageName: 'com.tienda.backend',
        totalFiles: 4,
        totalEntities: 1,
        zipDownloadUrl: '/api/generator/project/1/zip',
        fechaGeneracion: '2026-09-17T12:00:00',
        mensaje: 'Generado con éxito',
      };
      vi.mocked(apiClient.post).mockResolvedValueOnce({ data: mockResponse });

      const response = await generatorService.generateProject(1, {
        projectName: 'TiendaBackend',
      });

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/generator/project/1',
        expect.objectContaining({ projectName: 'TiendaBackend' })
      );
      expect(response.totalEntities).toBe(1);
    });

    it('downloadZip solicita el archivo binario y crea el blob para descarga', async () => {
      const mockBlob = new Blob(['fake-zip-data'], { type: 'application/zip' });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockBlob });

      const appendChildSpy = vi.spyOn(document.body, 'appendChild');

      await generatorService.downloadZip(1, { projectName: 'MiProyecto' });

      expect(apiClient.get).toHaveBeenCalledWith(
        expect.stringContaining('/api/generator/project/1/zip'),
        expect.objectContaining({ responseType: 'blob' })
      );
      expect(appendChildSpy).toHaveBeenCalled();
    });
  });

  // ---------------------------------------------------------------------------
  // 2. PRUEBAS DEL HOOK useGenerator
  // ---------------------------------------------------------------------------
  describe('useGenerator Hook', () => {
    it('fetchPreview carga el proyecto y selecciona pom.xml por defecto', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockPreview });

      const { result } = renderHook(() => useGenerator());

      await act(async () => {
        await result.current.fetchPreview(1);
      });

      expect(result.current.preview).toBeDefined();
      expect(result.current.preview?.totalFiles).toBe(4);
      expect(result.current.selectedFile?.relativePath).toBe('pom.xml');
    });

    it('updateOptions actualiza los parámetros de configuración', () => {
      const { result } = renderHook(() => useGenerator());

      act(() => {
        result.current.updateOptions({
          packageName: 'com.innovacion.api',
          serverPort: 9090,
        });
      });

      expect(result.current.options.packageName).toBe('com.innovacion.api');
      expect(result.current.options.serverPort).toBe(9090);
    });
  });

  // ---------------------------------------------------------------------------
  // 3. PRUEBAS DEL COMPONENTE CodeGeneratorModal
  // ---------------------------------------------------------------------------
  describe('CodeGeneratorModal Component', () => {
    it('renderiza la previsualización del código y el árbol de capas', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockPreview });

      await act(async () => {
        render(
          <CodeGeneratorModal
            isOpen={true}
            onClose={vi.fn()}
            modeloId={1}
            nombreModelo="Tienda Online"
          />
        );
      });

      // Esperar a que se cargue la previsualización
      expect(await screen.findByText(/Generador de Backend Spring Boot/i)).toBeDefined();
      expect(screen.getByText(/Estructura del Proyecto/i)).toBeDefined();

      const downloadButton = screen.getByRole('button', { name: /Descargar Proyecto \(\.ZIP\)/i });
      expect(downloadButton).toBeDefined();
    });

    it('permite cambiar el archivo seleccionado en el árbol', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockPreview });

      await act(async () => {
        render(
          <CodeGeneratorModal
            isOpen={true}
            onClose={vi.fn()}
            modeloId={1}
            nombreModelo="Tienda Online"
          />
        );
      });

      // Buscar el nodo Producto.java y seleccionarlo
      const entityNode = await screen.findByText('Producto.java');
      fireEvent.click(entityNode);

      // El visor de código debe mostrar el contenido del archivo seleccionado
      expect(await screen.findByText(/public class Producto/i)).toBeDefined();
    });
  });
});
