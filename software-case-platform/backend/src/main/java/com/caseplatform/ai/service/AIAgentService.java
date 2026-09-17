package com.caseplatform.ai.service;

import com.caseplatform.ai.command.AICommandRequest;
import com.caseplatform.ai.command.AICommandResponse;
import com.caseplatform.ai.command.VoiceCommandRequest;
import com.caseplatform.ai.model.AICommandHistory;

import java.util.List;

/**
 * Servicio central del Agente de Inteligencia Artificial para la interpretación
 * y ejecución inteligente de modificaciones sobre diagramas conceptuales UML.
 */
public interface AIAgentService {

    /**
     * Procesa una instrucción en lenguaje natural, valida la intención y ejecuta la acción UML correspondiente.
     */
    AICommandResponse procesarComando(Long modeloId, AICommandRequest request, String usuarioEmail);

    /**
     * Procesa un comando recibido por voz o transcrito a texto para su ejecución sobre el modelo.
     */
    AICommandResponse procesarVoz(Long modeloId, VoiceCommandRequest request, String usuarioEmail);

    /**
     * Obtiene el historial de comandos y auditoría de acciones ejecutadas por la IA para un modelo.
     */
    List<AICommandHistory> obtenerHistorialComandos(Long modeloId);
}
