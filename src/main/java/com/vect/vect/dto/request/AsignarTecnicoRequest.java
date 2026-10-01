package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AsignarTecnicoRequest(
    @NotNull UUID tecnicoId
) {}
