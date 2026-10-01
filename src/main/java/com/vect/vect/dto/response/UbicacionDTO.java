package com.vect.vect.dto.response;

import java.util.UUID;

public record UbicacionDTO(UUID id, String nombre, String direccion, Boolean activo) {}
