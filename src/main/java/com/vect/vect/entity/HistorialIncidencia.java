package com.vect.vect.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historial_incidencia", schema = "vect")
@Getter
@Setter
@NoArgsConstructor
public class HistorialIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidencia_id", nullable = false)
    private Incidencia incidencia;

    @Column(name = "tipo_evento", nullable = false, length = 80)
    private String tipoEvento;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado_origen")
    private Incidencia.EstadoIncidencia estadoOrigen;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado_destino")
    private Incidencia.EstadoIncidencia estadoDestino;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalles", nullable = false, columnDefinition = "jsonb")
    private JsonNode detalles = JsonNodeFactory.instance.objectNode();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realizado_por", nullable = false)
    private Usuario realizadoPor;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() {
        creadoEn = LocalDateTime.now();
        if (detalles == null) {
            detalles = JsonNodeFactory.instance.objectNode();
        }
    }
}
