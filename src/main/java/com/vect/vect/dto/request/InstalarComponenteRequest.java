package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record InstalarComponenteRequest(
    @NotNull UUID componenteId,
    @Size(max = 150) String numeroSerie
) {}
