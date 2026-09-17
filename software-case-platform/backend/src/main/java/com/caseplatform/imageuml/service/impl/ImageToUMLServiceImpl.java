package com.caseplatform.imageuml.service.impl;

import com.caseplatform.dto.uml.ModeloUMLDTO;
import com.caseplatform.exception.ResourceNotFoundException;
import com.caseplatform.exception.ValidationException;
import com.caseplatform.imageuml.detector.UMLDetectorService;
import com.caseplatform.imageuml.dto.*;
import com.caseplatform.imageuml.model.EstadoProcesamientoImagen;
import com.caseplatform.imageuml.model.ImagenUML;
import com.caseplatform.imageuml.processor.ImageProcessorService;
import com.caseplatform.imageuml.repository.ImagenUMLRepository;
import com.caseplatform.imageuml.service.ImageToUMLService;
import com.caseplatform.model.*;
import com.caseplatform.repository.*;
import com.caseplatform.service.ModeloUMLService;
import com.caseplatform.versioning.dto.CreateVersionDTO;
import com.caseplatform.versioning.model.TipoOperacionHistorial;
import com.caseplatform.versioning.service.VersionService;
import com.caseplatform.websocket.model.TipoElementoUML;
import com.caseplatform.websocket.model.TipoOperacionUML;
import com.caseplatform.websocket.model.UMLEvent;
import com.caseplatform.websocket.service.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación del servicio de conversión de diagramas visuales en modelos UML editables.
 * Integra almacenamiento temporal, visión artificial, estructuración conceptual,
 * persistencia JPA, versionado histórico y sincronización WebSocket en tiempo real.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageToUMLServiceImpl implements ImageToUMLService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/jpg");

    private final ImageProcessorService imageProcessorService;
    private final UMLDetectorService umlDetectorService;
    private final ImagenUMLRepository imagenUMLRepository;
    private final ModeloUMLRepository modeloUMLRepository;
    private final ClaseUMLRepository claseUMLRepository;
    private final AtributoUMLRepository atributoUMLRepository;
    private final RelacionUMLRepository relacionUMLRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModeloUMLService modeloUMLService;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private VersionService versionService;

    @Autowired(required = false)
    private EventPublisher eventPublisher;

    @Override
    @Transactional
    public ImageUploadResponseDTO subirYProcesarImagen(MultipartFile archivo, Long modeloId, String usuarioEmail) {
        log.info("Recibiendo imagen para procesamiento UML: '{}', tamaño: {} bytes, usuario: {}",
                archivo != null ? archivo.getOriginalFilename() : "null",
                archivo != null ? archivo.getSize() : 0,
                usuarioEmail);

        validarArchivo(archivo);

        String emailFinal = (usuarioEmail != null && !usuarioEmail.isBlank()) ? usuarioEmail : "usuario.anonimo@caseplatform.com";
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailFinal).orElse(null);
        ModeloUML modelo = (modeloId != null) ? modeloUMLRepository.findById(modeloId).orElse(null) : null;

        try {
            byte[] rawBytes = archivo.getBytes();
            String originalFilename = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "diagrama.png";
            String contentType = archivo.getContentType() != null ? archivo.getContentType() : "image/png";

            // 1. Decodificar y preprocesar imagen con visión artificial (Java 2D, Sobel, Otsu)
            BufferedImage originalImage = imageProcessorService.fromByteArray(rawBytes);
            BufferedImage preprocessedImage = imageProcessorService.preprocess(originalImage);

            // 2. Detección y extracción de elementos conceptuales UML
            ImageUMLDetectedDTO resultadoUML = umlDetectorService.detectUMLFromImage(preprocessedImage, rawBytes, originalFilename);

            // 3. Serializar resultado detectado para persistencia y auditoría
            String resultadoJson = objectMapper.writeValueAsString(resultadoUML);

            // 4. Persistir registro de imagen en base de datos
            ImagenUML imagen = ImagenUML.builder()
                    .nombreArchivo(originalFilename)
                    .tipoContenido(contentType)
                    .tamanioBytes(archivo.getSize())
                    .ancho(originalImage.getWidth())
                    .alto(originalImage.getHeight())
                    .datosImagen(rawBytes)
                    .usuarioEmail(emailFinal)
                    .usuario(usuario)
                    .modelo(modelo)
                    .fechaCarga(LocalDateTime.now())
                    .estado(EstadoProcesamientoImagen.PROCESADA)
                    .resultadoJson(resultadoJson)
                    .build();

            ImagenUML guardada = imagenUMLRepository.save(imagen);
            log.info("Imagen procesada exitosamente con ID: {}, clases detectadas: {}, relaciones: {}",
                    guardada.getId(), resultadoUML.getClases().size(), resultadoUML.getRelaciones().size());

            return ImageUploadResponseDTO.builder()
                    .idImagen(guardada.getId())
                    .nombreArchivo(guardada.getNombreArchivo())
                    .tamanioBytes(guardada.getTamanioBytes())
                    .ancho(guardada.getAncho())
                    .alto(guardada.getAlto())
                    .estadoProcesamiento(guardada.getEstado())
                    .mensaje("Imagen analizada correctamente mediante visión artificial.")
                    .fechaCarga(guardada.getFechaCarga())
                    .resultadoUML(resultadoUML)
                    .build();

        } catch (Exception e) {
            log.error("Error al procesar la imagen del diagrama UML: {}", e.getMessage(), e);
            throw new RuntimeException("Error en el procesamiento visual de la imagen: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ModeloUMLDTO aplicarModeloDetectado(Long modeloId, ApplyImageUMLRequestDTO request, String usuarioEmail) {
        log.info("Aplicando modelo UML detectado a modelo ID: {} por usuario: {}", modeloId, usuarioEmail);

        ModeloUML modelo = modeloUMLRepository.findById(modeloId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo UML no encontrado con ID: " + modeloId));

        ImageUMLDetectedDTO modeloADetectar = null;
        ImagenUML imagenEntity = null;

        if (request != null && request.getModeloAjustado() != null && !request.getModeloAjustado().getClases().isEmpty()) {
            modeloADetectar = request.getModeloAjustado();
        }

        if (request != null && request.getIdImagen() != null) {
            imagenEntity = imagenUMLRepository.findById(request.getIdImagen()).orElse(null);
            if (modeloADetectar == null && imagenEntity != null && imagenEntity.getResultadoJson() != null) {
                try {
                    modeloADetectar = objectMapper.readValue(imagenEntity.getResultadoJson(), ImageUMLDetectedDTO.class);
                } catch (Exception e) {
                    log.warn("No se pudo deserializar resultado previo de la imagen: {}", e.getMessage());
                }
            }
        }

        if (modeloADetectar == null || modeloADetectar.getClases().isEmpty()) {
            throw new ValidationException("No se proporcionaron elementos UML válidos para aplicar al modelo.");
        }

        // Si se solicitó limpiar elementos previos
        if (request != null && Boolean.TRUE.equals(request.getLimpiarModeloExistente())) {
            log.info("Limpiando clases y relaciones existentes del modelo ID: {}", modeloId);
            relacionUMLRepository.deleteAll(relacionUMLRepository.findByModeloUMLId(modeloId));
            claseUMLRepository.deleteAll(claseUMLRepository.findByModeloUMLId(modeloId));
        }

        // Mapear y crear clases UML
        Map<String, ClaseUML> clasesGuardadas = new HashMap<>();

        for (ClaseDetectadaDTO cDto : modeloADetectar.getClases()) {
            Optional<ClaseUML> existenteOpt = claseUMLRepository.findByModeloUMLIdAndNombreIgnoreCase(modeloId, cDto.getNombre().trim());
            ClaseUML clase;

            if (existenteOpt.isPresent()) {
                clase = existenteOpt.get();
                if (cDto.getDescripcion() != null) {
                    clase.setDescripcion(cDto.getDescripcion());
                }
            } else {
                clase = ClaseUML.builder()
                        .nombre(cDto.getNombre().trim())
                        .visibilidad(cDto.getVisibilidad() != null ? cDto.getVisibilidad() : VisibilidadUML.PUBLIC)
                        .descripcion(cDto.getDescripcion())
                        .posicionX(cDto.getPosicionX() != null ? cDto.getPosicionX() : 100.0)
                        .posicionY(cDto.getPosicionY() != null ? cDto.getPosicionY() : 100.0)
                        .modeloUML(modelo)
                        .atributos(new ArrayList<>())
                        .metodos(new ArrayList<>())
                        .build();
                clase = claseUMLRepository.save(clase);
            }

            // Atributos
            if (cDto.getAtributos() != null) {
                for (AtributoDetectadoDTO aDto : cDto.getAtributos()) {
                    final ClaseUML finalClase = clase;
                    boolean attrExiste = finalClase.getAtributos().stream()
                            .anyMatch(a -> a.getNombre().equalsIgnoreCase(aDto.getNombre()));
                    if (!attrExiste) {
                        AtributoUML attr = AtributoUML.builder()
                                .nombre(aDto.getNombre().trim())
                                .tipoDato(aDto.getTipoDato() != null ? aDto.getTipoDato() : "String")
                                .visibilidad(aDto.getVisibilidad() != null ? aDto.getVisibilidad() : VisibilidadUML.PRIVATE)
                                .valorInicial(aDto.getValorInicial())
                                .claseUML(clase)
                                .build();
                        atributoUMLRepository.save(attr);
                        clase.getAtributos().add(attr);
                    }
                }
            }

            // Métodos
            if (cDto.getMetodos() != null) {
                for (MetodoDetectadoDTO mDto : cDto.getMetodos()) {
                    final ClaseUML finalClase = clase;
                    boolean metodoExiste = finalClase.getMetodos().stream()
                            .anyMatch(m -> m.getNombre().equalsIgnoreCase(mDto.getNombre()));
                    if (!metodoExiste) {
                        MetodoUML metodo = MetodoUML.builder()
                                .nombre(mDto.getNombre().trim())
                                .tipoRetorno(mDto.getTipoRetorno() != null ? mDto.getTipoRetorno() : "void")
                                .visibilidad(mDto.getVisibilidad() != null ? mDto.getVisibilidad() : VisibilidadUML.PUBLIC)
                                .parametros(mDto.getParametros())
                                .claseUML(clase)
                                .build();
                        clase.getMetodos().add(metodo);
                    }
                }
            }

            clasesGuardadas.put(clase.getNombre().toLowerCase(), clase);
        }

        // Crear relaciones detectadas
        if (modeloADetectar.getRelaciones() != null) {
            for (RelacionDetectadaDTO rDto : modeloADetectar.getRelaciones()) {
                if (rDto.getClaseOrigen() == null || rDto.getClaseDestino() == null) continue;

                ClaseUML origen = clasesGuardadas.get(rDto.getClaseOrigen().toLowerCase().trim());
                ClaseUML destino = clasesGuardadas.get(rDto.getClaseDestino().toLowerCase().trim());

                if (origen != null && destino != null && !origen.getId().equals(destino.getId())) {
                    boolean relExiste = relacionUMLRepository.findByModeloUMLId(modeloId).stream()
                            .anyMatch(r -> r.getClaseOrigen().getId().equals(origen.getId()) &&
                                           r.getClaseDestino().getId().equals(destino.getId()));
                    if (!relExiste) {
                        RelacionUML rel = RelacionUML.builder()
                                .tipoRelacion(rDto.getTipoRelacion() != null ? rDto.getTipoRelacion() : TipoRelacionUML.ASOCIACION)
                                .claseOrigen(origen)
                                .claseDestino(destino)
                                .cardinalidadOrigen(rDto.getCardinalidadOrigen() != null ? rDto.getCardinalidadOrigen() : "1")
                                .cardinalidadDestino(rDto.getCardinalidadDestino() != null ? rDto.getCardinalidadDestino() : "1")
                                .descripcion(rDto.getDescripcion())
                                .modeloUML(modelo)
                                .build();
                        relacionUMLRepository.save(rel);
                    }
                }
            }
        }

        // Actualizar estado de la entidad imagen si existe
        if (imagenEntity != null) {
            imagenEntity.setEstado(EstadoProcesamientoImagen.APLICADA);
            imagenEntity.setModelo(modelo);
            imagenUMLRepository.save(imagenEntity);
        }

        String nombreArchivoOrigen = (imagenEntity != null) ? imagenEntity.getNombreArchivo() : "imagen_diagrama.png";

        // 13. Integración con Historial y Versiones (Fase 6)
        if (versionService != null) {
            try {
                CreateVersionDTO versionDTO = CreateVersionDTO.builder()
                        .modeloId(modeloId)
                        .nombreVersion("Versión Generada desde Imagen (" + nombreArchivoOrigen + ")")
                        .descripcion("Modelo generado automáticamente mediante procesamiento de imagen y visión artificial. " +
                                     "Clases: " + modeloADetectar.getClases().size() + ", Relaciones: " +
                                     (modeloADetectar.getRelaciones() != null ? modeloADetectar.getRelaciones().size() : 0))
                        .build();
                versionService.crearVersion(versionDTO, usuarioEmail);

                versionService.registrarCambio(modeloId, TipoOperacionHistorial.CREATE,
                        "Modelo UML Completo", modeloId.toString(),
                        null, "Generado automáticamente desde imagen: " + nombreArchivoOrigen, usuarioEmail);
            } catch (Exception e) {
                log.warn("No se pudo registrar la versión formal para la imagen: {}", e.getMessage());
            }
        }

        // 14. Integración con Colaboración en Tiempo Real WebSocket (Fase 5)
        if (eventPublisher != null) {
            try {
                UMLEvent event = UMLEvent.builder()
                        .modeloUMLId(modeloId)
                        .tipoOperacion(TipoOperacionUML.UPDATE)
                        .elementoTipo(TipoElementoUML.MODELO)
                        .elementoId(modeloId.toString())
                        .usuario(usuarioEmail != null ? usuarioEmail : "vision.ia@caseplatform.com")
                        .fecha(LocalDateTime.now())
                        .datosCambio(Map.of(
                                "mensaje", "Nuevo modelo generado desde imagen: " + nombreArchivoOrigen,
                                "totalClases", modeloADetectar.getClases().size(),
                                "origen", "IMAGE_TO_UML"
                        ))
                        .build();
                eventPublisher.publishEvent(modeloId, event);
            } catch (Exception e) {
                log.warn("No se pudo difundir el evento de modelo desde imagen por WebSocket: {}", e.getMessage());
            }
        }

        return modeloUMLService.obtenerPorId(modeloId);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageUploadResponseDTO obtenerDetalleImagen(Long imagenId) {
        ImagenUML img = imagenUMLRepository.findById(imagenId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen UML no encontrada con ID: " + imagenId));

        ImageUMLDetectedDTO detectedDTO = null;
        if (img.getResultadoJson() != null) {
            try {
                detectedDTO = objectMapper.readValue(img.getResultadoJson(), ImageUMLDetectedDTO.class);
            } catch (Exception ignored) {
            }
        }

        return ImageUploadResponseDTO.builder()
                .idImagen(img.getId())
                .nombreArchivo(img.getNombreArchivo())
                .tamanioBytes(img.getTamanioBytes())
                .ancho(img.getAncho())
                .alto(img.getAlto())
                .estadoProcesamiento(img.getEstado())
                .mensaje("Detalle de imagen recuperado exitosamente.")
                .fechaCarga(img.getFechaCarga())
                .resultadoUML(detectedDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] obtenerArchivoImagen(Long imagenId) {
        ImagenUML img = imagenUMLRepository.findById(imagenId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen UML no encontrada con ID: " + imagenId));
        if (img.getDatosImagen() == null || img.getDatosImagen().length == 0) {
            throw new ResourceNotFoundException("El archivo binario de la imagen no está disponible.");
        }
        return img.getDatosImagen();
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ValidationException("Debe seleccionar un archivo de imagen no vacío.");
        }

        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("El archivo excede el tamaño máximo permitido de 10 MB.");
        }

        String originalFilename = archivo.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ValidationException("El nombre del archivo no es válido.");
        }

        String lowerFilename = originalFilename.toLowerCase();
        boolean extensionValida = ALLOWED_EXTENSIONS.stream().anyMatch(lowerFilename::endsWith);
        if (!extensionValida) {
            throw new ValidationException("Formato no soportado. Formatos permitidos: PNG, JPG, JPEG.");
        }

        String contentType = archivo.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ValidationException("El tipo de contenido no corresponde a una imagen válida (PNG, JPG, JPEG).");
        }
    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}
