package com.caseplatform.ai.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para entrada por voz transcribible o transcript directo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCommandRequest {

    private String audioBase64;
    private String textoTranscrito;
}
