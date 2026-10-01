package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.vect.vect.entity.Incidencia;

import java.util.UUID;

public record IncidenciaCreateRequest(
    @NotBlank String titulo,
    @NotBlank String descripcion,
    @NotNull UUID categoriaId,
    UUID ubicacionId,
    @NotNull Incidencia.CanalIncidencia canal,
    @NotNull Incidencia.NivelPrioridad impacto,
    @NotNull Incidencia.NivelPrioridad urgencia
) {}
