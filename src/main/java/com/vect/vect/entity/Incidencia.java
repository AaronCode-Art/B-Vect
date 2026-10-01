package com.vect.vect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "incidencias", schema = "vect")
@Getter
@Setter
@NoArgsConstructor
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "titulo", nullable = false, length = 250)
    private String titulo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    @Column(name = "detalle_ubicacion", length = 250)
    private String detalleUbicacion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "canal", nullable = false, columnDefinition = "vect.canal_incidencia")
    private CanalIncidencia canal;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "impacto", nullable = false, columnDefinition = "vect.nivel_prioridad")
    private NivelPrioridad impacto;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "urgencia", nullable = false, columnDefinition = "vect.nivel_prioridad")
    private NivelPrioridad urgencia;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "prioridad", nullable = false, columnDefinition = "vect.nivel_prioridad")
    private NivelPrioridad prioridad;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado", nullable = false, columnDefinition = "vect.estado_incidencia")
    private EstadoIncidencia estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportante_id", nullable = false)
    private Usuario reportante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_asignado_id")
    private Usuario tecnicoAsignado;

    @Column(name = "asignado_en")
    private LocalDateTime asignadoEn;

    @Column(name = "resuelto_en")
    private LocalDateTime resueltoEn;

    @Column(name = "cerrado_en")
    private LocalDateTime cerradoEn;

    @Column(name = "creado_por", nullable = false)
    private UUID creadoPor;

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

    public enum CanalIncidencia {
        PORTAL_WEB, CORREO, TELEFONO, PRESENCIAL
    }

    public enum NivelPrioridad {
        BAJO, MEDIO, ALTO, CRITICO
    }

    public enum EstadoIncidencia {
        REGISTRADA, ASIGNADA, EN_ATENCION, EN_ESPERA_USUARIO, EN_ESPERA_REPUESTO, RESUELTA, CERRADA
    }
}
