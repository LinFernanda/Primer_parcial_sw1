package com.caseplatform.ai.model;

import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.Usuario;
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

import java.time.LocalDateTime;

/**
 * Entidad de persistencia para registrar cada comando recibido, interpretado
 * y ejecutado por el Agente de Inteligencia Artificial sobre los modelos UML.
 */
@Entity
@Table(name = "ai_comandos_historial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AICommandHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false, length = 50)
    private TipoOperacionAI tipoOperacion;

    @Column(name = "elemento_objetivo", length = 100)
    private String elementoObjetivo;

    @Column(name = "parametros", columnDefinition = "TEXT")
    private String parametros;

    @Column(name = "prompt_original", nullable = false, columnDefinition = "TEXT")
    private String promptOriginal;

    @Column(name = "respuesta_generada", columnDefinition = "TEXT")
    private String respuestaGenerada;

    @Column(name = "exitoso", nullable = false)
    @Builder.Default
    private Boolean exitoso = true;

    @Column(name = "requiere_confirmacion", nullable = false)
    @Builder.Default
    private Boolean requiereConfirmacion = false;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "usuario_email", nullable = false, length = 150)
    private String usuarioEmail;

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modelo_id", nullable = false)
    private ModeloUML modeloUML;

    public String getPrompt() {
        return promptOriginal;
    }

    public TipoOperacionAI getOperacion() {
        return tipoOperacion;
    }

    public Boolean getEjecutadoConExito() {
        return exitoso;
    }

    public LocalDateTime getFechaEjecucion() {
        return fecha;
    }

    public Long getModeloId() {
        return modeloUML != null ? modeloUML.getId() : null;
    }

    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.exitoso == null) {
            this.exitoso = true;
        }
        if (this.requiereConfirmacion == null) {
            this.requiereConfirmacion = false;
        }
    }
}
