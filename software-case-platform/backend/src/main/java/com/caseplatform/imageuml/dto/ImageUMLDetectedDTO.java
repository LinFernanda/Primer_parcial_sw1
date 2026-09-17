package com.caseplatform.imageuml.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenedor del modelo UML detectado a partir del procesamiento visual y OCR de la imagen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUMLDetectedDTO {

    @Builder.Default
    private List<ClaseDetectadaDTO> clases = new ArrayList<>();

    @Builder.Default
    private List<RelacionDetectadaDTO> relaciones = new ArrayList<>();

    @Builder.Default
    private Double nivelConfianza = 0.95;

    @Builder.Default
    private List<String> advertencias = new ArrayList<>();

    private String motorUtilizado; // "VISION_MULTIMODAL_AI", "COMPUTER_VISION_OCR_ENGINE"
    private Long tiempoProcesamientoMs;
}
