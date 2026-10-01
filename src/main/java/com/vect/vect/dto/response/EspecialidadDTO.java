package com.vect.vect.dto.response;

import java.util.UUID;

public record EspecialidadDTO(UUID id, String nombre, String descripcion, Boolean activo) {}
