package com.vect.vect.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ComponenteDTO(
    UUID id,
    String codigo,
    String nombre,
    String tipoComponente,
    String descripcion,
    BigDecimal costoUnitario,
    Integer stockActual,
    Integer stockMinimo,
    Boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {}
