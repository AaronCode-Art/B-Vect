package com.vect.vect.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MensajeConversacionDTO(
    UUID id,
    UUID conversacionId,
    UUID remitenteId,
    String remitenteNombre,
    String cuerpo,
    String archivoUrl,
    String archivoNombre,
    String archivoTipoMime,
    Long archivoTamanoBytes,
    LocalDateTime creadoEn,
    LocalDateTime editadoEn
) {}
