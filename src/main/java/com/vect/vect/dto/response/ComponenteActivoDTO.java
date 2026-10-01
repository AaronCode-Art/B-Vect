package com.vect.vect.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ComponenteActivoDTO(
    UUID id,
    UUID activoId,
    UUID componenteId,
    String componenteCodigo,
    String componenteNombre,
    String numeroSerie,
    LocalDateTime instaladoEn,
    LocalDateTime retiradoEn,
    UUID instaladoPor
) {}
