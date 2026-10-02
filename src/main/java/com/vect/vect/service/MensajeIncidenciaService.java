package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.MensajeChatRequest;
import com.vect.vect.dto.response.MensajeChatDTO;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.MensajeIncidencia;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.MensajeChatMapper;
import com.vect.vect.repository.IncidenciaRepository;
import com.vect.vect.repository.MensajeIncidenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MensajeIncidenciaService {

    private final MensajeIncidenciaRepository mensajeRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final MensajeChatMapper mensajeMapper;

    public MensajeIncidenciaService(MensajeIncidenciaRepository mensajeRepository,
                                    IncidenciaRepository incidenciaRepository,
                                    MensajeChatMapper mensajeMapper) {
        this.mensajeRepository = mensajeRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.mensajeMapper = mensajeMapper;
    }

    @Transactional(readOnly = true)
    public List<MensajeChatDTO> listarMensajes(UUID incidenciaId, MensajeIncidencia.CanalMensaje canal, Usuario actor) {
        Incidencia incidencia = buscarIncidencia(incidenciaId);
        verificarVisibilidad(incidencia, canal, actor);

        return mensajeRepository.findAllByIncidencia_IdAndCanalOrderByCreadoEnAsc(incidenciaId, canal).stream()
            .filter(m -> m.getVisibilidad() == MensajeIncidencia.VisibilidadMensaje.PUBLICO)
            .map(mensajeMapper::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MensajeChatDTO> listarChatTecnicoEmpleado(Usuario actor) {
        if (actor.getRol() != Usuario.RolUsuario.ADMIN
                && actor.getRol() != Usuario.RolUsuario.GERENCIA
                && actor.getRol() != Usuario.RolUsuario.SUPERVISOR) {
            throw ApiException.forbidden("ACCESO_DENEGADO", "No tiene permiso para supervisar este chat");
        }
        return mensajeRepository.findAllByCanalOrderByCreadoEnAsc(
                MensajeIncidencia.CanalMensaje.EMPLEADO_TECNICO).stream()
            .map(mensajeMapper::toDto)
            .toList();
    }

    @Transactional
    public MensajeChatDTO enviarMensaje(UUID incidenciaId, MensajeChatRequest request, Usuario remitente) {
        Incidencia incidencia = buscarIncidencia(incidenciaId);
        verificarVisibilidad(incidencia, request.canal(), remitente);

        if (request.visibilidad() != MensajeIncidencia.VisibilidadMensaje.PUBLICO) {
            throw ApiException.forbidden("MENSAJE_INTERNO_NO_PERMITIDO",
                "El chat de la incidencia solo admite mensajes visibles para sus participantes");
        }
        if (incidencia.getEstado() == Incidencia.EstadoIncidencia.CERRADA) {
            throw ApiException.conflict("CHAT_CERRADO", "El chat está bloqueado porque la incidencia está cerrada");
        }

        MensajeIncidencia mensaje = new MensajeIncidencia();
        mensaje.setIncidencia(incidencia);
        mensaje.setRemitente(remitente);
        mensaje.setCanal(request.canal());
        mensaje.setVisibilidad(request.visibilidad());
        mensaje.setCuerpo(request.contenido());

        mensaje = mensajeRepository.save(mensaje);
        return mensajeMapper.toDto(mensaje);
    }

    private Incidencia buscarIncidencia(UUID id) {
        return incidenciaRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada"));
    }

    private void verificarVisibilidad(Incidencia incidencia, MensajeIncidencia.CanalMensaje canal, Usuario actor) {
        boolean participante = incidencia.getReportante().getId().equals(actor.getId())
            || (incidencia.getTecnicoAsignado() != null
                && incidencia.getTecnicoAsignado().getId().equals(actor.getId()));
        boolean visible = canal == MensajeIncidencia.CanalMensaje.EMPLEADO_TECNICO && participante;
        if (!visible) {
            throw ApiException.forbidden("CHAT_SOLO_PARTICIPANTES",
                "Solo quien reportó la incidencia y el técnico asignado pueden acceder a este chat");
        }
    }
}
