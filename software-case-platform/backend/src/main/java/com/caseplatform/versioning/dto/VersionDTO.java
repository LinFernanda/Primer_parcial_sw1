package com.caseplatform.versioning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta y consulta de versiones de un modelo UML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDTO {

    private Long id;
    private String numeroVersion;
    private String nombreVersion;
    private String descripcion;
    private Long modeloId;
    private String modeloNombre;
    private Long usuarioCreadorId;
    private String usuarioCreadorNombre;
    private String usuarioCreadorEmail;
    private LocalDateTime fechaCreacion;
    private String estado;
    private String snapshotJson;
    private SnapshotModeloDTO snapshot;
}
