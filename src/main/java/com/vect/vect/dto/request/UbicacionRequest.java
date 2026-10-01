package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UbicacionRequest(
    @NotBlank @Size(max = 180) String nombre,
    String direccion,
    Boolean activo
) {}
