package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.response.MensajeChatDTO;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.MensajeIncidencia;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.MensajeChatMapper;
import com.vect.vect.repository.IncidenciaRepository;
import com.vect.vect.repository.MensajeIncidenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensajeIncidenciaServiceTest {

    @Mock private MensajeIncidenciaRepository mensajeRepository;
    @Mock private IncidenciaRepository incidenciaRepository;
    @Mock private MensajeChatMapper mensajeMapper;

    private MensajeIncidenciaService service;
    private UUID incidenciaId;
    private Usuario reportante;
    private Usuario tecnico;
    private Incidencia incidencia;

    @BeforeEach
    void setUp() {
        service = new MensajeIncidenciaService(mensajeRepository, incidenciaRepository, mensajeMapper);
        incidenciaId = UUID.randomUUID();
        reportante = usuario(Usuario.RolUsuario.EMPLEADO);
        tecnico = usuario(Usuario.RolUsuario.TECNICO);
        incidencia = new Incidencia();
        incidencia.setId(incidenciaId);
        incidencia.setEstado(Incidencia.EstadoIncidencia.EN_ATENCION);
        incidencia.setReportante(reportante);
        incidencia.setTecnicoAsignado(tecnico);
        when(incidenciaRepository.findById(incidenciaId)).thenReturn(Optional.of(incidencia));
    }

    @Test
    void allowsTheIncidentReporterAndAssignedTechnicianToReadPrivateChat() {
        when(mensajeRepository.findAllByIncidencia_IdAndCanalOrderByCreadoEnAsc(
            incidenciaId, MensajeIncidencia.CanalMensaje.EMPLEADO_TECNICO)).thenReturn(List.of());

        assertThat(service.listarMensajes(incidenciaId,
            MensajeIncidencia.CanalMensaje.EMPLEADO_TECNICO, reportante)).isEmpty();
        assertThat(service.listarMensajes(incidenciaId,
            MensajeIncidencia.CanalMensaje.EMPLEADO_TECNICO, tecnico)).isEmpty();
    }

    @Test
    void deniesOtherUsersAndTheOperationalGroupChannel() {
        Usuario supervisor = usuario(Usuario.RolUsuario.SUPERVISOR);

        assertThatThrownBy(() -> service.listarMensajes(incidenciaId,
            MensajeIncidencia.CanalMensaje.EMPLEADO_TECNICO, supervisor))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Solo quien reportó la incidencia");

        assertThatThrownBy(() -> service.listarMensajes(incidenciaId,
            MensajeIncidencia.CanalMensaje.GRUPO_OPERATIVO, reportante))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Solo quien reportó la incidencia");

        verifyNoInteractions(mensajeRepository);
    }

    @Test
    void hidesInternalMessagesFromTheParticipantChat() {
        MensajeIncidencia publicMessage = mensaje(MensajeIncidencia.VisibilidadMensaje.PUBLICO);
        MensajeIncidencia internalMessage = mensaje(MensajeIncidencia.VisibilidadMensaje.INTERNO);
        MensajeChatDTO publicDto = mock(MensajeChatDTO.class);
        when(mensajeRepository.findAllByIncidencia_IdAndCanalOrderByCreadoEnAsc(
            incidenciaId, MensajeIncidencia.CanalMensaje.EMPLEADO_TECNICO))
            .thenReturn(List.of(publicMessage, internalMessage));
        when(mensajeMapper.toDto(publicMessage)).thenReturn(publicDto);

        assertThat(service.listarMensajes(incidenciaId,
            MensajeIncidencia.CanalMensaje.EMPLEADO_TECNICO, reportante))
            .containsExactly(publicDto);
        verify(mensajeMapper, never()).toDto(internalMessage);
    }

    private MensajeIncidencia mensaje(MensajeIncidencia.VisibilidadMensaje visibilidad) {
        MensajeIncidencia mensaje = new MensajeIncidencia();
        mensaje.setVisibilidad(visibilidad);
        return mensaje;
    }

    private Usuario usuario(Usuario.RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }
}
