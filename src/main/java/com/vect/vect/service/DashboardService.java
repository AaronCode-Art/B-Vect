package com.vect.vect.service;

import com.vect.vect.dto.response.DashboardResumenDTO;
import com.vect.vect.entity.*;
import com.vect.vect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudCambioRepository solicitudRepository;
    private final ComponenteRepository componenteRepository;

    public DashboardService(IncidenciaRepository incidenciaRepository,
            UsuarioRepository usuarioRepository,
            SolicitudCambioRepository solicitudRepository,
            ComponenteRepository componenteRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
        this.componenteRepository = componenteRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResumenDTO resumen() {
        Map<Incidencia.EstadoIncidencia, Long> counts = new EnumMap<>(Incidencia.EstadoIncidencia.class);
        incidenciaRepository.contarTotalPorEstado().forEach(row ->
                counts.put((Incidencia.EstadoIncidencia) row[0], ((Number) row[1]).longValue()));
        for (Incidencia.EstadoIncidencia estado : Incidencia.EstadoIncidencia.values()) {
            counts.putIfAbsent(estado, 0L);
        }

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long abiertas = count(counts, Incidencia.EstadoIncidencia.REGISTRADA)
                + count(counts, Incidencia.EstadoIncidencia.ASIGNADA);
        long enProceso = count(counts, Incidencia.EstadoIncidencia.EN_ATENCION)
                + count(counts, Incidencia.EstadoIncidencia.EN_ESPERA_USUARIO)
                + count(counts, Incidencia.EstadoIncidencia.EN_ESPERA_REPUESTO);
        long resueltas = count(counts, Incidencia.EstadoIncidencia.RESUELTA);
        long cerradas = count(counts, Incidencia.EstadoIncidencia.CERRADA);
        long tecnicos = usuarioRepository.countByRolAndActivoTrue(Usuario.RolUsuario.TECNICO);
        long solicitudesPendientes = solicitudRepository.countByEstadoIn(java.util.List.of(
                SolicitudCambio.EstadoSolicitud.PENDIENTE_EVALUACION,
                SolicitudCambio.EstadoSolicitud.PENDIENTE_GERENCIA));
        long componentesBajoStock = componenteRepository.countBajoStock();

        Map<String, Long> porMacroEstado = new HashMap<>();
        porMacroEstado.put("PENDIENTE", abiertas);
        porMacroEstado.put("EN_PROCESO", enProceso);
        porMacroEstado.put("RESUELTO", resueltas + cerradas);

        return new DashboardResumenDTO(total, abiertas, enProceso, resueltas, cerradas, 0, tecnicos,
                solicitudesPendientes, componentesBajoStock, porMacroEstado);
    }

    private long count(Map<Incidencia.EstadoIncidencia, Long> counts, Incidencia.EstadoIncidencia estado) {
        return counts.getOrDefault(estado, 0L);
    }
}
