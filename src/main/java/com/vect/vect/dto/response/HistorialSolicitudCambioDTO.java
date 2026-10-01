package com.vect.vect.dto.response;

import com.vect.vect.entity.SolicitudCambio;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistorialSolicitudCambioDTO(
    Long id,
    UUID solicitudId,
    String solicitudCodigo,
    SolicitudCambio.EstadoSolicitud estadoOrigen,
    SolicitudCambio.EstadoSolicitud estadoDestino,
    String comentario,
    UUID realizadoPorId,
    String realizadoPorNombre,
    LocalDateTime creadoEn
) {}
