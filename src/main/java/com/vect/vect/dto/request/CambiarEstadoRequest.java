package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.vect.vect.entity.Incidencia;

public record CambiarEstadoRequest(
    @NotNull Incidencia.EstadoIncidencia estadoCodigo,
    @NotBlank String motivo
) {}
