package com.vect.vect.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReglaAutomatizacionDTO(
    UUID id,
    String nombre,
    String descripcion,
    String nombreEvento,
    JsonNode condiciones,
    JsonNode acciones,
    Integer prioridad,
    Boolean activo,
    UUID creadoPorId,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {}
