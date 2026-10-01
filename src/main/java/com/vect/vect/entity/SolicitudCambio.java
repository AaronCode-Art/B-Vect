package com.vect.vect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "solicitudes_cambio", schema = "vect")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidencia_id", nullable = false)
    private Incidencia incidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitado_por", nullable = false)
    private Usuario solicitadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "componente_id")
    private Componente componente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activo_id")
    private Activo activo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "justificacion", nullable = false, columnDefinition = "TEXT")
    private String justificacion;

    @Column(name = "costo_estimado", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoEstimado;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado", nullable = false, columnDefinition = "vect.estado_solicitud_cambio")
    private EstadoSolicitud estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluado_por")
    private Usuario evaluadoPor;

    @Column(name = "evaluado_en")
    private LocalDateTime evaluadoEn;

    @Column(name = "comentario_evaluacion", columnDefinition = "TEXT")
    private String comentarioEvaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_gerencia_por")
    private Usuario aprobadoGerenciaPor;

    @Column(name = "aprobado_gerencia_en")
    private LocalDateTime aprobadoGerenciaEn;

    @Column(name = "comentario_gerencia", columnDefinition = "TEXT")
    private String comentarioGerencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejecutado_por")
    private Usuario ejecutadoPor;

    @Column(name = "ejecutado_en")
    private LocalDateTime ejecutadoEn;

    @Column(name = "notas_ejecucion", columnDefinition = "TEXT")
    private String notasEjecucion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @PrePersist
    void onCreate() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }

    public enum EstadoSolicitud {
        PENDIENTE_EVALUACION, PENDIENTE_GERENCIA, APROBADA, RECHAZADA, EJECUTADA
    }
}
