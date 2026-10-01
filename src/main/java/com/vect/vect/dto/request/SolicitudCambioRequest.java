package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.UUID;

public record SolicitudCambioRequest(
    @NotNull UUID incidenciaId,
    UUID componenteId,
    UUID activoId,
    @NotBlank String descripcion,
    @NotBlank String justificacion,
    @NotNull @DecimalMin(value = "0.00") BigDecimal costoEstimado
) {}
