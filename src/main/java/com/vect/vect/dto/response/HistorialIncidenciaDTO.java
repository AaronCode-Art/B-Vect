package com.vect.vect.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.vect.vect.entity.Incidencia;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistorialIncidenciaDTO(
    Long id,
    String tipoEvento,
    Incidencia.EstadoIncidencia estadoOrigen,
    Incidencia.EstadoIncidencia estadoDestino,
    String motivo,
    JsonNode detalles,
    UUID realizadoPorId,
    String realizadoPorNombre,
    LocalDateTime creadoEn
) {}
