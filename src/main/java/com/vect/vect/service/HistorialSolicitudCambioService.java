package com.vect.vect.service;

import com.vect.vect.dto.response.HistorialSolicitudCambioDTO;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.HistorialSolicitudCambioMapper;
import com.vect.vect.repository.HistorialSolicitudCambioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HistorialSolicitudCambioService {

    private final HistorialSolicitudCambioRepository repository;
    private final HistorialSolicitudCambioMapper mapper;

    public HistorialSolicitudCambioService(HistorialSolicitudCambioRepository repository,
                                          HistorialSolicitudCambioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<HistorialSolicitudCambioDTO> listar(UUID solicitudId, Usuario actor) {
        if (actor.getRol() != Usuario.RolUsuario.ADMIN
                && actor.getRol() != Usuario.RolUsuario.GERENCIA
                && actor.getRol() != Usuario.RolUsuario.SUPERVISOR) {
            throw ApiException.forbidden("ACCESO_DENEGADO",
                "No tiene permiso para consultar el historial de aprobaciones");
        }
        var registros = solicitudId == null
            ? repository.findAllByOrderByCreadoEnDesc()
            : repository.findAllBySolicitud_IdOrderByCreadoEnDesc(solicitudId);
        return registros.stream().map(mapper::toDto).toList();
    }
}
