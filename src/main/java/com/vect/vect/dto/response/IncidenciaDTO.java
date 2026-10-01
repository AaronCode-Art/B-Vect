package com.vect.vect.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncidenciaDTO(
    UUID id,
    String codigo,
    String titulo,
    String descripcion,
    String categoriaNombre,
    String ubicacionNombre,
    String canal,
    String impacto,
    String urgencia,
    String prioridad,
    String estado,
    UUID reportanteId,
    String reportanteNombre,
    UUID tecnicoAsignadoId,
    String tecnicoNombre,
    LocalDateTime creadoEn,
    LocalDateTime resueltoEn,
    LocalDateTime cerradoEn
) {}
