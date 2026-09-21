import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, renderHook, act } from '@testing-library/react';
import { enterpriseArchitectService } from '../services/enterpriseArchitectService';
import { useEnterpriseArchitect } from '../hooks/useEnterpriseArchitect';
import { EnterpriseArchitectModal } from '../components/EnterpriseArchitectModal';
import {
  XMIExportResponseDTO,
  XMIImportResponseDTO,
  XMIValidationResponseDTO,
} from '../models/ea.types';
import { apiClient } from '../../../services/api';

vi.mock('../../../services/api', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    defaults: { baseURL: 'http://localhost:8080' },
  },
}));

if (typeof window !== 'undefined') {
  window.URL.createObjectURL = vi.fn(() => 'blob:http://localhost/test-xmi');
  window.URL.revokeObjectURL = vi.fn();
}

describe('Fase 10: Interoperabilidad con Enterprise Architect mediante XMI', () => {
  const mockValidationResult: XMIValidationResponseDTO = {
    valido: true,
    mensaje: 'Archivo XMI validado exitosamente',
    validacion: {
      valido: true,
      errores: [],
      advertencias: [],
      totalClases: 2,
      totalAtributos: 3,
      totalMetodos: 1,
      totalRelaciones: 1,
      nombreModelo: 'ModeloComercial',
      xmiVersion: '2.1',
      exportador: 'Enterprise Architect',
    },
    preview: {
      nombre: 'ModeloComercial',
      xmiVersion: '2.1',
      exporter: 'Enterprise Architect',
      clases: [
        {
          nombre: 'Cliente',
          visibilidad: 'PUBLIC',
          atributos: [
            { nombre: 'id', tipoDato: 'Integer' },
            { nombre: 'nombre', tipoDato: 'String' },
          ],
          metodos: [{ nombre: 'calcularDescuento', tipoRetorno: 'Double' }],
        },
        {
          nombre: 'Pedido',
          visibilidad: 'PUBLIC',
          atributos: [{ nombre: 'numero', tipoDato: 'String' }],
          metodos: [],
        },
      ],
      relaciones: [
        {
          tipoRelacion: 'ASOCIACION',
          claseOrigenNombre: 'Cliente',
          claseDestinoNombre: 'Pedido',
          cardinalidadOrigen: '1',
          cardinalidadDestino: '0..*',
        },
      ],
    },
  };

  const mockExportPreview: XMIExportResponseDTO = {
    nombreArchivo: 'Modelo_EA_xmi21.xml',
    versionXMI: '2.1',
    totalClases: 2,
    totalRelaciones: 1,
    tamanoBytes: 1540,
    contenidoXML:
      '<?xml version="1.0" encoding="UTF-8"?>\n<xmi:XMI xmi:version="2.1" xmlns:uml="http://schema.omg.org/spec/UML/2.1">\n  <uml:Model name="ModeloComercial"/>\n</xmi:XMI>',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('enterpriseArchitectService', () => {
    it('validateXMI envía FormData al backend y retorna resultado de validación', async () => {
      vi.mocked(apiClient.post).mockResolvedValueOnce({ data: mockValidationResult });

      const testFile = new File(['<xml></xml>'], 'modelo_ea.xml', { type: 'application/xml' });
      const res = await enterpriseArchitectService.validateXMI(testFile);

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/integration/ea/validate',
        expect.any(FormData),
        expect.objectContaining({
          headers: { 'Content-Type': 'multipart/form-data' },
        })
      );
      expect(res.valido).toBe(true);
      expect(res.validacion.totalClases).toBe(2);
    });

    it('importToExistingModel envía archivo y flag limpiarExistente', async () => {
      const mockImportRes: XMIImportResponseDTO = {
        exito: true,
        mensaje: 'Modelo importado',
        modeloId: 1,
        nombreModelo: 'ModeloComercial',
        clasesImportadas: 2,
        atributosImportados: 3,
        metodosImportados: 1,
        relacionesImportadas: 1,
        versionNumero: 'v1.1',
        fecha: new Date().toISOString(),
      };
      vi.mocked(apiClient.post).mockResolvedValueOnce({ data: mockImportRes });

      const testFile = new File(['<xml></xml>'], 'modelo_ea.xml', { type: 'application/xml' });
      const res = await enterpriseArchitectService.importToExistingModel(1, testFile, true);

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/integration/ea/models/1/import?limpiarExistente=true',
        expect.any(FormData),
        expect.any(Object)
      );
      expect(res.exito).toBe(true);
      expect(res.clasesImportadas).toBe(2);
    });

    it('previewExport consulta endpoint de previsualización XMI', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockExportPreview });

      const res = await enterpriseArchitectService.previewExport(1);
      expect(apiClient.get).toHaveBeenCalledWith('/api/integration/ea/models/1/export/preview');
      expect(res.versionXMI).toBe('2.1');
      expect(res.totalClases).toBe(2);
    });

    it('downloadXMI descarga blob y crea enlace temporal', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({
        data: '<xmi:XMI></xmi:XMI>',
      });

      await enterpriseArchitectService.downloadXMI(1, 'prueba.xml');
      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/integration/ea/models/1/export',
        expect.objectContaining({
          responseType: 'blob',
          headers: expect.objectContaining({
            Accept: expect.stringContaining('application/xml'),
          }),
        })
      );
      expect(window.URL.createObjectURL).toHaveBeenCalled();
    });
  });

  describe('useEnterpriseArchitect Hook', () => {
    it('valida archivo al seleccionarlo con handleSelectFile', async () => {
      vi.mocked(apiClient.post).mockResolvedValueOnce({ data: mockValidationResult });

      const { result } = renderHook(() => useEnterpriseArchitect());
      const testFile = new File(['<xml></xml>'], 'diagrama.xmi', { type: 'application/xml' });

      await act(async () => {
        await result.current.handleSelectFile(testFile);
      });

      expect(result.current.file).toBe(testFile);
      expect(result.current.validation?.valido).toBe(true);
      expect(result.current.validation?.validacion.totalClases).toBe(2);
      expect(result.current.error).toBeNull();
    });

    it('rechaza archivos con extensiones no compatibles', async () => {
      const { result } = renderHook(() => useEnterpriseArchitect());
      const invalidFile = new File(['foo'], 'documento.pdf', { type: 'application/pdf' });

      await act(async () => {
        await result.current.handleSelectFile(invalidFile);
      });

      expect(result.current.error).toContain('extensión .xml o .xmi');
    });
  });

  describe('EnterpriseArchitectModal Component', () => {
    it('renderiza correctamente el modal con las pestañas de Importar y Exportar', () => {
      render(
        <EnterpriseArchitectModal
          isOpen={true}
          onClose={vi.fn()}
          modeloId={1}
        />
      );

      expect(
        screen.getByText('Interoperabilidad con Enterprise Architect')
      ).toBeInTheDocument();
      expect(
        screen.getByText(/Importar desde Enterprise Architect/i)
      ).toBeInTheDocument();
      expect(
        screen.getByText(/Exportar a Enterprise Architect/i)
      ).toBeInTheDocument();
    });

    it('cambia entre pestañas y muestra el visor de exportación XMI', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockExportPreview });

      render(
        <EnterpriseArchitectModal
          isOpen={true}
          onClose={vi.fn()}
          modeloId={1}
        />
      );

      const exportTab = screen.getByText(/Exportar a Enterprise Architect/i);
      fireEvent.click(exportTab);

      expect(
        await screen.findByText(/Modelo Listo para Exportación a Enterprise Architect/i)
      ).toBeInTheDocument();
      expect(
        await screen.findByText(/Descargar XMI/i)
      ).toBeInTheDocument();
    });
  });
});
