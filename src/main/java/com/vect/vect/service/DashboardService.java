package com.vect.vect.service;

import com.vect.vect.dto.response.DashboardResumenDTO;
import com.vect.vect.entity.*;
import com.vect.vect.repository.*;
import org.springframework.stereotype.Service;

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

    public DashboardResumenDTO resumen() {
        long total = incidenciaRepository.count();
        long abiertas = incidenciaRepository.findAll().stream()
                .filter(i -> i.getEstado() == Incidencia.EstadoIncidencia.REGISTRADA
                        || i.getEstado() == Incidencia.EstadoIncidencia.ASIGNADA)
                .count();
        long enProceso = incidenciaRepository.findAll().stream()
                .filter(i -> i.getEstado() == Incidencia.EstadoIncidencia.EN_ATENCION
                        || i.getEstado() == Incidencia.EstadoIncidencia.EN_ESPERA_USUARIO
                        || i.getEstado() == Incidencia.EstadoIncidencia.EN_ESPERA_REPUESTO)
                .count();
        long resueltas = incidenciaRepository.findAll().stream()
                .filter(i -> i.getEstado() == Incidencia.EstadoIncidencia.RESUELTA)
                .count();
        long cerradas = incidenciaRepository.findAll().stream()
                .filter(i -> i.getEstado() == Incidencia.EstadoIncidencia.CERRADA)
                .count();

        long tecnicos = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Usuario.RolUsuario.TECNICO && u.getActivo())
                .count();

        long solicitudesPendientes = solicitudRepository.findAll().stream()
                .filter(s -> s.getEstado() == SolicitudCambio.EstadoSolicitud.PENDIENTE_EVALUACION
                        || s.getEstado() == SolicitudCambio.EstadoSolicitud.PENDIENTE_GERENCIA)
                .count();

        long componentesBajoStock = componenteRepository.findAll().stream()
                .filter(c -> c.getStockActual() <= c.getStockMinimo())
                .count();

        Map<String, Long> porMacroEstado = new HashMap<>();
        porMacroEstado.put("PENDIENTE", abiertas);
        porMacroEstado.put("EN_PROCESO", enProceso);
        porMacroEstado.put("RESUELTO", resueltas + cerradas);

        return new DashboardResumenDTO(total, abiertas, enProceso, resueltas, cerradas, 0, tecnicos,
                solicitudesPendientes, componentesBajoStock, porMacroEstado);
    }
}
