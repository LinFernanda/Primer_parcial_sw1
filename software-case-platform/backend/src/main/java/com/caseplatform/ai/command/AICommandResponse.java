package com.caseplatform.ai.command;

import com.caseplatform.dto.uml.ModeloUMLDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Respuesta del Agente IA al usuario tras interpretar y/o ejecutar la instrucción.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AICommandResponse {

    private String mensaje;
    private boolean exitoso;
    private boolean requiereConfirmacion;
    private String preguntaConfirmacion;
    private ParsedAIAction accion;
    private ModeloUMLDTO modeloActualizado;
    private LocalDateTime fecha;
}
