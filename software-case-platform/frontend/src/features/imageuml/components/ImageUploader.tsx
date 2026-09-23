import React, { useRef, useState } from 'react';
import {
  UploadCloud,
  Image as ImageIcon,
  CheckCircle,
  AlertCircle,
  FileText,
  ScanText,
  Code2,
  Sparkles,
} from 'lucide-react';

interface ImageUploaderProps {
  file: File | null;
  filePreviewUrl: string | null;
  onSelectFile: (file: File) => boolean;
  onProcess: () => void;
  isLoading: boolean;
  error: string | null;
  ocrText?: string;
  setOcrText?: (text: string) => void;
  onScanOcr?: () => void;
  isOcrRunning?: boolean;
  onProcessText?: (text: string) => void;
}

export const ImageUploader: React.FC<ImageUploaderProps> = ({
  file,
  filePreviewUrl,
  onSelectFile,
  onProcess,
  isLoading,
  error,
  ocrText = '',
  setOcrText,
  onScanOcr,
  isOcrRunning = false,
  onProcessText,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [activeTab, setActiveTab] = useState<'image' | 'text'>('image');
  const [showOcrDrawer, setShowOcrDrawer] = useState(false);
  const [manualText, setManualText] = useState(ocrText);

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      onSelectFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      onSelectFile(e.target.files[0]);
    }
  };

  const handleLoadExample = (exampleType: 'ventas' | 'universidad' | 'herencia') => {
    let example = '';
    if (exampleType === 'ventas') {
      example = `class Cliente {
  - id: Long
  - nombre: String
  - email: String
  + registrar(): Boolean
}

class Pedido {
  - numero: String
  - fecha: Date
  - total: Double
  + calcularTotal(): Double
}

Cliente 1 -- * Pedido : realiza`;
    } else if (exampleType === 'universidad') {
      example = `class Estudiante {
  - matricula: String
  - nombre: String
  - semestre: Integer
}

class Curso {
  - codigo: String
  - titulo: String
  - creditos: Integer
}

Estudiante * -- 1..* Curso : cursa`;
    } else {
      example = `class Persona {
  # id: Long
  # nombre: String
  # documento: String
  + obtenerDatos(): String
}

class Empleado {
  - legajo: String
  - salario: Double
}

class Administrador {
  - areaResponsable: String
}

Persona <|-- Empleado
Empleado <|-- Administrador`;
    }

    setManualText(example);
    if (setOcrText) setOcrText(example);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {/* Selector de modo: Imagen vs Texto/Código UML */}
      <div
        style={{
          display: 'flex',
          gap: '8px',
          backgroundColor: '#0f172a',
          padding: '4px',
          borderRadius: '8px',
          border: '1px solid #334155',
        }}
      >
        <button
          type="button"
          onClick={() => setActiveTab('image')}
          style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            padding: '8px 14px',
            borderRadius: '6px',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer',
            transition: 'all 0.2s',
            border: 'none',
            backgroundColor: activeTab === 'image' ? '#2563eb' : 'transparent',
            color: activeTab === 'image' ? '#ffffff' : '#94a3b8',
          }}
        >
          <ImageIcon size={16} />
          Cargar Imagen del Diagrama
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('text')}
          style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            padding: '8px 14px',
            borderRadius: '6px',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer',
            transition: 'all 0.2s',
            border: 'none',
            backgroundColor: activeTab === 'text' ? '#2563eb' : 'transparent',
            color: activeTab === 'text' ? '#ffffff' : '#94a3b8',
          }}
        >
          <Code2 size={16} />
          Escribir / Pegar Texto UML
        </button>
      </div>

      {activeTab === 'image' ? (
        <>
          {/* Zona de Drag & Drop */}
          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            style={{
              border: isDragging
                ? '2px dashed #3b82f6'
                : file
                ? '2px solid #10b981'
                : '2px dashed #475569',
              borderRadius: '12px',
              padding: '28px 20px',
              textAlign: 'center',
              backgroundColor: isDragging
                ? 'rgba(59, 130, 246, 0.08)'
                : file
                ? 'rgba(16, 185, 129, 0.05)'
                : 'rgba(30, 41, 59, 0.5)',
              cursor: 'pointer',
              transition: 'all 0.2s ease',
            }}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".png,.jpg,.jpeg,image/png,image/jpeg"
              style={{ display: 'none' }}
              onChange={handleFileInputChange}
            />

            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '12px' }}>
              {file ? (
                <div
                  style={{
                    width: 52,
                    height: 52,
                    borderRadius: '50%',
                    backgroundColor: 'rgba(16, 185, 129, 0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#10b981',
                  }}
                >
                  <CheckCircle size={26} />
                </div>
              ) : (
                <div
                  style={{
                    width: 52,
                    height: 52,
                    borderRadius: '50%',
                    backgroundColor: 'rgba(59, 130, 246, 0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#38bdf8',
                  }}
                >
                  <UploadCloud size={26} />
                </div>
              )}

              <div>
                <p style={{ margin: '0 0 4px', fontSize: '15px', fontWeight: 600, color: '#f8fafc' }}>
                  {file ? file.name : 'Arrastra una imagen de diagrama aquí o haz clic'}
                </p>
                <p style={{ margin: 0, fontSize: '13px', color: '#94a3b8' }}>
                  Formatos aceptados: <strong>PNG, JPG, JPEG</strong> (Máx. 10 MB).
                </p>
                <p style={{ margin: '4px 0 0', fontSize: '12px', color: '#38bdf8' }}>
                  Resolución automática N:M: las relaciones muchos a muchos se descomponen en entidades asociativas intermedias con foreign keys.
                </p>
              </div>
            </div>
          </div>

          {/* Previsualización del archivo seleccionado */}
          {file && filePreviewUrl && (
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '16px',
                backgroundColor: '#1e293b',
                border: '1px solid #334155',
                borderRadius: '8px',
                padding: '12px',
              }}
            >
              <div
                style={{
                  width: 76,
                  height: 76,
                  borderRadius: '6px',
                  overflow: 'hidden',
                  backgroundColor: '#0f172a',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  border: '1px solid #475569',
                  flexShrink: 0,
                }}
              >
                <img
                  src={filePreviewUrl}
                  alt="Diagram preview"
                  style={{ width: '100%', height: '100%', objectFit: 'contain' }}
                />
              </div>

              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
                  <FileText size={14} style={{ color: '#38bdf8' }} />
                  <span style={{ fontSize: '14px', fontWeight: 600, color: '#f8fafc' }}>
                    {file.name}
                  </span>
                </div>
                <div style={{ fontSize: '12px', color: '#94a3b8', marginBottom: '6px' }}>
                  Tamaño: {(file.size / (1024 * 1024)).toFixed(2)} MB
                </div>

                {/* Botón para desplegar texto OCR opcional */}
                <button
                  type="button"
                  onClick={() => setShowOcrDrawer(!showOcrDrawer)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    backgroundColor: 'transparent',
                    border: 'none',
                    color: '#38bdf8',
                    fontSize: '12px',
                    fontWeight: 500,
                    cursor: 'pointer',
                    padding: 0,
                  }}
                >
                  <ScanText size={14} />
                  {showOcrDrawer ? 'Ocultar texto OCR del diagrama' : 'Ver / Ajustar texto OCR reconocido'}
                </button>
              </div>

              <button
                className="uml-toolbar-btn primary"
                onClick={onProcess}
                disabled={isLoading || isOcrRunning}
                style={{
                  padding: '10px 18px',
                  fontSize: '14px',
                  fontWeight: 600,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  backgroundColor: '#2563eb',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: isLoading || isOcrRunning ? 'not-allowed' : 'pointer',
                }}
              >
                <Sparkles size={16} />
                {isLoading || isOcrRunning ? 'Procesando...' : 'Analizar Diagrama'}
              </button>
            </div>
          )}

          {/* Desplegable opcional de texto OCR reconocido */}
          {file && showOcrDrawer && (
            <div
              style={{
                backgroundColor: '#0f172a',
                border: '1px solid #334155',
                borderRadius: '8px',
                padding: '12px',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '12px', fontWeight: 600, color: '#e2e8f0' }}>
                  Texto reconocido por OCR (editable antes de analizar):
                </span>
                {onScanOcr && (
                  <button
                    type="button"
                    onClick={onScanOcr}
                    disabled={isOcrRunning}
                    style={{
                      fontSize: '11px',
                      color: '#38bdf8',
                      background: 'none',
                      border: '1px solid #38bdf8',
                      borderRadius: '4px',
                      padding: '2px 8px',
                      cursor: 'pointer',
                    }}
                  >
                    {isOcrRunning ? 'Reconociendo...' : 'Re-escanear OCR ahora'}
                  </button>
                )}
              </div>
              <textarea
                value={ocrText}
                onChange={(e) => setOcrText && setOcrText(e.target.value)}
                placeholder="Si la imagen tiene escritura a mano o calidad baja, puedes revisar o pegar aquí las clases (ej. class Cliente { id: Long })..."
                rows={4}
                style={{
                  width: '100%',
                  backgroundColor: '#1e293b',
                  color: '#f8fafc',
                  border: '1px solid #475569',
                  borderRadius: '6px',
                  padding: '8px',
                  fontSize: '12px',
                  fontFamily: 'monospace',
                  resize: 'vertical',
                }}
              />
            </div>
          )}
        </>
      ) : (
        /* Pestaña de entrada textual / PlantUML / Mermaid */
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontSize: '13px', color: '#94a3b8' }}>
              Carga rápida de ejemplos conceptuales:
            </span>
            <div style={{ display: 'flex', gap: '6px' }}>
              <button
                type="button"
                onClick={() => handleLoadExample('ventas')}
                style={{
                  fontSize: '12px',
                  backgroundColor: '#1e293b',
                  border: '1px solid #475569',
                  color: '#cbd5e1',
                  borderRadius: '4px',
                  padding: '4px 8px',
                  cursor: 'pointer',
                }}
              >
                Ventas
              </button>
              <button
                type="button"
                onClick={() => handleLoadExample('universidad')}
                style={{
                  fontSize: '12px',
                  backgroundColor: '#1e293b',
                  border: '1px solid #475569',
                  color: '#cbd5e1',
                  borderRadius: '4px',
                  padding: '4px 8px',
                  cursor: 'pointer',
                }}
              >
                Universidad
              </button>
              <button
                type="button"
                onClick={() => handleLoadExample('herencia')}
                style={{
                  fontSize: '12px',
                  backgroundColor: '#1e293b',
                  border: '1px solid #475569',
                  color: '#cbd5e1',
                  borderRadius: '4px',
                  padding: '4px 8px',
                  cursor: 'pointer',
                }}
              >
                Herencia
              </button>
            </div>
          </div>

          <textarea
            value={manualText}
            onChange={(e) => {
              setManualText(e.target.value);
              if (setOcrText) setOcrText(e.target.value);
            }}
            placeholder={`Escribe aquí la estructura UML. Soporta formato estándar, PlantUML o Mermaid:\n\nclass Usuario {\n  - id: Long\n  - nombre: String\n  + login(): Boolean\n}\n\nclass Rol {\n  - id: Long\n  - nombre: String\n}\n\nUsuario *-- 1..* Rol : posee`}
            rows={9}
            style={{
              width: '100%',
              backgroundColor: '#0f172a',
              color: '#f8fafc',
              border: '1px solid #334155',
              borderRadius: '8px',
              padding: '12px',
              fontSize: '13px',
              fontFamily: 'monospace',
              lineHeight: 1.5,
              resize: 'vertical',
            }}
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => {
                if (onProcessText) {
                  onProcessText(manualText);
                } else {
                  onProcess();
                }
              }}
              disabled={isLoading || !manualText.trim()}
              style={{
                padding: '10px 20px',
                fontSize: '14px',
                fontWeight: 600,
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                backgroundColor: '#2563eb',
                color: '#ffffff',
                border: 'none',
                borderRadius: '6px',
                cursor: isLoading || !manualText.trim() ? 'not-allowed' : 'pointer',
              }}
            >
              <Sparkles size={16} />
              {isLoading ? 'Analizando...' : 'Analizar Estructura UML'}
            </button>
          </div>
        </div>
      )}

      {/* Mensaje de error de validación */}
      {error && (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            backgroundColor: 'rgba(239, 68, 68, 0.12)',
            border: '1px solid #ef4444',
            borderRadius: '8px',
            padding: '10px 14px',
            color: '#fca5a5',
            fontSize: '13px',
          }}
        >
          <AlertCircle size={16} style={{ flexShrink: 0 }} />
          <span>{error}</span>
        </div>
      )}
    </div>
  );
};
