package com.vect.vect.dto.response;

import java.util.UUID;

public record CategoriaDTO(UUID id, String nombre, String descripcion, Boolean activo) {}
