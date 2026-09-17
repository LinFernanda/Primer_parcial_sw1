package com.caseplatform.imageuml.dto;

import com.caseplatform.imageuml.model.EstadoProcesamientoImagen;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Respuesta devuelta tras la carga y procesamiento de la imagen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponseDTO {

    private Long idImagen;
    private String nombreArchivo;
    private Long tamanioBytes;
    private Integer ancho;
    private Integer alto;
    private EstadoProcesamientoImagen estadoProcesamiento;
    private String mensaje;
    private LocalDateTime fechaCarga;
    private ImageUMLDetectedDTO resultadoUML;
}
