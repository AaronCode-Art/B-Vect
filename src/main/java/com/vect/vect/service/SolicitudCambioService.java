package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.SolicitudCambioRequest;
import com.vect.vect.dto.response.SolicitudCambioDTO;
import com.vect.vect.entity.*;
import com.vect.vect.mapper.SolicitudCambioMapper;
import com.vect.vect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Service
public class SolicitudCambioService {

    private static final BigDecimal UMBRAL_APROBACION_GERENCIA = new BigDecimal("1500");

    private final SolicitudCambioRepository solicitudRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final ComponenteRepository componenteRepository;
    private final ActivoRepository activoRepository;
    private final SolicitudCambioMapper solicitudMapper;
    private final DatabaseActorContext databaseActorContext;

    public SolicitudCambioService(SolicitudCambioRepository solicitudRepository,
                                  IncidenciaRepository incidenciaRepository,
                                  ComponenteRepository componenteRepository,
                                  ActivoRepository activoRepository,
                                  SolicitudCambioMapper solicitudMapper,
                                  DatabaseActorContext databaseActorContext) {
        this.solicitudRepository = solicitudRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.componenteRepository = componenteRepository;
        this.activoRepository = activoRepository;
        this.solicitudMapper = solicitudMapper;
        this.databaseActorContext = databaseActorContext;
    }

    @Transactional(readOnly = true)
    public List<SolicitudCambioDTO> listar(Usuario actor) {
        List<SolicitudCambio> solicitudes = switch (actor.getRol()) {
            case ADMIN, GERENCIA, SUPERVISOR -> solicitudRepository.findAllByOrderByCreadoEnDesc();
            case TECNICO -> solicitudRepository.findAllBySolicitadoPor_IdOrderByCreadoEnDesc(actor.getId());
            case EMPLEADO -> throw ApiException.forbidden("ACCESO_DENEGADO",
                "Los empleados no tienen acceso a las solicitudes de cambio");
        };
        return solicitudes.stream().map(solicitudMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudCambioDTO obtener(UUID id, Usuario actor) {
        SolicitudCambio solicitud = buscar(id);
        verificarVisibilidad(solicitud, actor);
        return solicitudMapper.toDto(solicitud);
    }

    @Transactional
    public SolicitudCambioDTO crear(SolicitudCambioRequest request, Usuario solicitante) {
        Incidencia incidencia = incidenciaRepository.findById(request.incidenciaId())
            .orElseThrow(() -> ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada"));

        if (solicitante.getRol() == Usuario.RolUsuario.TECNICO
                && (incidencia.getTecnicoAsignado() == null
                    || !incidencia.getTecnicoAsignado().getId().equals(solicitante.getId()))) {
            throw ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada");
        }
        if (request.componenteId() == null && request.activoId() == null) {
            throw ApiException.badRequest("DESTINO_REQUERIDO",
                "La solicitud debe referenciar un componente o un activo");
        }

        Componente componente = request.componenteId() != null
            ? componenteRepository.findById(request.componenteId())
                .orElseThrow(() -> ApiException.notFound("COMPONENTE_NO_ENCONTRADO", "Componente no encontrado"))
            : null;

        Activo activo = request.activoId() != null
            ? activoRepository.findById(request.activoId())
                .orElseThrow(() -> ApiException.notFound("ACTIVO_NO_ENCONTRADO", "Activo no encontrado"))
            : null;

        SolicitudCambio solicitud = new SolicitudCambio();
        solicitud.setCodigo(generarCodigo());
        solicitud.setIncidencia(incidencia);
        solicitud.setSolicitadoPor(solicitante);
        solicitud.setComponente(componente);
        solicitud.setActivo(activo);
        solicitud.setDescripcion(request.descripcion());
        solicitud.setJustificacion(request.justificacion());
        solicitud.setCostoEstimado(request.costoEstimado());
        solicitud.setEstado(SolicitudCambio.EstadoSolicitud.PENDIENTE_EVALUACION);

        solicitud = solicitudRepository.save(solicitud);
        return solicitudMapper.toDto(solicitud);
    }

    @Transactional
    public SolicitudCambioDTO evaluar(UUID id, Boolean aprobar, String comentario, String motivoRechazo, Usuario evaluador) {
        SolicitudCambio solicitud = buscar(id);
        verificarVisibilidad(solicitud, evaluador);

        if (solicitud.getEstado() != SolicitudCambio.EstadoSolicitud.PENDIENTE_EVALUACION) {
            throw ApiException.conflict("SOLICITUD_NO_PENDIENTE",
                "La solicitud no está pendiente de evaluación");
        }
        if (aprobar == null) {
            throw ApiException.badRequest("DECISION_REQUERIDA", "Debe indicar si aprueba o rechaza la solicitud");
        }
        if (!aprobar && (motivoRechazo == null || motivoRechazo.isBlank())) {
            throw ApiException.badRequest("MOTIVO_RECHAZO_REQUERIDO",
                "Debe indicar el motivo del rechazo");
        }

        databaseActorContext.setActor(evaluador);
        if (aprobar) {
            if (solicitud.getCostoEstimado().compareTo(UMBRAL_APROBACION_GERENCIA) >= 0) {
                solicitud.setEstado(SolicitudCambio.EstadoSolicitud.PENDIENTE_GERENCIA);
            } else {
                solicitud.setEstado(SolicitudCambio.EstadoSolicitud.APROBADA);
            }
        } else {
            solicitud.setEstado(SolicitudCambio.EstadoSolicitud.RECHAZADA);
        }

        solicitud.setEvaluadoPor(evaluador);
        solicitud.setEvaluadoEn(java.time.LocalDateTime.now());
        solicitud.setComentarioEvaluacion(aprobar ? comentario
            : (motivoRechazo == null || motivoRechazo.isBlank() ? comentario : motivoRechazo));

        solicitud = solicitudRepository.save(solicitud);
        return solicitudMapper.toDto(solicitud);
    }

    @Transactional
    public SolicitudCambioDTO aprobarGerencia(UUID id, String comentario, Usuario gerente) {
        SolicitudCambio solicitud = buscar(id);
        verificarVisibilidad(solicitud, gerente);

        if (solicitud.getEstado() != SolicitudCambio.EstadoSolicitud.PENDIENTE_GERENCIA) {
            throw ApiException.conflict("SOLICITUD_NO_PENDIENTE_GERENCIA",
                "La solicitud no está pendiente de aprobación de gerencia");
        }

        databaseActorContext.setActor(gerente);
        solicitud.setEstado(SolicitudCambio.EstadoSolicitud.APROBADA);
        solicitud.setAprobadoGerenciaPor(gerente);
        solicitud.setAprobadoGerenciaEn(java.time.LocalDateTime.now());
        solicitud.setComentarioGerencia(comentario);

        solicitud = solicitudRepository.save(solicitud);
        return solicitudMapper.toDto(solicitud);
    }

    @Transactional
    public SolicitudCambioDTO rechazarGerencia(UUID id, String motivo, Usuario gerente) {
        SolicitudCambio solicitud = buscar(id);
        verificarVisibilidad(solicitud, gerente);
        if (motivo == null || motivo.isBlank()) {
            throw ApiException.badRequest("MOTIVO_RECHAZO_REQUERIDO",
                "Debe indicar el motivo del rechazo");
        }
        if (solicitud.getEstado() != SolicitudCambio.EstadoSolicitud.PENDIENTE_GERENCIA) {
            throw ApiException.conflict("SOLICITUD_NO_PENDIENTE_GERENCIA",
                "La solicitud no está pendiente de aprobación de gerencia");
        }
        databaseActorContext.setActor(gerente);
        solicitud.setEstado(SolicitudCambio.EstadoSolicitud.RECHAZADA);
        solicitud.setAprobadoGerenciaPor(gerente);
        solicitud.setAprobadoGerenciaEn(java.time.LocalDateTime.now());
        solicitud.setComentarioGerencia(motivo.trim());
        return solicitudMapper.toDto(solicitudRepository.save(solicitud));
    }

    @Transactional
    public SolicitudCambioDTO ejecutar(UUID id, Usuario tecnico) {
        SolicitudCambio solicitud = buscar(id);
        verificarVisibilidad(solicitud, tecnico);

        if (solicitud.getEstado() != SolicitudCambio.EstadoSolicitud.APROBADA) {
            throw ApiException.conflict("SOLICITUD_NO_APROBADA", "La solicitud no está aprobada");
        }

        databaseActorContext.setActor(tecnico);
        solicitud.setEstado(SolicitudCambio.EstadoSolicitud.EJECUTADA);
        solicitud.setEjecutadoPor(tecnico);
        solicitud.setEjecutadoEn(java.time.LocalDateTime.now());

        solicitud = solicitudRepository.save(solicitud);
        return solicitudMapper.toDto(solicitud);
    }

    private String generarCodigo() {
        return "SOL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    private SolicitudCambio buscar(UUID id) {
        return solicitudRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("SOLICITUD_NO_ENCONTRADA", "Solicitud no encontrada"));
    }

    private void verificarVisibilidad(SolicitudCambio solicitud, Usuario actor) {
        boolean visible = switch (actor.getRol()) {
            case ADMIN, GERENCIA, SUPERVISOR -> true;
            case TECNICO -> solicitud.getSolicitadoPor().getId().equals(actor.getId());
            case EMPLEADO -> false;
        };
        if (!visible) {
            throw ApiException.notFound("SOLICITUD_NO_ENCONTRADA", "Solicitud no encontrada");
        }
    }
}
