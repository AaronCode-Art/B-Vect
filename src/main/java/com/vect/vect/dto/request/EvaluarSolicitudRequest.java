package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotNull;

public record EvaluarSolicitudRequest(
    @NotNull Boolean aprobar,
    String comentario,
    String motivoRechazo
) {}
