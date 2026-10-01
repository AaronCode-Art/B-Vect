package com.vect.vect.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParametroSistemaDTO(
    String clave,
    JsonNode valor,
    String descripcion,
    Boolean esPublico,
    UUID actualizadoPor,
    LocalDateTime actualizadoEn
) {}
