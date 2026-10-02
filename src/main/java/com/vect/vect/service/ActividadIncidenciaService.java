package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.ActividadRequest;
import com.vect.vect.dto.response.ActividadIncidenciaDTO;
import com.vect.vect.entity.ActividadIncidencia;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.ActividadIncidenciaMapper;
import com.vect.vect.repository.ActividadIncidenciaRepository;
import com.vect.vect.repository.IncidenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ActividadIncidenciaService {

    private final ActividadIncidenciaRepository actividadRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final ActividadIncidenciaMapper actividadMapper;

    public ActividadIncidenciaService(ActividadIncidenciaRepository actividadRepository,
                                      IncidenciaRepository incidenciaRepository,
                                      ActividadIncidenciaMapper actividadMapper) {
        this.actividadRepository = actividadRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.actividadMapper = actividadMapper;
    }

    @Transactional(readOnly = true)
    public List<ActividadIncidenciaDTO> listar(UUID incidenciaId, Usuario actor) {
        Incidencia incidencia = buscarIncidencia(incidenciaId);
        verificarAcceso(incidencia, actor);
        return actividadRepository.findAllByIncidencia_IdOrderByCreadoEnDesc(incidenciaId)
            .stream().map(actividadMapper::toDto).toList();
    }

    @Transactional
    public ActividadIncidenciaDTO registrar(UUID incidenciaId, ActividadRequest request, Usuario tecnico) {
        Incidencia incidencia = buscarIncidencia(incidenciaId);
        if (tecnico.getRol() != Usuario.RolUsuario.TECNICO
                || incidencia.getTecnicoAsignado() == null
                || !incidencia.getTecnicoAsignado().getId().equals(tecnico.getId())) {
            throw ApiException.forbidden("TECNICO_NO_ASIGNADO",
                "Solo el técnico asignado puede registrar actividades en esta incidencia");
        }
        if (incidencia.getEstado() == Incidencia.EstadoIncidencia.CERRADA) {
            throw ApiException.conflict("INCIDENCIA_CERRADA", "No se pueden registrar actividades en una incidencia cerrada");
        }

        ActividadIncidencia actividad = new ActividadIncidencia();
        actividad.setIncidencia(incidencia);
        actividad.setTecnico(tecnico);
        actividad.setTipoActividad(request.tipo().trim());
        actividad.setDescripcion(request.descripcion().trim());
        actividad.setMinutos(request.minutos());
        return actividadMapper.toDto(actividadRepository.save(actividad));
    }

    private Incidencia buscarIncidencia(UUID id) {
        return incidenciaRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada"));
    }

    private void verificarAcceso(Incidencia incidencia, Usuario actor) {
        boolean visible = switch (actor.getRol()) {
            case ADMIN, GERENCIA, SUPERVISOR -> true;
            case TECNICO -> incidencia.getTecnicoAsignado() != null
                && incidencia.getTecnicoAsignado().getId().equals(actor.getId());
            case EMPLEADO -> incidencia.getReportante().getId().equals(actor.getId());
        };
        if (!visible) {
            throw ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada");
        }
    }
}
