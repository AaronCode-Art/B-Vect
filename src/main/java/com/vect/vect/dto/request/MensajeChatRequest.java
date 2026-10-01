package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.vect.vect.entity.MensajeIncidencia;

public record MensajeChatRequest(
    @NotBlank String contenido,
    @NotNull MensajeIncidencia.CanalMensaje canal,
    @NotNull MensajeIncidencia.VisibilidadMensaje visibilidad
) {}
