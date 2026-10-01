package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RechazarSolicitudRequest(@NotBlank String motivoRechazo) {}
