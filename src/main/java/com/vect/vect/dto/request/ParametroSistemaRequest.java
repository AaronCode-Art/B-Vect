package com.vect.vect.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record ParametroSistemaRequest(
    @NotNull JsonNode valor
) {}
