import React from 'react';
import { useImageUML } from '../hooks/useImageUML';
import { ImageUploader } from './ImageUploader';
import { ProcessingStatus } from './ProcessingStatus';
import { ResultPreview } from './ResultPreview';
import { ModeloUML } from '../models/imageuml.types';
import { Camera, X } from 'lucide-react';

interface ImageUMLModalProps {
  isOpen: boolean;
  onClose: () => void;
  modeloId: number;
  onModelApplied: (modelo: ModeloUML) => void;
}

export const ImageUMLModal: React.FC<ImageUMLModalProps> = ({
  isOpen,
  onClose,
  modeloId,
  onModelApplied,
}) => {
  const {
    step,
    file,
    filePreviewUrl,
    previewUml,
    currentProcessingStep,
    isLoading,
    isOcrRunning,
    ocrText,
    setOcrText,
    runOcrOnBlob,
    processText,
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
  } = useImageUML();

  if (!isOpen) return null;

  const handleApply = async () => {
    try {
      const modeloActualizado = await applyToModel(modeloId);
      onModelApplied(modeloActualizado);
      reset();
      onClose();
    } catch (err) {
      console.error('Error al aplicar el modelo detectado:', err);
    }
  };

  const handleClose = () => {
    reset();
    onClose();
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
      onClick={handleClose}
    >
      <div
        style={{
          backgroundColor: '#0b132b',
          border: '1px solid #334155',
          borderRadius: '12px',
          width: '100%',
          maxWidth: '850px',
          maxHeight: '90vh',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
          overflow: 'hidden',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Encabezado del Modal */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '16px 20px',
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
                backgroundColor: 'rgba(56, 189, 248, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#38bdf8',
              }}
            >
              <Camera size={18} />
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: '#f8fafc' }}>
                Conversión de Imagen a Modelo UML
              </h3>
              <p style={{ margin: 0, fontSize: '12px', color: '#94a3b8' }}>
                Visión Artificial, Detección de Contornos y OCR
              </p>
            </div>
          </div>

          <button
            type="button"
            className="uml-del-btn"
            onClick={handleClose}
            style={{ color: '#94a3b8', padding: '6px' }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Cuerpo del Modal con Scroll */}
        <div style={{ padding: '20px', overflowY: 'auto', flex: 1 }}>
          {step === 'upload' && (
            <ImageUploader
              file={file}
              filePreviewUrl={filePreviewUrl}
              onSelectFile={handleSelectFile}
              onProcess={() => processImage(modeloId)}
              isLoading={isLoading}
              error={error}
              ocrText={ocrText}
              setOcrText={setOcrText}
              onScanOcr={() => file && runOcrOnBlob(file)}
              isOcrRunning={isOcrRunning}
              onProcessText={(text) => processText(text)}
            />
          )}

          {step === 'processing' && (
            <ProcessingStatus currentStepMessage={currentProcessingStep} />
          )}

          {step === 'preview' && previewUml && (
            <ResultPreview
              previewUml={previewUml}
              filePreviewUrl={filePreviewUrl}
              limpiarModeloExistente={limpiarModeloExistente}
              setLimpiarModeloExistente={setLimpiarModeloExistente}
              onUpdateClass={updateDetectedClass}
              onDeleteClass={deleteDetectedClass}
              onAddClass={addDetectedClass}
              onUpdateAttribute={updateDetectedAttribute}
              onDeleteAttribute={deleteDetectedAttribute}
              onAddAttribute={addDetectedAttribute}
              onUpdateMethod={updateDetectedMethod}
              onDeleteMethod={deleteDetectedMethod}
              onAddMethod={addDetectedMethod}
              onUpdateRelation={updateDetectedRelation}
              onDeleteRelation={deleteDetectedRelation}
              onAddRelation={addDetectedRelation}
              onApply={handleApply}
              onCancel={reset}
              isLoading={isLoading}
            />
          )}
        </div>
      </div>
    </div>
  );
};
