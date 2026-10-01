package com.vect.vect.dto.request;

import com.vect.vect.entity.Conversacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ConversacionCreateRequest(
    @NotNull Conversacion.TipoConversacion tipo,
    @Size(max = 180) String nombre,
    @NotNull @Size(min = 1) List<UUID> participantes
) {}
