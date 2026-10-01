package com.vect.vect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UsuarioEspecialidadId implements Serializable {

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "especialidad_id")
    private UUID especialidadId;
}
