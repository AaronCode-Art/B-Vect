package com.vect.vect.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ComponenteRequest(
    @NotBlank @Size(max = 50) String codigo,
    @NotBlank @Size(max = 180) String nombre,
    @NotBlank @Size(max = 100) String tipoComponente,
    String descripcion,
    @NotNull @DecimalMin("0.00") BigDecimal costoUnitario,
    @NotNull @Min(0) Integer stockMinimo,
    @NotNull Boolean activo
) {}
