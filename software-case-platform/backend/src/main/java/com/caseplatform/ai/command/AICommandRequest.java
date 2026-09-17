package com.caseplatform.ai.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición con la instrucción en lenguaje natural o confirmación de una acción previa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AICommandRequest {

    @NotBlank(message = "La instrucción para el agente IA no puede estar vacía")
    private String prompt;

    @Builder.Default
    private Boolean confirmado = false;

    private ParsedAIAction accionConfirmada;
}
