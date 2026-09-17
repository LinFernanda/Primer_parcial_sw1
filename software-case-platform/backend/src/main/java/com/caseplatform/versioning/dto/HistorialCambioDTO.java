package com.caseplatform.versioning.dto;

import com.caseplatform.versioning.model.TipoOperacionHistorial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la consulta y reporte de modificaciones históricas del modelo UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCambioDTO {

    private Long id;
    private TipoOperacionHistorial tipoOperacion;
    private String elementoModificado;
    private String idElemento;
    private String datosAnteriores;
    private String datosNuevos;
    private Long usuarioId;
    private String usuarioEmail;
    private String usuarioNombre;
    private LocalDateTime fechaCambio;
    private Long versionModeloId;
    private String versionNumero;
    private Long modeloId;
    private String descripcionResumen;
}
