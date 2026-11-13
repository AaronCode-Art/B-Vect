package com.vect.vect.service;

import com.vect.vect.dto.response.ReporteIncidenciasDTO;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.Usuario;
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
        return generar(dias, null);
    }

    @Transactional(readOnly = true)
    public ReporteIncidenciasDTO generar(int dias, Usuario actor) {
        if (dias < 1 || dias > MAX_DIAS_REPORTE) {
            throw com.vect.vect.common.exception.ApiException.badRequest("RANGO_REPORTE_INVALIDO",
                "El rango del reporte debe estar entre 1 y " + MAX_DIAS_REPORTE + " días");
        }
        LocalDate hasta = LocalDate.now();
        LocalDate desde = hasta.minusDays(dias - 1L);
        LocalDateTime desdeHora = desde.atStartOfDay();
        boolean globalAccess = actor != null && (actor.getRol() == Usuario.RolUsuario.ADMIN
            || actor.getRol() == Usuario.RolUsuario.GERENCIA
            || actor.getRol() == Usuario.RolUsuario.SUPERVISOR);
        boolean technician = actor != null && actor.getRol() == Usuario.RolUsuario.TECNICO;

        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (Incidencia.EstadoIncidencia estado : Incidencia.EstadoIncidencia.values()) {
            porEstado.put(estado.name(), 0L);
        }
        var estados = actor == null
            ? incidenciaRepository.contarPorEstado(desdeHora)
            : incidenciaRepository.contarEstadosVisibles(
                desdeHora, globalAccess, technician, actor.getId());
        estados.forEach(row ->
            porEstado.put(((Incidencia.EstadoIncidencia) row[0]).name(), ((Number) row[1]).longValue()));

        Map<String, Long> porCategoria = toMap(actor == null
            ? incidenciaRepository.contarPorCategoria(desdeHora)
            : incidenciaRepository.contarCategoriasVisibles(
                desdeHora, globalAccess, technician, actor.getId()));
        Map<String, Long> porPrioridad = new LinkedHashMap<>();
        for (Incidencia.NivelPrioridad prioridad : Incidencia.NivelPrioridad.values()) {
            porPrioridad.put(prioridad.name(), 0L);
        }
        var prioridades = actor == null
            ? incidenciaRepository.contarPorPrioridad(desdeHora)
            : incidenciaRepository.contarPrioridadesVisibles(
                desdeHora, globalAccess, technician, actor.getId());
        prioridades.forEach(row ->
            porPrioridad.put(((Incidencia.NivelPrioridad) row[0]).name(), ((Number) row[1]).longValue()));
        long total = porEstado.values().stream().mapToLong(Long::longValue).sum();
        var actividad = actor == null
            ? incidenciaRepository.contarCreadasYResueltasPorDia(desdeHora, hasta)
            : incidenciaRepository.contarActividadVisiblePorDia(
                desdeHora, hasta, globalAccess, technician, actor.getId());
        List<ReporteIncidenciasDTO.SerieDiaria> serie = actividad.stream()
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
