package com.vect.vect.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActividadIncidenciaDTO(
    UUID id,
    UUID incidenciaId,
    UUID tecnicoId,
    String tecnicoNombre,
    String tipoActividad,
    String descripcion,
    Integer minutos,
    LocalDateTime creadoEn
) {}
