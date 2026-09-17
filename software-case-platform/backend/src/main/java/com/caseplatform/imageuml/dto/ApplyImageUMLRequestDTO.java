package com.caseplatform.imageuml.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para aplicar el modelo UML detectado (o corregido manualmente)
 * al modelo UML existente dentro del editor visual.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyImageUMLRequestDTO {

    private Long idImagen;
    private ImageUMLDetectedDTO modeloAjustado;
    
    @Builder.Default
    private Boolean limpiarModeloExistente = false;

    @Builder.Default
    private String comentario = "Generado automáticamente desde imagen";
}
