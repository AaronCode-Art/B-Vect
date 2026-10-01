package com.vect.vect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "componentes_activo", schema = "vect")
@Getter
@Setter
@NoArgsConstructor
public class ComponenteActivo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activo_id", nullable = false)
    private Activo activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "componente_id", nullable = false)
    private Componente componente;

    @Column(name = "numero_serie", length = 150)
    private String numeroSerie;

    @Column(name = "instalado_en", nullable = false)
    private LocalDateTime instaladoEn;

    @Column(name = "retirado_en")
    private LocalDateTime retiradoEn;

    @Column(name = "instalado_por")
    private UUID instaladoPor;

    @PrePersist
    void onCreate() {
        instaladoEn = LocalDateTime.now();
    }
}
