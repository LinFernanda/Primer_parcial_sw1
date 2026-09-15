package com.caseplatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * Entidad RelacionUML que representa una relación o conexión entre dos clases (Asociación, Herencia, Dependencia, etc.).
 */
@Entity
@Table(name = "relaciones_uml")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelacionUML {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_relacion", nullable = false, length = 30)
    private TipoRelacionUML tipoRelacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clase_origen_id", nullable = false)
    private ClaseUML claseOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clase_destino_id", nullable = false)
    private ClaseUML claseDestino;

    @Column(name = "cardinalidad_origen", length = 20)
    private String cardinalidadOrigen;

    @Column(name = "cardinalidad_destino", length = 20)
    private String cardinalidadDestino;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modelo_id", nullable = false)
    private ModeloUML modeloUML;

    @PrePersist
    protected void onCreate() {
        if (this.tipoRelacion == null) {
            this.tipoRelacion = TipoRelacionUML.ASOCIACION;
        }
    }
}
