package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.response.HistorialIncidenciaDTO;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.HistorialIncidenciaMapper;
import com.vect.vect.repository.HistorialIncidenciaRepository;
import com.vect.vect.repository.IncidenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HistorialIncidenciaService {

    private final HistorialIncidenciaRepository historialRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final HistorialIncidenciaMapper historialMapper;

    public HistorialIncidenciaService(HistorialIncidenciaRepository historialRepository,
                                      IncidenciaRepository incidenciaRepository,
                                      HistorialIncidenciaMapper historialMapper) {
        this.historialRepository = historialRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.historialMapper = historialMapper;
    }

    @Transactional(readOnly = true)
    public List<HistorialIncidenciaDTO> listar(UUID incidenciaId, Usuario actor) {
        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
            .orElseThrow(() -> ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada"));
        boolean permitido = switch (actor.getRol()) {
            case ADMIN, GERENCIA, SUPERVISOR -> true;
            case TECNICO -> incidencia.getTecnicoAsignado() != null
                && incidencia.getTecnicoAsignado().getId().equals(actor.getId());
            case EMPLEADO -> incidencia.getReportante().getId().equals(actor.getId());
        };
        if (!permitido) {
            throw ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada");
        }
        return historialRepository.findAllByIncidencia_IdOrderByCreadoEnDesc(incidenciaId)
            .stream().map(historialMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialIncidenciaDTO> listarDelTecnico(UUID tecnicoId, Usuario actor) {
        if (actor.getRol() != Usuario.RolUsuario.ADMIN && actor.getRol() != Usuario.RolUsuario.GERENCIA
                && actor.getRol() != Usuario.RolUsuario.SUPERVISOR
                && !(actor.getRol() == Usuario.RolUsuario.TECNICO && actor.getId().equals(tecnicoId))) {
            throw ApiException.forbidden("ACCESO_DENEGADO", "No tiene permiso para consultar este historial");
        }
        return historialRepository.findAllByIncidencia_TecnicoAsignado_IdOrderByCreadoEnDesc(tecnicoId)
            .stream().map(historialMapper::toDto).toList();
    }
}
