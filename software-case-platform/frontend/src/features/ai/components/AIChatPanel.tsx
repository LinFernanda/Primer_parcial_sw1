import React, { useState, useEffect, useRef } from 'react';
import {
  Sparkles,
  Send,
  X,
  Bot,
  User,
  CheckCircle2,
  History,
  MessageSquare,
  Lightbulb,
} from 'lucide-react';
import {
  AICommandHistory,
  AICommandResponse,
  ChatMessage,
  ParsedAIAction,
} from '../models/ai.types';
import { aiService } from '../services/aiService';
import { VoiceButton } from './VoiceButton';
import { CommandHistory } from './CommandHistory';
import { ModeloUML } from '../../uml-editor/models/uml.types';

interface AIChatPanelProps {
  isOpen: boolean;
  onClose: () => void;
  modeloId: number;
  nombreModelo: string;
  onModelUpdated: (modeloActualizado: ModeloUML) => void;
}

export const AIChatPanel: React.FC<AIChatPanelProps> = ({
  isOpen,
  onClose,
  modeloId,
  nombreModelo,
  onModelUpdated,
}) => {
  const [activeTab, setActiveTab] = useState<'chat' | 'history'>('chat');
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      remitente: 'assistant',
      texto: `¡Hola! Soy tu asistente de modelado UML inteligente para **${nombreModelo}**. Puedes pedirme en lenguaje natural o dictarme por voz que cree clases, atributos, relaciones o modifique el diagrama.`,
      fecha: new Date().toISOString(),
      esExitoso: true,
    },
  ]);
  const [inputPrompt, setInputPrompt] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [historyList, setHistoryList] = useState<AICommandHistory[]>([]);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);
  const chatBottomRef = useRef<HTMLDivElement>(null);

  const sugerencias = [
    'Crear clase Cliente con atributo nombre String',
    'Agregar atributo email String a Cliente',
    'Relacionar Cliente con Venta con cardinalidad uno a muchos',
    'Cambiar el nombre de Cliente a Usuario',
  ];

  useEffect(() => {
    if (isOpen) {
      cargarHistorial();
    }
  }, [isOpen, modeloId]);

  useEffect(() => {
    chatBottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, activeTab]);

  const cargarHistorial = async () => {
    setIsLoadingHistory(true);
    try {
      const data = await aiService.obtenerHistorial(modeloId);
      setHistoryList(data);
    } catch (err) {
      console.warn('No se pudo cargar historial de comandos:', err);
    } finally {
      setIsLoadingHistory(false);
    }
  };

  const enviarComandoTexto = async (texto: string, confirmado = false, accionConfirmada?: ParsedAIAction) => {
    if (!texto.trim() || isProcessing) return;

    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      remitente: 'user',
      texto: texto.trim(),
      fecha: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputPrompt('');
    setIsProcessing(true);

    try {
      const res: AICommandResponse = await aiService.enviarComando(modeloId, {
        prompt: texto.trim(),
        confirmado,
        accionConfirmada,
      });

      const assistantMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        remitente: 'assistant',
        texto: res.mensaje || res.message || '',
        fecha: res.fecha || new Date().toISOString(),
        requiereConfirmacion: res.requiereConfirmacion ?? res.confirmationRequired,
        accionPendiente: (res.requiereConfirmacion ?? res.confirmationRequired) ? (res.accion || res.parsedAction) : undefined,
        accionEjecutada: !(res.requiereConfirmacion ?? res.confirmationRequired) ? (res.accion || res.parsedAction) : undefined,
        esExitoso: res.exitoso ?? res.success,
      };

      setMessages((prev) => [...prev, assistantMsg]);

      if (res.modeloActualizado && !res.requiereConfirmacion) {
        onModelUpdated(res.modeloActualizado);
      }

      await cargarHistorial();
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || err.message || 'Error al procesar la instrucción.';
      setMessages((prev) => [
        ...prev,
        {
          id: (Date.now() + 1).toString(),
          remitente: 'assistant',
          texto: `⚠️ ${errorMsg}`,
          fecha: new Date().toISOString(),
          esExitoso: false,
        },
      ]);
    } finally {
      setIsProcessing(false);
    }
  };

  const enviarComandoAudio = async (audioBase64: string) => {
    if (!audioBase64 || isProcessing) return;

    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      remitente: 'user',
      texto: '🎙️ [Comando de voz enviado - transcribiendo...]',
      fecha: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, userMsg]);
    setIsProcessing(true);

    try {
      const res: AICommandResponse = await aiService.enviarVoz(modeloId, '', audioBase64);

      if (res.mensaje && res.mensaje.includes('🎙️ Voz reconocida:')) {
        const lineas = res.mensaje.split('\n');
        const vozText = lineas[0].replace('🎙️ Voz reconocida: ', '').replace(/"/g, '');
        setMessages((prev) =>
          prev.map((m) => (m.id === userMsg.id ? { ...m, texto: `🎙️ "${vozText}"` } : m))
        );
      }

      const assistantMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        remitente: 'assistant',
        texto: res.mensaje || res.message || '',
        fecha: res.fecha || new Date().toISOString(),
        requiereConfirmacion: res.requiereConfirmacion ?? res.confirmationRequired,
        accionPendiente: (res.requiereConfirmacion ?? res.confirmationRequired) ? (res.accion || res.parsedAction) : undefined,
        accionEjecutada: !(res.requiereConfirmacion ?? res.confirmationRequired) ? (res.accion || res.parsedAction) : undefined,
        esExitoso: res.exitoso ?? res.success,
      };

      setMessages((prev) => [...prev, assistantMsg]);

      if (res.modeloActualizado && !res.requiereConfirmacion) {
        onModelUpdated(res.modeloActualizado);
      }

      await cargarHistorial();
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || err.message || 'Error al procesar el audio por voz.';
      setMessages((prev) => [
        ...prev,
        {
          id: (Date.now() + 1).toString(),
          remitente: 'assistant',
          texto: `⚠️ ${errorMsg}`,
          fecha: new Date().toISOString(),
          esExitoso: false,
        },
      ]);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleVoiceTranscript = (textoDictado: string) => {
    setInputPrompt(textoDictado);
    enviarComandoTexto(textoDictado);
  };

  const handleVoiceAudio = (audioBase64: string) => {
    enviarComandoAudio(audioBase64);
  };

  const handleConfirmAction = (accion: ParsedAIAction) => {
    enviarComandoTexto(`Confirmar: ${accion.explicacion || accion.nombreClase}`, true, accion);
  };

  if (!isOpen) return null;

  return (
    <div
      style={{
        position: 'fixed',
        top: '60px',
        right: '16px',
        bottom: '16px',
        width: '390px',
        backgroundColor: '#0f172a',
        border: '1px solid #334155',
        borderRadius: '12px',
        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.4)',
        display: 'flex',
        flexDirection: 'column',
        zIndex: 50,
        overflow: 'hidden',
      }}
    >
      {/* Header */}
      <div
        style={{
          padding: '12px 16px',
          borderBottom: '1px solid #334155',
          backgroundColor: '#1e293b',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div
            style={{
              width: '28px',
              height: '28px',
              borderRadius: '6px',
              backgroundColor: '#3b82f6',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#ffffff',
            }}
          >
            <Sparkles size={16} />
          </div>
          <div>
            <h4 style={{ margin: 0, fontSize: '14px', color: '#f8fafc', fontWeight: 600 }}>
              Asistente IA UML
            </h4>
            <span style={{ fontSize: '11px', color: '#94a3b8' }}>Edición inteligente por texto y voz</span>
          </div>
        </div>

        <button
          className="uml-modal-close"
          onClick={onClose}
          style={{ width: '26px', height: '26px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
        >
          <X size={16} />
        </button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', borderBottom: '1px solid #334155', backgroundColor: '#0f172a' }}>
        <button
          onClick={() => setActiveTab('chat')}
          style={{
            flex: 1,
            padding: '8px 12px',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'chat' ? '2px solid #3b82f6' : '2px solid transparent',
            color: activeTab === 'chat' ? '#60a5fa' : '#94a3b8',
            fontSize: '12px',
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '6px',
          }}
        >
          <MessageSquare size={13} /> Chat / Comandos
        </button>
        <button
          onClick={() => setActiveTab('history')}
          style={{
            flex: 1,
            padding: '8px 12px',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'history' ? '2px solid #3b82f6' : '2px solid transparent',
            color: activeTab === 'history' ? '#60a5fa' : '#94a3b8',
            fontSize: '12px',
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '6px',
          }}
        >
          <History size={13} /> Historial IA ({historyList.length})
        </button>
      </div>

      {/* Main Content Area */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '14px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {activeTab === 'chat' ? (
          <>
            {/* Mensajes del Chat */}
            {messages.map((m) => (
              <div
                key={m.id}
                style={{
                  display: 'flex',
                  gap: '8px',
                  alignSelf: m.remitente === 'user' ? 'flex-end' : 'flex-start',
                  maxWidth: '90%',
                }}
              >
                {m.remitente === 'assistant' && (
                  <div
                    style={{
                      width: '24px',
                      height: '24px',
                      borderRadius: '50%',
                      backgroundColor: '#2563eb',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: '#ffffff',
                      flexShrink: 0,
                      marginTop: '2px',
                    }}
                  >
                    <Bot size={13} />
                  </div>
                )}

                <div
                  style={{
                    backgroundColor: m.remitente === 'user' ? '#2563eb' : '#1e293b',
                    color: '#f8fafc',
                    padding: '8px 12px',
                    borderRadius: '10px',
                    borderTopRightRadius: m.remitente === 'user' ? '2px' : '10px',
                    borderTopLeftRadius: m.remitente === 'assistant' ? '2px' : '10px',
                    border: m.remitente === 'assistant' ? '1px solid #334155' : 'none',
                    fontSize: '12px',
                    lineHeight: '1.4',
                  }}
                >
                  <div>{m.texto}</div>

                  {/* Insignia de Acción Ejecutada */}
                  {m.accionEjecutada && (
                    <div
                      style={{
                        marginTop: '6px',
                        padding: '4px 6px',
                        backgroundColor: 'rgba(16, 185, 129, 0.15)',
                        border: '1px solid #10b981',
                        borderRadius: '4px',
                        color: '#34d399',
                        fontSize: '11px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                      }}
                    >
                      <CheckCircle2 size={12} />
                      <span>{m.accionEjecutada.tipoOperacion}</span>
                    </div>
                  )}

                  {/* Confirmación Requerida para comandos ambiguos */}
                  {m.requiereConfirmacion && m.accionPendiente && (
                    <div style={{ marginTop: '8px', display: 'flex', gap: '6px' }}>
                      <button
                        className="uml-toolbar-btn primary"
                        style={{ padding: '3px 8px', fontSize: '11px' }}
                        onClick={() => handleConfirmAction(m.accionPendiente!)}
                      >
                        Sí, crear clase
                      </button>
                      <button
                        className="uml-toolbar-btn"
                        style={{ padding: '3px 8px', fontSize: '11px' }}
                        onClick={() =>
                          setMessages((prev) => [
                            ...prev,
                            {
                              id: Date.now().toString(),
                              remitente: 'assistant',
                              texto: 'Acción cancelada.',
                              fecha: new Date().toISOString(),
                            },
                          ])
                        }
                      >
                        Cancelar
                      </button>
                    </div>
                  )}
                </div>

                {m.remitente === 'user' && (
                  <div
                    style={{
                      width: '24px',
                      height: '24px',
                      borderRadius: '50%',
                      backgroundColor: '#3b82f6',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: '#ffffff',
                      flexShrink: 0,
                      marginTop: '2px',
                    }}
                  >
                    <User size={13} />
                  </div>
                )}
              </div>
            ))}

            {isProcessing && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#94a3b8', fontSize: '12px' }}>
                <Bot size={14} className="animate-bounce" />
                <span>Interpretando instrucción y modificando modelo...</span>
              </div>
            )}

            <div ref={chatBottomRef} />
          </>
        ) : (
          <CommandHistory
            history={historyList}
            isLoading={isLoadingHistory}
            onSelectPrompt={(p) => {
              setInputPrompt(p);
              setActiveTab('chat');
            }}
          />
        )}
      </div>

      {/* Sugerencias Rápidas */}
      {activeTab === 'chat' && (
        <div style={{ padding: '6px 12px', borderTop: '1px solid #334155', backgroundColor: '#0f172a' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '10px', color: '#64748b', marginBottom: '4px' }}>
            <Lightbulb size={11} /> Sugerencias:
          </div>
          <div style={{ display: 'flex', gap: '4px', overflowX: 'auto', paddingBottom: '4px' }}>
            {sugerencias.map((s, i) => (
              <button
                key={i}
                type="button"
                onClick={() => setInputPrompt(s)}
                style={{
                  whiteSpace: 'nowrap',
                  fontSize: '10px',
                  padding: '2px 8px',
                  backgroundColor: '#1e293b',
                  border: '1px solid #334155',
                  borderRadius: '12px',
                  color: '#cbd5e1',
                  cursor: 'pointer',
                }}
              >
                {s}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Input Bar con Botón de Voz y Envío */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          enviarComandoTexto(inputPrompt);
        }}
        style={{
          padding: '10px 12px',
          borderTop: '1px solid #334155',
          backgroundColor: '#1e293b',
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
        }}
      >
        <VoiceButton
          onTranscript={handleVoiceTranscript}
          onAudioRecorded={handleVoiceAudio}
          disabled={isProcessing}
        />

        <input
          type="text"
          className="uml-input"
          placeholder="Escriba o dicte comando (ej. 'Crear clase Pedido')..."
          value={inputPrompt}
          onChange={(e) => setInputPrompt(e.target.value)}
          disabled={isProcessing}
          style={{ flex: 1, fontSize: '12px', padding: '6px 10px' }}
        />

        <button
          type="submit"
          className="uml-toolbar-btn primary"
          disabled={isProcessing || !inputPrompt.trim()}
          style={{ padding: '6px 10px' }}
          title="Enviar comando a la IA"
        >
          <Send size={14} />
        </button>
      </form>
    </div>
  );
};
