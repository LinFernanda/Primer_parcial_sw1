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
 * Entidad AtributoUML que representa una propiedad o campo de una clase.
 */
@Entity
@Table(name = "atributos_uml")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtributoUML {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo_dato", nullable = false, length = 50)
    private String tipoDato;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidad", nullable = false, length = 20)
    @Builder.Default
    private VisibilidadUML visibilidad = VisibilidadUML.PRIVATE;

    @Column(name = "valor_inicial", length = 100)
    private String valorInicial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clase_id", nullable = false)
    private ClaseUML claseUML;

    @PrePersist
    protected void onCreate() {
        if (this.visibilidad == null) {
            this.visibilidad = VisibilidadUML.PRIVATE;
        }
    }
}
