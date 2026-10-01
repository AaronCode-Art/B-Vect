package com.vect.vect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "miembros_conversacion", schema = "vect")
@Getter
@Setter
@NoArgsConstructor
public class MiembroConversacion {

    @EmbeddedId
    private MiembroConversacionId id = new MiembroConversacionId();

    @MapsId("conversacionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private Conversacion conversacion;

    @MapsId("usuarioId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "unido_en", nullable = false)
    private LocalDateTime unidoEn;

    @Column(name = "salio_en")
    private LocalDateTime salioEn;

}
