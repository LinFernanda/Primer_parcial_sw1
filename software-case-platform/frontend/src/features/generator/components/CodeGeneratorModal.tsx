import React, { useEffect, useState } from 'react';
import { useGenerator } from '../hooks/useGenerator';
import { GeneratedFileDTO } from '../models/generator.types';
import {
  Code2,
  Download,
  FolderTree,
  Copy,
  Check,
  RefreshCw,
  X,
  Database,
  FileCode,
  Layers,
  Settings,
  AlertCircle,
  CheckCircle2,
} from 'lucide-react';

interface CodeGeneratorModalProps {
  isOpen: boolean;
  onClose: () => void;
  modeloId: number;
  nombreModelo?: string;
}

export const CodeGeneratorModal: React.FC<CodeGeneratorModalProps> = ({
  isOpen,
  onClose,
  modeloId,
  nombreModelo = 'Diagrama',
}) => {
  const {
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
  } = useGenerator();

  const [copied, setCopied] = useState(false);

  // Cargar previsualización al abrir el modal
  useEffect(() => {
    if (isOpen && modeloId) {
      // Ajustar nombre por defecto si es la primera vez
      if (nombreModelo && nombreModelo !== 'Diagrama') {
        const clean = nombreModelo.replace(/[^a-zA-Z0-9]/g, '');
        if (clean) {
          updateOptions({ projectName: clean + 'Backend' });
        }
      }
      fetchPreview(modeloId);
    }
  }, [isOpen, modeloId]);

  if (!isOpen) return null;

  const handleCopy = () => {
    if (selectedFile?.content) {
      navigator.clipboard.writeText(selectedFile.content);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  // Agrupar archivos por categoría para navegación limpia
  const categorizedFiles: Record<string, GeneratedFileDTO[]> = {
    'Entidades JPA': preview?.files.filter((f) => f.category === 'ENTITY') || [],
    'Repositorios Data JPA': preview?.files.filter((f) => f.category === 'REPOSITORY') || [],
    'Servicios de Negocio': preview?.files.filter((f) => f.category === 'SERVICE') || [],
    'Controladores REST': preview?.files.filter((f) => f.category === 'CONTROLLER') || [],
    'Data Transfer Objects': preview?.files.filter((f) => f.category === 'DTO') || [],
    'Configuración & Build': preview?.files.filter((f) => ['CONFIG', 'BUILD', 'DOCKER', 'DOCS'].includes(f.category)) || [],
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.75)',
        backdropFilter: 'blur(4px)',
        zIndex: 9999,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '20px',
      }}
      onClick={onClose}
    >
      <div
        style={{
          backgroundColor: '#0b132b',
          border: '1px solid #334155',
          borderRadius: '12px',
          width: '100%',
          maxWidth: '1100px',
          height: '88vh',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
          overflow: 'hidden',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Cabecera del Modal */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '14px 20px',
            borderBottom: '1px solid #334155',
            backgroundColor: '#0f172a',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div
              style={{
                width: 32,
                height: 32,
                borderRadius: '8px',
                backgroundColor: 'rgba(16, 185, 129, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#10b981',
              }}
            >
              <Code2 size={18} />
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: '#f8fafc' }}>
                Generador de Backend Spring Boot
              </h3>
              <p style={{ margin: 0, fontSize: '12px', color: '#94a3b8' }}>
                Arquitectura por capas, Spring Data JPA y PostgreSQL (Fase 9)
              </p>
            </div>
          </div>

          <button
            type="button"
            className="uml-del-btn"
            onClick={onClose}
            style={{ color: '#94a3b8', padding: '6px' }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Barra de Configuración Rápida */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '12px',
            padding: '10px 20px',
            backgroundColor: '#1e293b',
            borderBottom: '1px solid #334155',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Settings size={14} style={{ color: '#38bdf8' }} />
            <span style={{ fontSize: '12px', color: '#94a3b8' }}>Paquete Base:</span>
            <input
              type="text"
              className="uml-field-input"
              value={options.packageName || ''}
              onChange={(e) => updateOptions({ packageName: e.target.value })}
              placeholder="com.empresa.app"
              style={{ fontSize: '12px', padding: '3px 8px', width: '180px' }}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '12px', color: '#94a3b8' }}>Proyecto:</span>
            <input
              type="text"
              className="uml-field-input"
              value={options.projectName || ''}
              onChange={(e) => updateOptions({ projectName: e.target.value })}
              placeholder="NombreProyecto"
              style={{ fontSize: '12px', padding: '3px 8px', width: '160px' }}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '12px', color: '#94a3b8' }}>Puerto:</span>
            <input
              type="number"
              className="uml-field-input"
              value={options.serverPort || 8081}
              onChange={(e) => updateOptions({ serverPort: parseInt(e.target.value, 10) || 8081 })}
              style={{ fontSize: '12px', padding: '3px 8px', width: '70px' }}
            />
          </div>

          <button
            type="button"
            className="uml-toolbar-btn"
            onClick={() => fetchPreview(modeloId)}
            disabled={isLoading}
            style={{ fontSize: '12px', display: 'flex', alignItems: 'center', gap: '6px', marginLeft: 'auto' }}
          >
            <RefreshCw size={13} style={{ animation: isLoading ? 'spin 1.5s linear infinite' : undefined }} />
            Actualizar Vista Previa
          </button>
        </div>

        {/* Notificaciones de Error o Éxito */}
        {error && (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              backgroundColor: 'rgba(239, 68, 68, 0.15)',
              borderBottom: '1px solid #ef4444',
              padding: '8px 20px',
              color: '#fca5a5',
              fontSize: '12px',
            }}
          >
            <AlertCircle size={15} />
            <span>{error}</span>
          </div>
        )}

        {successMessage && (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              backgroundColor: 'rgba(16, 185, 129, 0.15)',
              borderBottom: '1px solid #10b981',
              padding: '8px 20px',
              color: '#34d399',
              fontSize: '12px',
            }}
          >
            <CheckCircle2 size={15} />
            <span>{successMessage}</span>
          </div>
        )}

        {/* Contenedor Principal: Árbol de Archivos (Izq) + Visor de Código (Der) */}
        <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
          {/* Columna Izquierda: Árbol de archivos */}
          <div
            style={{
              width: '320px',
              backgroundColor: '#0f172a',
              borderRight: '1px solid #334155',
              overflowY: 'auto',
              padding: '12px',
              display: 'flex',
              flexDirection: 'column',
              gap: '12px',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', fontWeight: 600, color: '#f8fafc', paddingBottom: '6px', borderBottom: '1px solid #334155' }}>
              <FolderTree size={16} style={{ color: '#38bdf8' }} />
              Estructura del Proyecto ({preview?.totalFiles || 0} archivos)
            </div>

            {isLoading && !preview ? (
              <div style={{ padding: '20px', textAlign: 'center', color: '#94a3b8', fontSize: '13px' }}>
                <RefreshCw size={20} style={{ animation: 'spin 1.5s linear infinite', marginBottom: '8px' }} />
                <p style={{ margin: 0 }}>Analizando modelo UML...</p>
              </div>
            ) : (
              Object.entries(categorizedFiles).map(([categoryName, files]) => {
                if (files.length === 0) return null;

                return (
                  <div key={categoryName} style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <div
                      style={{
                        fontSize: '11px',
                        fontWeight: 700,
                        textTransform: 'uppercase',
                        color: '#64748b',
                        padding: '4px 6px',
                        letterSpacing: '0.5px',
                      }}
                    >
                      {categoryName} ({files.length})
                    </div>

                    {files.map((file) => {
                      const isSelected = selectedFile?.relativePath === file.relativePath;
                      const fileName = file.relativePath.split('/').pop() || file.relativePath;

                      return (
                        <div
                          key={file.relativePath}
                          onClick={() => setSelectedFile(file)}
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            padding: '6px 10px',
                            borderRadius: '6px',
                            fontSize: '12px',
                            cursor: 'pointer',
                            backgroundColor: isSelected ? 'rgba(56, 189, 248, 0.15)' : 'transparent',
                            color: isSelected ? '#38bdf8' : '#cbd5e1',
                            border: isSelected ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid transparent',
                            transition: 'all 0.15s ease',
                          }}
                        >
                          <FileCode size={14} style={{ color: isSelected ? '#38bdf8' : '#94a3b8', flexShrink: 0 }} />
                          <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={file.relativePath}>
                            {fileName}
                          </span>
                        </div>
                      );
                    })}
                  </div>
                );
              })
            )}
          </div>

          {/* Columna Derecha: Visor de Código Fuente */}
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', backgroundColor: '#020617', overflow: 'hidden' }}>
            {selectedFile ? (
              <>
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '8px 16px',
                    backgroundColor: '#0f172a',
                    borderBottom: '1px solid #334155',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: '#f8fafc', fontWeight: 500 }}>
                    <FileCode size={16} style={{ color: '#38bdf8' }} />
                    <code>{selectedFile.relativePath}</code>
                    <span
                      style={{
                        fontSize: '10px',
                        padding: '2px 6px',
                        borderRadius: '4px',
                        backgroundColor: '#1e293b',
                        color: '#94a3b8',
                      }}
                    >
                      {selectedFile.category}
                    </span>
                  </div>

                  <button
                    type="button"
                    className="uml-toolbar-btn"
                    onClick={handleCopy}
                    style={{ fontSize: '12px', display: 'flex', alignItems: 'center', gap: '6px', padding: '4px 8px' }}
                  >
                    {copied ? <Check size={14} style={{ color: '#10b981' }} /> : <Copy size={14} />}
                    {copied ? '¡Copiado!' : 'Copiar'}
                  </button>
                </div>

                <div
                  style={{
                    flex: 1,
                    overflow: 'auto',
                    padding: '16px',
                    fontFamily: 'Consolas, Monaco, "Courier New", monospace',
                    fontSize: '13px',
                    lineHeight: '1.6',
                    color: '#e2e8f0',
                    backgroundColor: '#020617',
                    whiteSpace: 'pre',
                  }}
                >
                  {selectedFile.content}
                </div>
              </>
            ) : (
              <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#64748b', fontSize: '14px' }}>
                Selecciona un archivo del árbol para inspeccionar su código fuente
              </div>
            )}
          </div>
        </div>

        {/* Pie del Modal con Resumen y Descarga */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '12px 20px',
            backgroundColor: '#0f172a',
            borderTop: '1px solid #334155',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '14px', fontSize: '12px', color: '#94a3b8' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Database size={14} style={{ color: '#38bdf8' }} />
              Entidades: <strong>{preview?.totalEntities || 0}</strong>
            </span>
            <span>&bull;</span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Layers size={14} style={{ color: '#10b981' }} />
              Archivos: <strong>{preview?.totalFiles || 0}</strong>
            </span>
            <span>&bull;</span>
            <span>Spring Boot 3.3.4 &bull; Java 21</span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <button
              type="button"
              className="uml-toolbar-btn"
              onClick={onClose}
              style={{ fontSize: '13px', padding: '7px 14px' }}
            >
              Cerrar
            </button>

            <button
              type="button"
              className="uml-toolbar-btn primary"
              onClick={() => downloadZip(modeloId)}
              disabled={isDownloading || !preview || preview.totalEntities === 0}
              style={{
                fontSize: '13px',
                padding: '7px 18px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                fontWeight: 600,
                backgroundColor: '#2563eb',
                color: '#ffffff',
                border: 'none',
                borderRadius: '6px',
                cursor: isDownloading ? 'not-allowed' : 'pointer',
              }}
            >
              <Download size={15} />
              {isDownloading ? 'Empaquetando...' : 'Descargar Proyecto (.ZIP)'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
