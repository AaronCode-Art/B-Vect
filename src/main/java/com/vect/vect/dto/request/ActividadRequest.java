package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActividadRequest(
    @NotBlank @Size(max = 100) String tipo,
    @NotBlank String descripcion,
    @NotNull @Min(1) Integer minutos
) {}
