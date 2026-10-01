package com.vect.vect.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "parametros_sistema", schema = "vect")
@Getter
@Setter
@NoArgsConstructor
public class ParametroSistema {

    @Id
    @Column(name = "clave", nullable = false, length = 120)
    private String clave;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valor", nullable = false, columnDefinition = "jsonb")
    private JsonNode valor;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "es_publico", nullable = false)
    private Boolean esPublico = false;

    @Column(name = "actualizado_por")
    private UUID actualizadoPor;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @PrePersist
    void onCreate() {
        actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
