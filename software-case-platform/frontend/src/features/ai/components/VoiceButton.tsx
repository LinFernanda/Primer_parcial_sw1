import React, { useState, useEffect, useRef } from 'react';
import { Mic, MicOff, Loader2 } from 'lucide-react';

interface VoiceButtonProps {
  onTranscript: (texto: string) => void;
  disabled?: boolean;
}

export const VoiceButton: React.FC<VoiceButtonProps> = ({ onTranscript, disabled = false }) => {
  const [isListening, setIsListening] = useState(false);
  const [isSupported, setIsSupported] = useState(true);
  const recognitionRef = useRef<any>(null);

  useEffect(() => {
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      setIsSupported(false);
      return;
    }

    try {
      const recognition = new SpeechRecognition();
      recognition.continuous = false;
      recognition.interimResults = false;
      recognition.lang = 'es-ES';

      recognition.onstart = () => {
        setIsListening(true);
      };

      recognition.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        if (transcript && transcript.trim()) {
          onTranscript(transcript.trim());
        }
      };

      recognition.onerror = (event: any) => {
        console.warn('Error en reconocimiento de voz:', event.error);
        setIsListening(false);
      };

      recognition.onend = () => {
        setIsListening(false);
      };

      recognitionRef.current = recognition;
    } catch (e) {
      setIsSupported(false);
    }

    return () => {
      if (recognitionRef.current) {
        recognitionRef.current.abort();
      }
    };
  }, [onTranscript]);

  const toggleListening = () => {
    if (!isSupported) {
      alert('El reconocimiento de voz Web Speech no es compatible con este navegador. Por favor use Google Chrome o ingrese el comando por texto.');
      return;
    }

    if (isListening) {
      recognitionRef.current?.stop();
      setIsListening(false);
    } else {
      try {
        recognitionRef.current?.start();
      } catch (err) {
        console.error('Error al iniciar reconocimiento de voz:', err);
      }
    }
  };

  return (
    <button
      type="button"
      className={`uml-toolbar-btn ${isListening ? 'danger' : ''}`}
      onClick={toggleListening}
      disabled={disabled}
      title={
        isListening
          ? 'Escuchando comando de voz... Haga clic para detener'
          : 'Presione para dictar comando de voz (ej. "Crear clase Cliente")'
      }
      style={{
        position: 'relative',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '6px 10px',
        backgroundColor: isListening ? '#dc2626' : undefined,
        borderColor: isListening ? '#ef4444' : undefined,
        color: isListening ? '#ffffff' : undefined,
        boxShadow: isListening ? '0 0 12px rgba(239, 68, 68, 0.6)' : undefined,
        transition: 'all 0.2s ease',
      }}
    >
      {isListening ? (
        <>
          <Loader2 size={16} className="animate-spin" style={{ marginRight: '4px' }} />
          <span style={{ fontSize: '12px', fontWeight: 600 }}>Escuchando...</span>
        </>
      ) : isSupported ? (
        <Mic size={16} style={{ color: '#38bdf8' }} />
      ) : (
        <MicOff size={16} style={{ color: '#64748b' }} />
      )}
    </button>
  );
};
