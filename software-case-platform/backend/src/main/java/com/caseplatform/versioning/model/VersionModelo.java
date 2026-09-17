package com.caseplatform.versioning.model;

import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad VersionModelo que representa una versión congelada (snapshot) del modelo UML.
 */
@Entity
@Table(name = "versiones_modelo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionModelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_version", nullable = false, length = 50)
    private String numeroVersion;

    @Column(name = "nombre_version", nullable = false, length = 150)
    private String nombreVersion;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String snapshotJson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modelo_id", nullable = false)
    private ModeloUML modeloUML;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creador_id")
    private Usuario usuarioCreador;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    private String estado = "ACTIVA";

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "ACTIVA";
        }
    }
}
