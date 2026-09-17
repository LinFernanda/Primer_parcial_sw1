package com.caseplatform.imageuml.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implementación del servicio de procesamiento visual de imágenes de diagramas UML.
 * Utiliza algoritmos de visión artificial en Java 2D: Convolución Gaussiana, Sobel,
 * Umbralización Otsu y Expansión Dinámica de Contraste.
 */
@Slf4j
@Service
public class ImageProcessorServiceImpl implements ImageProcessorService {

    private static final int DEFAULT_MAX_WIDTH = 1920;
    private static final int DEFAULT_MAX_HEIGHT = 1200;

    @Override
    public BufferedImage preprocess(BufferedImage originalImage) {
        if (originalImage == null) {
            throw new IllegalArgumentException("La imagen original no puede ser nula");
        }

        log.debug("Iniciando preprocesamiento visual de imagen ({}x{})",
                originalImage.getWidth(), originalImage.getHeight());

        // 1. Redimensionar si excede límites máximos preservando proporción
        BufferedImage resized = resize(originalImage, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT);

        // 2. Convertir a escala de grises
        BufferedImage gray = toGrayscale(resized);

        // 3. Eliminación de ruido mediante filtro gaussiano
        BufferedImage smoothed = reduceNoise(gray);

        // 4. Mejora de contraste por ecualización lineal
        BufferedImage enhanced = enhanceContrast(smoothed);

        // 5. Binarización de alto contraste para claridad de cajas UML y texto
        BufferedImage binarized = binarizeOtsu(enhanced);

        log.debug("Preprocesamiento visual finalizado con éxito ({}x{})",
                binarized.getWidth(), binarized.getHeight());

        return binarized;
    }

    @Override
    public BufferedImage resize(BufferedImage img, int maxWidth, int maxHeight) {
        int width = img.getWidth();
        int height = img.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return img;
        }

        double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int targetWidth = Math.max(1, (int) (width * ratio));
        int targetHeight = Math.max(1, (int) (height * ratio));

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g2d.dispose();
        }

        return resized;
    }

    @Override
    public BufferedImage toGrayscale(BufferedImage img) {
        BufferedImage gray = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = gray.createGraphics();
        try {
            g2d.drawImage(img, 0, 0, null);
        } finally {
            g2d.dispose();
        }
        return gray;
    }

    @Override
    public BufferedImage reduceNoise(BufferedImage img) {
        // Filtro Gaussiano 3x3 normalizado para reducción de ruido de captura
        float[] gaussianKernel = {
                1f / 16f, 2f / 16f, 1f / 16f,
                2f / 16f, 4f / 16f, 2f / 16f,
                1f / 16f, 2f / 16f, 1f / 16f
        };

        Kernel kernel = new Kernel(3, 3, gaussianKernel);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage output = new BufferedImage(img.getWidth(), img.getHeight(), img.getType() == 0 ? BufferedImage.TYPE_BYTE_GRAY : img.getType());
        return op.filter(img, output);
    }

    @Override
    public BufferedImage enhanceContrast(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int minVal = 255;
        int maxVal = 0;

        // Primer pase: identificar rango dinámico min y max
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int gray = rgb & 0xFF;
                if (gray < minVal) minVal = gray;
                if (gray > maxVal) maxVal = gray;
            }
        }

        if (maxVal <= minVal) {
            return img; // Imagen completamente uniforme
        }

        // Segundo pase: estiramiento de contraste lineal
        BufferedImage enhanced = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        double scale = 255.0 / (maxVal - minVal);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int gray = rgb & 0xFF;
                int newGray = Math.min(255, Math.max(0, (int) ((gray - minVal) * scale)));
                int newRgb = (newGray << 16) | (newGray << 8) | newGray;
                enhanced.setRGB(x, y, newRgb);
            }
        }

        return enhanced;
    }

    @Override
    public BufferedImage detectEdgesSobel(BufferedImage img) {
        BufferedImage gray = (img.getType() == BufferedImage.TYPE_BYTE_GRAY) ? img : toGrayscale(img);
        int width = gray.getWidth();
        int height = gray.getHeight();

        BufferedImage edges = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int[][] gx = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };

        int[][] gy = {
                {-1, -2, -1},
                { 0,  0,  0},
                { 1,  2,  1}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int pixelX = 0;
                int pixelY = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int val = gray.getRGB(x + kx, y + ky) & 0xFF;
                        pixelX += gx[ky + 1][kx + 1] * val;
                        pixelY += gy[ky + 1][kx + 1] * val;
                    }
                }

                int magnitude = (int) Math.sqrt(pixelX * pixelX + pixelY * pixelY);
                magnitude = Math.min(255, Math.max(0, magnitude));

                int rgb = (magnitude << 16) | (magnitude << 8) | magnitude;
                edges.setRGB(x, y, rgb);
            }
        }

        return edges;
    }

    @Override
    public BufferedImage binarizeOtsu(BufferedImage img) {
        BufferedImage gray = (img.getType() == BufferedImage.TYPE_BYTE_GRAY) ? img : toGrayscale(img);
        int width = gray.getWidth();
        int height = gray.getHeight();
        int totalPixels = width * height;

        // Histograma de 256 niveles de gris
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = gray.getRGB(x, y) & 0xFF;
                histogram[val]++;
            }
        }

        // Algoritmo de Otsu para encontrar el umbral óptimo
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        float sumB = 0;
        int wB = 0;
        int wF = 0;
        float varMax = 0;
        int threshold = 128;

        for (int t = 0; t < 256; t++) {
            wB += histogram[t];
            if (wB == 0) continue;

            wF = totalPixels - wB;
            if (wF == 0) break;

            sumB += (float) (t * histogram[t]);

            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        // Generar imagen binaria (blanco y negro puro)
        BufferedImage binarized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = gray.getRGB(x, y) & 0xFF;
                int binaryVal = (val >= threshold) ? 0xFFFFFF : 0x000000;
                binarized.setRGB(x, y, binaryVal);
            }
        }

        return binarized;
    }

    @Override
    public BufferedImage fromByteArray(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Los bytes de la imagen están vacíos");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new IllegalArgumentException("No se pudo decodificar el formato de la imagen");
            }
            return image;
        } catch (IOException e) {
            throw new RuntimeException("Error al leer imagen desde arreglo de bytes: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] toByteArray(BufferedImage img, String format) {
        if (img == null) {
            throw new IllegalArgumentException("La imagen no puede ser nula");
        }
        String fmt = (format != null && !format.isBlank()) ? format.toLowerCase().replace("image/", "") : "png";
        if (!fmt.equals("jpg") && !fmt.equals("jpeg") && !fmt.equals("png")) {
            fmt = "png";
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, fmt, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al serializar imagen a bytes: " + e.getMessage(), e);
        }
    }
}
