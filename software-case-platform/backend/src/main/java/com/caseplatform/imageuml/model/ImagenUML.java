package com.caseplatform.imageuml.model;

import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad ImagenUML para el registro y almacenamiento temporal de diagramas
 * escaneados, bocetos o capturas cargadas por el usuario para su conversión.
 */
@Entity
@Table(name = "imagenes_uml")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagenUML {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "tipo_contenido", nullable = false, length = 100)
    private String tipoContenido;

    @Column(name = "tamanio_bytes", nullable = false)
    private Long tamanioBytes;

    @Column(name = "ancho")
    private Integer ancho;

    @Column(name = "alto")
    private Integer alto;

    @Column(name = "ruta_almacenamiento", length = 500)
    private String rutaAlmacenamiento;

    @Column(name = "datos_imagen", columnDefinition = "BYTEA")
    private byte[] datosImagen;

    @Column(name = "usuario_email", nullable = false, length = 150)
    private String usuarioEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_id")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private ModeloUML modelo;

    @Column(name = "fecha_carga", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_procesamiento", nullable = false, length = 30, columnDefinition = "varchar(30)")
    @Builder.Default
    private EstadoProcesamientoImagen estado = EstadoProcesamientoImagen.CARGADA;

    @Column(name = "resultado_json", columnDefinition = "TEXT")
    private String resultadoJson;

    @Column(name = "mensaje_error", length = 1000)
    private String mensajeError;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCarga == null) {
            this.fechaCarga = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoProcesamientoImagen.CARGADA;
        }
    }
}
