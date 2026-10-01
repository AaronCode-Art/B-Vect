package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UsuarioEspecialidadRequest(@NotNull List<@NotNull UUID> especialidadIds, UUID principalId) {}
