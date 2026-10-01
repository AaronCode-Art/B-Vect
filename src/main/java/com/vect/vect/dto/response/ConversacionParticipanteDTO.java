package com.vect.vect.dto.response;

import java.util.UUID;

public record ConversacionParticipanteDTO(
    UUID id,
    String nombre,
    String correo,
    String rol
) {}
