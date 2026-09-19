package com.caseplatform.imageuml.controller;

import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.imageuml.dto.ApplyImageUMLRequestDTO;
import com.caseplatform.imageuml.dto.ImageUploadResponseDTO;
import com.caseplatform.imageuml.service.ImageToUMLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST para la carga, preprocesamiento y conversión de imágenes
 * de diagramas conceptuales a modelos UML digitales editables.
 */
@Slf4j
@RestController
@RequestMapping("/api/imageuml")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ImageUploadController {

    private final ImageToUMLService imageToUMLService;
    private final com.caseplatform.imageuml.detector.UMLDetectorService umlDetectorService;

    /**
     * Endpoint oficial según especificación de la Fase 8:
     * POST /api/imageuml/upload
     * Recibe la imagen en formato multipart/form-data (y opcionalmente texto OCR previo) y retorna el resultado.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponseDTO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "modeloId", required = false) Long modeloId,
            @RequestParam(value = "ocrText", required = false) String ocrText
    ) {
        String usuarioEmail = getAuthenticatedUserEmail();
        log.info("Petición POST /api/imageuml/upload recibida para archivo: '{}', modeloId: {}, usuario: {}, ocrText presente: {}",
                file != null ? file.getOriginalFilename() : "null", modeloId, usuarioEmail, (ocrText != null && !ocrText.isBlank()));

        ImageUploadResponseDTO response = imageToUMLService.subirYProcesarImagen(file, modeloId, usuarioEmail, ocrText);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Parsea directamente una descripción o transcripción textual UML sin requerir subir archivo.
     * POST /api/imageuml/parse-text
     */
    @PostMapping("/parse-text")
    public ResponseEntity<com.caseplatform.imageuml.dto.ImageUMLDetectedDTO> parseText(
            @RequestBody java.util.Map<String, String> body
    ) {
        String text = body != null ? body.get("text") : "";
        com.caseplatform.imageuml.dto.ImageUMLDetectedDTO response = umlDetectorService.detectUMLFromText(text);
        return ResponseEntity.ok(response);
    }

    /**
     * Aplica el modelo UML detectado (con posibles modificaciones manuales de previsualización)
     * al lienzo del modelo UML especificado.
     * POST /api/imageuml/models/{modeloId}/apply
     */
    @PostMapping("/models/{modeloId}/apply")
    public ResponseEntity<ModeloUMLDTO> applyDetectedModel(
            @PathVariable Long modeloId,
            @RequestBody ApplyImageUMLRequestDTO request
    ) {
        String usuarioEmail = getAuthenticatedUserEmail();
        log.info("Petición POST /api/imageuml/models/{}/apply por usuario: {}", modeloId, usuarioEmail);

        ModeloUMLDTO modeloActualizado = imageToUMLService.aplicarModeloDetectado(modeloId, request, usuarioEmail);
        return ResponseEntity.ok(modeloActualizado);
    }

    /**
     * Conversión directa en un solo paso: sube, analiza y aplica al modelo UML.
     * POST /api/imageuml/models/{modeloId}/convert
     */
    @PostMapping(value = "/models/{modeloId}/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModeloUMLDTO> convertAndApplyDirectly(
            @PathVariable Long modeloId,
            @RequestParam("file") MultipartFile file
    ) {
        String usuarioEmail = getAuthenticatedUserEmail();
        log.info("Petición POST /api/imageuml/models/{}/convert directa por usuario: {}", modeloId, usuarioEmail);

        ImageUploadResponseDTO uploadResponse = imageToUMLService.subirYProcesarImagen(file, modeloId, usuarioEmail);
        ApplyImageUMLRequestDTO applyRequest = ApplyImageUMLRequestDTO.builder()
                .idImagen(uploadResponse.getIdImagen())
                .modeloAjustado(uploadResponse.getResultadoUML())
                .limpiarModeloExistente(false)
                .build();

        ModeloUMLDTO modeloActualizado = imageToUMLService.aplicarModeloDetectado(modeloId, applyRequest, usuarioEmail);
        return ResponseEntity.ok(modeloActualizado);
    }

    /**
     * Consulta el estado y resultado de una imagen procesada previamente.
     * GET /api/imageuml/{imagenId}
     */
    @GetMapping("/{imagenId}")
    public ResponseEntity<ImageUploadResponseDTO> getImageDetail(@PathVariable Long imagenId) {
        ImageUploadResponseDTO response = imageToUMLService.obtenerDetalleImagen(imagenId);
        return ResponseEntity.ok(response);
    }

    /**
     * Descarga el archivo binario de la imagen cargada para visualización lado a lado.
     * GET /api/imageuml/{imagenId}/file
     */
    @GetMapping("/{imagenId}/file")
    public ResponseEntity<byte[]> getImageFile(@PathVariable Long imagenId) {
        byte[] imageBytes = imageToUMLService.obtenerArchivoImagen(imagenId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(imageBytes);
    }

    private String getAuthenticatedUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
            return auth.getName();
        }
        return "ingeniero.uml@caseplatform.com";
    }
}
