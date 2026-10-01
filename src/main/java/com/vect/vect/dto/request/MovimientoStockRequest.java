package com.vect.vect.dto.request;

import com.vect.vect.entity.MovimientoStock;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MovimientoStockRequest(
    @NotNull MovimientoStock.TipoMovimiento tipoMovimiento,
    @NotNull @Min(1) Integer cantidad,
    @NotBlank String motivo,
    String tipoReferencia,
    UUID referenciaId
) {}
