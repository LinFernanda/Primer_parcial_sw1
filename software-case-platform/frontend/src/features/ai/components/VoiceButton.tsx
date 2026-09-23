import React, { useState, useEffect, useRef } from 'react';
import { Mic, Loader2 } from 'lucide-react';

interface VoiceButtonProps {
  onTranscript: (texto: string) => void;
  onAudioRecorded?: (audioBase64: string) => void;
  disabled?: boolean;
}

export const VoiceButton: React.FC<VoiceButtonProps> = ({
  onTranscript,
  onAudioRecorded,
  disabled = false,
}) => {
  const [isListening, setIsListening] = useState(false);
  const [interimText, setInterimText] = useState('');

  // Mantener referencias estables a los callbacks para evitar abortos involuntarios por re-renderizado
  const onTranscriptRef = useRef(onTranscript);
  const onAudioRecordedRef = useRef(onAudioRecorded);
  useEffect(() => {
    onTranscriptRef.current = onTranscript;
    onAudioRecordedRef.current = onAudioRecorded;
  }, [onTranscript, onAudioRecorded]);

  const recognitionRef = useRef<any>(null);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const streamRef = useRef<MediaStream | null>(null);
  const speechCapturedRef = useRef<boolean>(false);

  useEffect(() => {
    return () => {
      stopAll();
    };
  }, []);

  const stopAll = () => {
    try {
      if (recognitionRef.current) {
        recognitionRef.current.abort();
        recognitionRef.current = null;
      }
    } catch (_) {}

    try {
      if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
        mediaRecorderRef.current.stop();
      }
    } catch (_) {}

    try {
      if (streamRef.current) {
        streamRef.current.getTracks().forEach((track) => track.stop());
        streamRef.current = null;
      }
    } catch (_) {}

    setIsListening(false);
  };

  const startListening = async () => {
    setInterimText('');
    speechCapturedRef.current = false;
    audioChunksRef.current = [];

    // 1. Solicitar acceso al micrófono del navegador
    let stream: MediaStream;
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        throw new Error('Navegador no soporta captura de audio.');
      }
      stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      streamRef.current = stream;
    } catch (err: any) {
      console.error('Error al acceder al micrófono:', err);
      if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
        alert(
          'Permiso de micrófono denegado. Por favor haz clic en el ícono de permisos en la barra de direcciones de tu navegador y permite el acceso al micrófono.'
        );
      } else {
        alert('No se pudo acceder al micrófono: ' + (err.message || 'Error desconocido'));
      }
      return;
    }

    setIsListening(true);

    // 2. Iniciar MediaRecorder para capturar audio real (procesado con Groq Whisper como respaldo o principal)
    try {
      const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
        ? 'audio/webm;codecs=opus'
        : MediaRecorder.isTypeSupported('audio/webm')
        ? 'audio/webm'
        : MediaRecorder.isTypeSupported('audio/ogg;codecs=opus')
        ? 'audio/ogg;codecs=opus'
        : '';

      const recorder = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream);
      mediaRecorderRef.current = recorder;

      recorder.ondataavailable = (event) => {
        if (event.data && event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      recorder.onstop = () => {
        if (streamRef.current) {
          streamRef.current.getTracks().forEach((t) => t.stop());
          streamRef.current = null;
        }

        // Si Web Speech ya capturó el texto, no enviamos audio repetido
        if (speechCapturedRef.current) {
          return;
        }

        // Si Web Speech no produjo texto (o falló en red), enviamos el audio a Groq Whisper
        if (audioChunksRef.current.length > 0 && onAudioRecordedRef.current) {
          const audioBlob = new Blob(audioChunksRef.current, {
            type: recorder.mimeType || 'audio/webm',
          });
          if (audioBlob.size > 500) {
            const reader = new FileReader();
            reader.onloadend = () => {
              const base64Audio = reader.result as string;
              if (base64Audio) {
                onAudioRecordedRef.current?.(base64Audio);
              }
            };
            reader.readAsDataURL(audioBlob);
          }
        }
      };

      recorder.start(250);
    } catch (recorderErr) {
      console.warn('MediaRecorder no disponible:', recorderErr);
    }

    // 3. Web Speech API para transcripción visual instantánea
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (SpeechRecognition) {
      try {
        const recognition = new SpeechRecognition();
        recognition.continuous = true;
        recognition.interimResults = true;
        recognition.lang = navigator.language || 'es-ES';

        let finalAccumulated = '';

        recognition.onresult = (event: any) => {
          let interim = '';
          for (let i = event.resultIndex; i < event.results.length; i++) {
            const part = event.results[i][0].transcript;
            if (event.results[i].isFinal) {
              finalAccumulated += ' ' + part;
            } else {
              interim += part;
            }
          }

          const currentBest = (finalAccumulated + ' ' + interim).trim();
          setInterimText(currentBest);

          if (finalAccumulated.trim()) {
            speechCapturedRef.current = true;
          }
        };

        recognition.onerror = (event: any) => {
          console.warn('Web Speech API error/advertencia:', event.error);
        };

        recognition.onend = () => {
          if (finalAccumulated.trim()) {
            speechCapturedRef.current = true;
            onTranscriptRef.current(finalAccumulated.trim());
          }
        };

        recognitionRef.current = recognition;
        recognition.start();
      } catch (speechErr) {
        console.warn('SpeechRecognition error al iniciar:', speechErr);
      }
    }
  };

  const stopListening = () => {
    setIsListening(false);
    setInterimText('');

    if (recognitionRef.current) {
      try {
        recognitionRef.current.stop();
      } catch (_) {}
    }

    if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
      try {
        mediaRecorderRef.current.stop();
      } catch (_) {}
    } else {
      if (streamRef.current) {
        streamRef.current.getTracks().forEach((t) => t.stop());
        streamRef.current = null;
      }
    }
  };

  const toggleListening = () => {
    if (isListening) {
      stopListening();
    } else {
      startListening();
    }
  };

  return (
    <div style={{ position: 'relative', display: 'inline-flex', alignItems: 'center' }}>
      <button
        type="button"
        className={`uml-toolbar-btn ${isListening ? 'danger' : ''}`}
        onClick={toggleListening}
        disabled={disabled}
        title={
          isListening
            ? 'Detener grabación y enviar comando'
            : 'Presione para dictar comando de voz (ej. "Crear clase Cliente con nombre String")'
        }
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '6px 10px',
          backgroundColor: isListening ? '#dc2626' : undefined,
          borderColor: isListening ? '#ef4444' : undefined,
          color: isListening ? '#ffffff' : undefined,
          boxShadow: isListening ? '0 0 14px rgba(239, 68, 68, 0.7)' : undefined,
          transition: 'all 0.2s ease',
        }}
      >
        {isListening ? (
          <>
            <span
              style={{
                width: '8px',
                height: '8px',
                borderRadius: '50%',
                backgroundColor: '#ffffff',
                marginRight: '6px',
                display: 'inline-block',
              }}
            />
            <span style={{ fontSize: '12px', fontWeight: 600 }}>Detener</span>
          </>
        ) : (
          <Mic size={16} style={{ color: '#38bdf8' }} />
        )}
      </button>

      {/* Indicador flotante en vivo cuando está escuchando */}
      {isListening && (
        <div
          style={{
            position: 'absolute',
            bottom: '125%',
            left: '0',
            backgroundColor: '#1e293b',
            border: '1px solid #38bdf8',
            borderRadius: '6px',
            padding: '4px 10px',
            whiteSpace: 'nowrap',
            fontSize: '11px',
            color: '#f8fafc',
            boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
            zIndex: 100,
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
          }}
        >
          <Loader2 size={12} className="animate-spin" style={{ color: '#38bdf8' }} />
          <span>{interimText ? `"${interimText}"` : 'Habla ahora...'}</span>
        </div>
      )}
    </div>
  );
};
