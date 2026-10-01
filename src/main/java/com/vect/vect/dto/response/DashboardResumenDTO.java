package com.vect.vect.dto.response;

import java.util.Map;

public record DashboardResumenDTO(
    long totalIncidencias,
    long abiertas,
    long enProceso,
    long resueltas,
    long cerradas,
    long slasVencidos,
    long tecnicosDisponibles,
    long solicitudesPendientes,
    long componentesBajoStock,
    Map<String, Long> porMacroEstado
) {}
