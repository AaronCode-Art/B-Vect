package com.vect.vect.dto.response;

import com.vect.vect.entity.Conversacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConversacionDTO(
    UUID id,
    Conversacion.TipoConversacion tipo,
    String nombre,
    UUID creadoPorId,
    List<ParticipanteDTO> participantes,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
    public record ParticipanteDTO(UUID id, String nombre, String correo) {}
}
