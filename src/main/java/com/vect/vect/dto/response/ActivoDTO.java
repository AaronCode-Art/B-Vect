package com.vect.vect.dto.response;

import com.vect.vect.entity.Activo;

import java.time.LocalDate;
import java.util.UUID;

public record ActivoDTO(
    UUID id,
    String codigo,
    String nombre,
    String numeroSerie,
    Activo.EstadoActivo estado,
    UUID usuarioAsignadoId,
    String usuarioAsignadoNombre,
    UUID ubicacionId,
    String ubicacionNombre,
    LocalDate fechaCompra,
    String notas
) {}
