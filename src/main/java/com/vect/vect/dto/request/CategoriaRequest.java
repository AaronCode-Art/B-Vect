package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
    @NotBlank @Size(max = 150) String nombre,
    String descripcion,
    Boolean activo
) {}
