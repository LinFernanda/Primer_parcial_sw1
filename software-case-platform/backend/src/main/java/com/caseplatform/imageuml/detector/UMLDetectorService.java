package com.caseplatform.imageuml.detector;

import com.caseplatform.imageuml.dto.ImageUMLDetectedDTO;

import java.awt.image.BufferedImage;

/**
 * Servicio de detección y extracción de elementos UML a partir de imágenes
 * mediante modelos de visión multimodal e inteligencia computacional / OCR.
 */
public interface UMLDetectorService {

    /**
     * Procesa la imagen visual y detecta todas las clases, atributos, métodos,
     * relaciones y cardinalidades presentes en el diagrama.
     */
    ImageUMLDetectedDTO detectUMLFromImage(BufferedImage image, byte[] imageBytes, String filename);

    /**
     * Detecta y estructura el modelo UML a partir de una descripción o transcripción textual.
     */
    ImageUMLDetectedDTO detectUMLFromText(String rawText);
}
