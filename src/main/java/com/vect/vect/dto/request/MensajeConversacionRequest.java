package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MensajeConversacionRequest(
    @NotBlank String cuerpo
) {}
