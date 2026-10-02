package com.vect.vect.service;

import com.vect.vect.dto.response.ReporteIncidenciasDTO;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.repository.IncidenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    private static final int MAX_DIAS_REPORTE = 366;

    private final IncidenciaRepository incidenciaRepository;

    public ReporteService(IncidenciaRepository incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    @Transactional(readOnly = true)
    public ReporteIncidenciasDTO generar(int dias) {
        if (dias < 1 || dias > MAX_DIAS_REPORTE) {
            throw com.vect.vect.common.exception.ApiException.badRequest("RANGO_REPORTE_INVALIDO",
                "El rango del reporte debe estar entre 1 y " + MAX_DIAS_REPORTE + " días");
        }
        LocalDate hasta = LocalDate.now();
        LocalDate desde = hasta.minusDays(dias - 1L);
        LocalDateTime desdeHora = desde.atStartOfDay();

        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (Incidencia.EstadoIncidencia estado : Incidencia.EstadoIncidencia.values()) {
            porEstado.put(estado.name(), 0L);
        }
        incidenciaRepository.contarPorEstado(desdeHora).forEach(row ->
            porEstado.put(((Incidencia.EstadoIncidencia) row[0]).name(), ((Number) row[1]).longValue()));

        Map<String, Long> porCategoria = toMap(incidenciaRepository.contarPorCategoria(desdeHora));
        Map<String, Long> porPrioridad = new LinkedHashMap<>();
        for (Incidencia.NivelPrioridad prioridad : Incidencia.NivelPrioridad.values()) {
            porPrioridad.put(prioridad.name(), 0L);
        }
        incidenciaRepository.contarPorPrioridad(desdeHora).forEach(row ->
            porPrioridad.put(((Incidencia.NivelPrioridad) row[0]).name(), ((Number) row[1]).longValue()));

        long total = porEstado.values().stream().mapToLong(Long::longValue).sum();
        List<ReporteIncidenciasDTO.SerieDiaria> serie = incidenciaRepository
            .contarCreadasYResueltasPorDia(desdeHora, hasta).stream()
            .map(row -> new ReporteIncidenciasDTO.SerieDiaria(
                LocalDate.parse(row[0].toString()),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue()))
            .toList();
        return new ReporteIncidenciasDTO(desde, hasta, total,
            porEstado, porCategoria, porPrioridad, serie);
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        rows.forEach(row -> result.put((String) row[0], ((Number) row[1]).longValue()));
        return result;
    }
}
