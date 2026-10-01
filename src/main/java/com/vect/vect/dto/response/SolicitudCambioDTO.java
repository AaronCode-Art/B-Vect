package com.vect.vect.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SolicitudCambioDTO(
    UUID id,
    String codigo,
    UUID incidenciaId,
    String incidenciaCodigo,
    String tipo,
    String componenteNombre,
    String activoCodigo,
    String descripcion,
    String justificacion,
    BigDecimal costoEstimado,
    String estado,
    String solicitadoPorNombre,
    LocalDateTime creadoEn
) {}
