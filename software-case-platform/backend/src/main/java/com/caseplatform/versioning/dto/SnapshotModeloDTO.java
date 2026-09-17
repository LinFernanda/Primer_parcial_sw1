package com.caseplatform.versioning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Representación estructurada del snapshot del modelo UML completo para serialización en JSON.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotModeloDTO {

    private Long modeloId;
    private String nombreModelo;
    private String version;
    @Builder.Default
    private List<SnapshotClaseDTO> clases = new ArrayList<>();
    @Builder.Default
    private List<SnapshotRelacionDTO> relaciones = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SnapshotClaseDTO {
        private Long id;
        private String nombre;
        private String visibilidad;
        private String descripcion;
        private Double posicionX;
        private Double posicionY;
        @Builder.Default
        private List<SnapshotAtributoDTO> atributos = new ArrayList<>();
        @Builder.Default
        private List<SnapshotMetodoDTO> metodos = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SnapshotAtributoDTO {
        private Long id;
        private String nombre;
        private String tipoDato;
        private String visibilidad;
        private String valorInicial;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SnapshotMetodoDTO {
        private Long id;
        private String nombre;
        private String tipoRetorno;
        private String visibilidad;
        private String parametros;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SnapshotRelacionDTO {
        private Long id;
        private String tipoRelacion;
        private Long claseOrigenId;
        private String claseOrigenNombre;
        private Long claseDestinoId;
        private String claseDestinoNombre;
        private String cardinalidadOrigen;
        private String cardinalidadDestino;
        private String descripcion;
    }
}
