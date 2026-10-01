package com.vect.vect.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ReporteIncidenciasDTO(
    LocalDate desde,
    LocalDate hasta,
    long total,
    Map<String, Long> porEstado,
    Map<String, Long> porCategoria,
    Map<String, Long> porPrioridad,
    List<SerieDiaria> serieDiaria
) {
    public record SerieDiaria(LocalDate fecha, long creadas, long resueltas) {}
}
