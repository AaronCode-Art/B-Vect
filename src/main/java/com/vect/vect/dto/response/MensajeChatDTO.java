package com.vect.vect.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MensajeChatDTO(
    UUID id,
    UUID incidenciaId,
    UUID remitenteId,
    String remitenteNombre,
    String canal,
    String visibilidad,
    String contenido,
    LocalDateTime creadoEn
) {}
