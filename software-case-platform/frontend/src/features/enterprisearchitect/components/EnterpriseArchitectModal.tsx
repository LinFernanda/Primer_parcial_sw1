import React, { useEffect, useRef, useState } from 'react';
import { useEnterpriseArchitect } from '../hooks/useEnterpriseArchitect';
import {
  X,
  Upload,
  Download,
  FileCode,
  CheckCircle,
  AlertCircle,
  Layers,
  Copy,
  Check,
  RefreshCw,
  FolderSync,
} from 'lucide-react';

interface EnterpriseArchitectModalProps {
  isOpen: boolean;
  onClose: () => void;
  modeloId: number;
  onModelImported?: () => void;
}

export const EnterpriseArchitectModal: React.FC<EnterpriseArchitectModalProps> = ({
  isOpen,
  onClose,
  modeloId,
  onModelImported,
}) => {
  const {
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
  } = useEnterpriseArchitect();

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (isOpen && activeTab === 'export') {
      loadExportPreview(modeloId);
    }
  }, [isOpen, activeTab, modeloId, loadExportPreview]);

  if (!isOpen) return null;

  const handleClose = () => {
    reset();
    onClose();
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleSelectFile(e.dataTransfer.files[0]);
    }
  };

  const handleImportClick = async () => {
    const res = await executeImport(modeloId);
    if (res && onModelImported) {
      setTimeout(() => {
        onModelImported();
      }, 1000);
    }
  };

  const handleCopyCode = () => {
    if (exportPreview?.contenidoXML) {
      navigator.clipboard.writeText(exportPreview.contenidoXML);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(15, 23, 42, 0.75)',
        backdropFilter: 'blur(4px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
        padding: '16px',
      }}
    >
      <div
        style={{
          backgroundColor: '#1e293b',
          borderRadius: '16px',
          border: '1px solid #334155',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
          width: '100%',
          maxWidth: '850px',
          maxHeight: '90vh',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          color: '#f8fafc',
        }}
      >
        {/* Cabecera del Modal */}
        <div
          style={{
            padding: '16px 24px',
            borderBottom: '1px solid #334155',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: '#0f172a',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div
              style={{
                width: '40px',
                height: '40px',
                borderRadius: '10px',
                backgroundColor: 'rgba(217, 119, 6, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                border: '1px solid rgba(217, 119, 6, 0.3)',
              }}
            >
              <FolderSync size={22} style={{ color: '#f59e0b' }} />
            </div>
            <div>
              <h2 style={{ fontSize: '18px', fontWeight: 600, margin: 0 }}>
                Interoperabilidad con Enterprise Architect
              </h2>
              <p style={{ fontSize: '12px', color: '#94a3b8', margin: '2px 0 0' }}>
                Importación y exportación de modelos UML mediante estándar XMI 2.1
              </p>
            </div>
          </div>
          <button
            onClick={handleClose}
            style={{
              background: 'none',
              border: 'none',
              color: '#94a3b8',
              cursor: 'pointer',
              padding: '6px',
              borderRadius: '8px',
            }}
          >
            <X size={20} />
          </button>
        </div>

        {/* Barra de Pestañas (Tabs) */}
        <div
          style={{
            display: 'flex',
            backgroundColor: '#0f172a',
            padding: '0 24px',
            borderBottom: '1px solid #334155',
            gap: '8px',
          }}
        >
          <button
            onClick={() => setActiveTab('import')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '12px 16px',
              fontSize: '13px',
              fontWeight: 600,
              backgroundColor: 'transparent',
              border: 'none',
              borderBottom: activeTab === 'import' ? '2px solid #f59e0b' : '2px solid transparent',
              color: activeTab === 'import' ? '#f59e0b' : '#94a3b8',
              cursor: 'pointer',
            }}
          >
            <Upload size={16} /> Importar desde Enterprise Architect (XMI)
          </button>
          <button
            onClick={() => setActiveTab('export')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '12px 16px',
              fontSize: '13px',
              fontWeight: 600,
              backgroundColor: 'transparent',
              border: 'none',
              borderBottom: activeTab === 'export' ? '2px solid #f59e0b' : '2px solid transparent',
              color: activeTab === 'export' ? '#f59e0b' : '#94a3b8',
              cursor: 'pointer',
            }}
          >
            <Download size={16} /> Exportar a Enterprise Architect (XMI 2.1)
          </button>
        </div>

        {/* Mensajes de Feedback Global */}
        {error && (
          <div
            style={{
              margin: '16px 24px 0',
              padding: '12px 16px',
              borderRadius: '8px',
              backgroundColor: 'rgba(239, 68, 68, 0.15)',
              border: '1px solid rgba(239, 68, 68, 0.3)',
              color: '#f87171',
              fontSize: '13px',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '10px',
            }}
          >
            <AlertCircle size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div
            style={{
              margin: '16px 24px 0',
              padding: '12px 16px',
              borderRadius: '8px',
              backgroundColor: 'rgba(16, 185, 129, 0.15)',
              border: '1px solid rgba(16, 185, 129, 0.3)',
              color: '#34d399',
              fontSize: '13px',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '10px',
            }}
          >
            <CheckCircle size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
            <span>{success}</span>
          </div>
        )}

        {/* Contenido Principal */}
        <div style={{ padding: '20px 24px', overflowY: 'auto', flex: 1 }}>
          {activeTab === 'import' ? (
            <div>
              {/* Zona Drag & Drop */}
              <div
                onDragOver={(e) => {
                  e.preventDefault();
                  setIsDragOver(true);
                }}
                onDragLeave={() => setIsDragOver(false)}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
                style={{
                  border: isDragOver ? '2px dashed #f59e0b' : '2px dashed #475569',
                  borderRadius: '12px',
                  padding: '32px 20px',
                  textAlign: 'center',
                  backgroundColor: isDragOver ? 'rgba(245, 158, 11, 0.05)' : '#0f172a',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                }}
              >
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".xml,.xmi"
                  style={{ display: 'none' }}
                  onChange={(e) => {
                    if (e.target.files && e.target.files[0]) {
                      handleSelectFile(e.target.files[0]);
                    }
                  }}
                />
                <FileCode size={40} style={{ color: '#f59e0b', margin: '0 auto 12px' }} />
                <h3 style={{ fontSize: '15px', fontWeight: 600, margin: '0 0 4px' }}>
                  {file ? file.name : 'Arrastra tu archivo XMI/XML aquí'}
                </h3>
                <p style={{ fontSize: '13px', color: '#94a3b8', margin: 0 }}>
                  {file
                    ? `${(file.size / 1024).toFixed(1)} KB — Clic para seleccionar otro archivo`
                    : 'Compatible con Enterprise Architect 6.5 a 16+ (XMI 2.1 / UML 2.1 - 2.5)'}
                </p>
              </div>

              {/* Vista Previa del Análisis */}
              {validation?.preview && (
                <div style={{ marginTop: '20px' }}>
                  <h4 style={{ fontSize: '14px', fontWeight: 600, color: '#cbd5e1', marginBottom: '12px' }}>
                    Resumen de Elementos Detectados en el XMI
                  </h4>
                  <div
                    style={{
                      display: 'grid',
                      gridTemplateColumns: 'repeat(4, 1fr)',
                      gap: '12px',
                      marginBottom: '16px',
                    }}
                  >
                    <div style={{ backgroundColor: '#0f172a', padding: '12px', borderRadius: '8px', border: '1px solid #334155' }}>
                      <span style={{ fontSize: '11px', color: '#94a3b8' }}>Clases</span>
                      <p style={{ fontSize: '20px', fontWeight: 700, color: '#38bdf8', margin: '4px 0 0' }}>
                        {validation.validacion.totalClases}
                      </p>
                    </div>
                    <div style={{ backgroundColor: '#0f172a', padding: '12px', borderRadius: '8px', border: '1px solid #334155' }}>
                      <span style={{ fontSize: '11px', color: '#94a3b8' }}>Atributos</span>
                      <p style={{ fontSize: '20px', fontWeight: 700, color: '#34d399', margin: '4px 0 0' }}>
                        {validation.validacion.totalAtributos}
                      </p>
                    </div>
                    <div style={{ backgroundColor: '#0f172a', padding: '12px', borderRadius: '8px', border: '1px solid #334155' }}>
                      <span style={{ fontSize: '11px', color: '#94a3b8' }}>Métodos</span>
                      <p style={{ fontSize: '20px', fontWeight: 700, color: '#c084fc', margin: '4px 0 0' }}>
                        {validation.validacion.totalMetodos}
                      </p>
                    </div>
                    <div style={{ backgroundColor: '#0f172a', padding: '12px', borderRadius: '8px', border: '1px solid #334155' }}>
                      <span style={{ fontSize: '11px', color: '#94a3b8' }}>Relaciones</span>
                      <p style={{ fontSize: '20px', fontWeight: 700, color: '#f59e0b', margin: '4px 0 0' }}>
                        {validation.validacion.totalRelaciones}
                      </p>
                    </div>
                  </div>

                  {/* Lista de Clases Detectadas */}
                  <div
                    style={{
                      maxHeight: '160px',
                      overflowY: 'auto',
                      backgroundColor: '#0f172a',
                      borderRadius: '8px',
                      padding: '8px 12px',
                      border: '1px solid #334155',
                    }}
                  >
                    {validation.preview.clases.map((c, idx) => (
                      <div
                        key={idx}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          padding: '6px 0',
                          borderBottom: idx < validation.preview.clases.length - 1 ? '1px solid #1e293b' : 'none',
                          fontSize: '12px',
                        }}
                      >
                        <span style={{ fontWeight: 600, color: '#f8fafc' }}>{c.nombre}</span>
                        <div style={{ display: 'flex', gap: '8px', color: '#94a3b8', fontSize: '11px' }}>
                          <span>{c.atributos?.length || 0} atributos</span>
                          <span>•</span>
                          <span>{c.metodos?.length || 0} métodos</span>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Opciones de Importación */}
                  <div style={{ marginTop: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <input
                      type="checkbox"
                      id="limpiarExistente"
                      checked={limpiarExistente}
                      onChange={(e) => setLimpiarExistente(e.target.checked)}
                      style={{ cursor: 'pointer', accentColor: '#f59e0b' }}
                    />
                    <label htmlFor="limpiarExistente" style={{ fontSize: '13px', color: '#cbd5e1', cursor: 'pointer' }}>
                      Reemplazar clases y relaciones existentes en el modelo actual
                    </label>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div>
              {/* Tab Exportar */}
              <div
                style={{
                  backgroundColor: '#0f172a',
                  borderRadius: '12px',
                  padding: '16px',
                  border: '1px solid #334155',
                  marginBottom: '16px',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <div>
                    <h4 style={{ fontSize: '14px', fontWeight: 600, margin: '0 0 4px' }}>
                      Modelo Listo para Exportación a Enterprise Architect
                    </h4>
                    <p style={{ fontSize: '12px', color: '#94a3b8', margin: 0 }}>
                      Genera un archivo XMI 2.1 estándar con metadatos Sparx Systems para preservar posiciones y diagramas.
                    </p>
                  </div>
                  <button
                    onClick={() => executeExport(modeloId, exportPreview?.nombreArchivo)}
                    disabled={isExporting}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px',
                      padding: '10px 18px',
                      borderRadius: '8px',
                      backgroundColor: '#f59e0b',
                      color: '#0f172a',
                      fontWeight: 700,
                      fontSize: '13px',
                      border: 'none',
                      cursor: isExporting ? 'not-allowed' : 'pointer',
                    }}
                  >
                    <Download size={16} /> Descargar XMI (.xml)
                  </button>
                </div>
              </div>

              {/* Visor de Código XML */}
              <div style={{ position: 'relative' }}>
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '8px 12px',
                    backgroundColor: '#0f172a',
                    borderTopLeftRadius: '8px',
                    borderTopRightRadius: '8px',
                    border: '1px solid #334155',
                    borderBottom: 'none',
                  }}
                >
                  <span style={{ fontSize: '12px', color: '#94a3b8' }}>
                    {exportPreview?.nombreArchivo || 'xmi_preview.xml'} ({exportPreview?.totalClases || 0} clases, {exportPreview?.totalRelaciones || 0} relaciones)
                  </span>
                  <button
                    onClick={handleCopyCode}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      backgroundColor: '#1e293b',
                      color: '#94a3b8',
                      border: '1px solid #475569',
                      borderRadius: '6px',
                      padding: '4px 10px',
                      fontSize: '11px',
                      cursor: 'pointer',
                    }}
                  >
                    {copied ? <Check size={12} style={{ color: '#34d399' }} /> : <Copy size={12} />}
                    {copied ? 'Copiado' : 'Copiar XML'}
                  </button>
                </div>
                <pre
                  style={{
                    margin: 0,
                    padding: '12px',
                    backgroundColor: '#020617',
                    borderBottomLeftRadius: '8px',
                    borderBottomRightRadius: '8px',
                    border: '1px solid #334155',
                    maxHeight: '260px',
                    overflowY: 'auto',
                    fontFamily: 'monospace',
                    fontSize: '12px',
                    color: '#93c5fd',
                  }}
                >
                  <code>{exportPreview?.contenidoXML || 'Cargando previsualización XML...'}</code>
                </pre>
              </div>
            </div>
          )}
        </div>

        {/* Pie del Modal */}
        <div
          style={{
            padding: '16px 24px',
            borderTop: '1px solid #334155',
            backgroundColor: '#0f172a',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            gap: '12px',
          }}
        >
          <button
            onClick={handleClose}
            style={{
              padding: '8px 16px',
              borderRadius: '8px',
              border: '1px solid #475569',
              backgroundColor: 'transparent',
              color: '#cbd5e1',
              fontSize: '13px',
              cursor: 'pointer',
            }}
          >
            Cerrar
          </button>

          {activeTab === 'import' && (
            <button
              onClick={handleImportClick}
              disabled={!validation?.valido || isLoading}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 18px',
                borderRadius: '8px',
                border: 'none',
                backgroundColor: validation?.valido && !isLoading ? '#f59e0b' : '#475569',
                color: validation?.valido && !isLoading ? '#0f172a' : '#94a3b8',
                fontWeight: 600,
                fontSize: '13px',
                cursor: validation?.valido && !isLoading ? 'pointer' : 'not-allowed',
              }}
            >
              {isLoading ? <RefreshCw size={16} className="animate-spin" /> : <Layers size={16} />}
              {isLoading ? 'Importando...' : 'Importar al Modelo UML'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
