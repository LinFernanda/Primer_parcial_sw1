package com.caseplatform.imageuml.service;

import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.imageuml.dto.ApplyImageUMLRequestDTO;
import com.caseplatform.imageuml.dto.ImageUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio principal de orquestación de conversión de diagramas visuales a modelos UML.
 */
public interface ImageToUMLService {

    /**
     * Valida, procesa y detecta los elementos UML de una imagen cargada.
     */
    ImageUploadResponseDTO subirYProcesarImagen(MultipartFile archivo, Long modeloId, String usuarioEmail);

    /**
     * Aplica el modelo UML detectado o editado en previsualización directamente
     * a las entidades persistentes del ModeloUML, registrando versión y difundiendo por WebSocket.
     */
    ModeloUMLDTO aplicarModeloDetectado(Long modeloId, ApplyImageUMLRequestDTO request, String usuarioEmail);

    /**
     * Obtiene el detalle y resultado de detección de una imagen registrada.
     */
    ImageUploadResponseDTO obtenerDetalleImagen(Long imagenId);

    /**
     * Obtiene los bytes de la imagen almacenada.
     */
    byte[] obtenerArchivoImagen(Long imagenId);
}
