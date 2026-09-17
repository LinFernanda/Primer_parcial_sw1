package com.caseplatform.imageuml.processor;

import java.awt.image.BufferedImage;

/**
 * Servicio de procesamiento visual computacional de imágenes de diagramas UML.
 * Implementa filtros de reducción de ruido, mejora de contraste, escala de grises,
 * binarización y convolución de bordes (Sobel).
 */
public interface ImageProcessorService {

    /**
     * Ejecuta el pipeline completo de preprocesamiento visual optimizado para diagramas UML y OCR.
     */
    BufferedImage preprocess(BufferedImage originalImage);

    /**
     * Redimensiona la imagen preservando la relación de aspecto si excede las dimensiones máximas.
     */
    BufferedImage resize(BufferedImage img, int maxWidth, int maxHeight);

    /**
     * Convierte la imagen a escala de grises.
     */
    BufferedImage toGrayscale(BufferedImage img);

    /**
     * Aplica filtro de eliminación de ruido (suavizado gaussiano 3x3).
     */
    BufferedImage reduceNoise(BufferedImage img);

    /**
     * Aplica mejora de contraste mediante expansión lineal de histograma.
     */
    BufferedImage enhanceContrast(BufferedImage img);

    /**
     * Aplica detector de bordes con operador de Sobel (gradiente horizontal y vertical).
     */
    BufferedImage detectEdgesSobel(BufferedImage img);

    /**
     * Binariza la imagen usando umbralización adaptativa/Otsu.
     */
    BufferedImage binarizeOtsu(BufferedImage img);

    /**
     * Convierte un arreglo de bytes en BufferedImage.
     */
    BufferedImage fromByteArray(byte[] imageBytes);

    /**
     * Convierte un BufferedImage a arreglo de bytes en formato PNG o JPG.
     */
    byte[] toByteArray(BufferedImage img, String format);
}
