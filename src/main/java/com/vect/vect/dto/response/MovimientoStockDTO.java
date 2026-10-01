package com.vect.vect.dto.response;

import com.vect.vect.entity.MovimientoStock;

import java.time.LocalDateTime;
import java.util.UUID;

public record MovimientoStockDTO(
    UUID id,
    UUID componenteId,
    String componenteCodigo,
    String componenteNombre,
    MovimientoStock.TipoMovimiento tipoMovimiento,
    Integer cantidad,
    Integer stockAnterior,
    Integer stockResultante,
    String tipoReferencia,
    UUID referenciaId,
    String motivo,
    String realizadoPorNombre,
    LocalDateTime creadoEn
) {}
