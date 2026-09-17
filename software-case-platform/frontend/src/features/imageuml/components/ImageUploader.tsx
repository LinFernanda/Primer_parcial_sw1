import React, { useRef, useState } from 'react';
import { UploadCloud, Image as ImageIcon, CheckCircle, AlertCircle, FileText } from 'lucide-react';

interface ImageUploaderProps {
  file: File | null;
  filePreviewUrl: string | null;
  onSelectFile: (file: File) => boolean;
  onProcess: () => void;
  isLoading: boolean;
  error: string | null;
}

export const ImageUploader: React.FC<ImageUploaderProps> = ({
  file,
  filePreviewUrl,
  onSelectFile,
  onProcess,
  isLoading,
  error,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDragging, setIsDragging] = useState(false);

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

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
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
          padding: '32px 20px',
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
                width: 56,
                height: 56,
                borderRadius: '50%',
                backgroundColor: 'rgba(16, 185, 129, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#10b981',
              }}
            >
              <CheckCircle size={28} />
            </div>
          ) : (
            <div
              style={{
                width: 56,
                height: 56,
                borderRadius: '50%',
                backgroundColor: 'rgba(59, 130, 246, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#38bdf8',
              }}
            >
              <UploadCloud size={28} />
            </div>
          )}

          <div>
            <p style={{ margin: '0 0 4px', fontSize: '15px', fontWeight: 600, color: '#f8fafc' }}>
              {file ? file.name : 'Arrastra una imagen de diagrama aquí o haz clic'}
            </p>
            <p style={{ margin: 0, fontSize: '13px', color: '#94a3b8' }}>
              Formatos aceptados: <strong>PNG, JPG, JPEG</strong> (Máx. 10 MB).
            </p>
            <p style={{ margin: '4px 0 0', fontSize: '12px', color: '#64748b' }}>
              Admite diagramas conceptuales de papel, pizarra, capturas o esquemas visuales.
            </p>
          </div>
        </div>
      </div>

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

      {/* Previsualización de la miniatura si hay archivo seleccionado */}
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
              width: 80,
              height: 80,
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
            <div style={{ fontSize: '12px', color: '#94a3b8' }}>
              Tamaño: {(file.size / (1024 * 1024)).toFixed(2)} MB &bull; Tipo: {file.type || 'image'}
            </div>
          </div>

          <button
            className="uml-toolbar-btn primary"
            onClick={onProcess}
            disabled={isLoading}
            style={{
              padding: '8px 16px',
              fontSize: '14px',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              backgroundColor: '#2563eb',
              color: '#ffffff',
              border: 'none',
              borderRadius: '6px',
              cursor: isLoading ? 'not-allowed' : 'pointer',
            }}
          >
            <ImageIcon size={16} />
            {isLoading ? 'Analizando...' : 'Analizar Diagrama'}
          </button>
        </div>
      )}
    </div>
  );
};
