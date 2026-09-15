package com.caseplatform.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad ClaseUML que representa una clase dentro del diagrama conceptual.
 */
@Entity
@Table(
        name = "clases_uml",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"modelo_id", "nombre"}, name = "uk_clase_modelo_nombre")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaseUML {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidad", nullable = false, length = 20)
    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PUBLIC;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "posicion_x")
    @Builder.Default
    private Double posicionX = 0.0;

    @Column(name = "posicion_y")
    @Builder.Default
    private Double posicionY = 0.0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modelo_id", nullable = false)
    private ModeloUML modeloUML;

    @OneToMany(mappedBy = "claseUML", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AtributoUML> atributos = new ArrayList<>();

    @OneToMany(mappedBy = "claseUML", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MetodoUML> metodos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.visibilidad == null) {
            this.visibilidad = VisibilidadUML.PUBLIC;
        }
        if (this.posicionX == null) {
            this.posicionX = 0.0;
        }
        if (this.posicionY == null) {
            this.posicionY = 0.0;
        }
    }
}
