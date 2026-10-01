package com.vect.vect.dto.request;

import com.vect.vect.entity.Activo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record ActivoRequest(
    @NotBlank @Size(max = 50) String codigo,
    @NotBlank @Size(max = 180) String nombre,
    @Size(max = 150) String numeroSerie,
    @NotNull Activo.EstadoActivo estado,
    UUID usuarioAsignadoId,
    UUID ubicacionId,
    LocalDate fechaCompra,
    String notas
) {}
