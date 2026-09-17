package com.caseplatform.imageuml;

import com.caseplatform.imageuml.processor.ImageProcessorService;
import com.caseplatform.imageuml.processor.ImageProcessorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - ImageProcessorService (Visión Artificial)")
class ImageProcessorServiceTest {

    private ImageProcessorService imageProcessorService;

    @BeforeEach
    void setUp() {
        imageProcessorService = new ImageProcessorServiceImpl();
    }

    private BufferedImage createSyntheticUMLDiagramImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        try {
            // Fondo blanco
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // Dibujar caja de clase UML (rectángulo negro)
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(50, 50, 200, 150);

            // Línea separadora de atributos
            g2d.drawLine(50, 80, 250, 80);

            // Línea de relación hacia otra caja
            g2d.drawLine(250, 120, 350, 120);

            // Segunda caja
            g2d.drawRect(350, 50, 200, 150);
        } finally {
            g2d.dispose();
        }
        return img;
    }

    @Test
    @DisplayName("Debe redimensionar la imagen si excede el tamaño máximo permitido preservando proporciones")
    void testResize_RedimensionaSiExcedeMaximo() {
        BufferedImage large = createSyntheticUMLDiagramImage(3000, 2000);
        BufferedImage resized = imageProcessorService.resize(large, 1920, 1200);

        assertNotNull(resized);
        assertTrue(resized.getWidth() <= 1920);
        assertTrue(resized.getHeight() <= 1200);
    }

    @Test
    @DisplayName("Debe convertir la imagen a escala de grises correctamente")
    void testToGrayscale_ConvierteCorrectamente() {
        BufferedImage colorImg = createSyntheticUMLDiagramImage(400, 300);
        BufferedImage grayImg = imageProcessorService.toGrayscale(colorImg);

        assertNotNull(grayImg);
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, grayImg.getType());
        assertEquals(400, grayImg.getWidth());
        assertEquals(300, grayImg.getHeight());
    }

    @Test
    @DisplayName("Debe aplicar reducción de ruido mediante suavizado gaussiano")
    void testReduceNoise_AplicaSuavizado() {
        BufferedImage img = createSyntheticUMLDiagramImage(400, 300);
        BufferedImage smoothed = imageProcessorService.reduceNoise(img);

        assertNotNull(smoothed);
        assertEquals(400, smoothed.getWidth());
        assertEquals(300, smoothed.getHeight());
    }

    @Test
    @DisplayName("Debe mejorar el contraste dinámico mediante estiramiento lineal")
    void testEnhanceContrast_AplicaMejora() {
        BufferedImage img = createSyntheticUMLDiagramImage(400, 300);
        BufferedImage enhanced = imageProcessorService.enhanceContrast(img);

        assertNotNull(enhanced);
        assertEquals(400, enhanced.getWidth());
        assertEquals(300, enhanced.getHeight());
    }

    @Test
    @DisplayName("Debe detectar bordes de cajas y líneas UML con operador Sobel")
    void testDetectEdgesSobel_DetectaBordes() {
        BufferedImage img = createSyntheticUMLDiagramImage(400, 300);
        BufferedImage edges = imageProcessorService.detectEdgesSobel(img);

        assertNotNull(edges);
        assertEquals(400, edges.getWidth());
        assertEquals(300, edges.getHeight());
    }

    @Test
    @DisplayName("Debe binarizar la imagen mediante umbralización Otsu")
    void testBinarizeOtsu_BinarizaCorrectamente() {
        BufferedImage img = createSyntheticUMLDiagramImage(400, 300);
        BufferedImage bin = imageProcessorService.binarizeOtsu(img);

        assertNotNull(bin);
        assertEquals(BufferedImage.TYPE_BYTE_BINARY, bin.getType());
    }

    @Test
    @DisplayName("Debe convertir correctamente entre BufferedImage y arreglo de bytes")
    void testByteConversion_Idempotente() {
        BufferedImage original = createSyntheticUMLDiagramImage(200, 150);
        byte[] bytes = imageProcessorService.toByteArray(original, "png");

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        BufferedImage recovered = imageProcessorService.fromByteArray(bytes);
        assertNotNull(recovered);
        assertEquals(200, recovered.getWidth());
        assertEquals(150, recovered.getHeight());
    }
}
