package com.vect.vect.service;

import com.vect.vect.entity.SolicitudCambio;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.SolicitudCambioMapper;
import com.vect.vect.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudCambioServiceTest {

    @Mock private SolicitudCambioRepository solicitudRepository;
    @Mock private IncidenciaRepository incidenciaRepository;
    @Mock private ComponenteRepository componenteRepository;
    @Mock private ActivoRepository activoRepository;
    @Mock private SolicitudCambioMapper solicitudMapper;
    @Mock private DatabaseActorContext databaseActorContext;

    private SolicitudCambioService service;

    @BeforeEach
    void setUp() {
        service = new SolicitudCambioService(solicitudRepository, incidenciaRepository,
            componenteRepository, activoRepository, solicitudMapper, databaseActorContext);
    }

    @Test
    void approvalUnderThresholdGoesDirectlyToApproved() {
        UUID id = UUID.randomUUID();
        Usuario supervisor = usuario(Usuario.RolUsuario.SUPERVISOR);
        SolicitudCambio solicitud = solicitud(id, "1499.99");
        when(solicitudRepository.findById(id)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);
        when(solicitudMapper.toDto(solicitud)).thenReturn(null);

        service.evaluar(id, true, "Aprobada", null, supervisor);

        assertThat(solicitud.getEstado()).isEqualTo(SolicitudCambio.EstadoSolicitud.APROBADA);
        verify(databaseActorContext).setActor(supervisor);
    }

    @Test
    void approvalAtThresholdRequiresManagementApproval() {
        UUID id = UUID.randomUUID();
        Usuario supervisor = usuario(Usuario.RolUsuario.SUPERVISOR);
        SolicitudCambio solicitud = solicitud(id, "1500.00");
        when(solicitudRepository.findById(id)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);
        when(solicitudMapper.toDto(solicitud)).thenReturn(null);

        service.evaluar(id, true, "Evaluada", null, supervisor);

        assertThat(solicitud.getEstado()).isEqualTo(SolicitudCambio.EstadoSolicitud.PENDIENTE_GERENCIA);
    }

    private SolicitudCambio solicitud(UUID id, String cost) {
        SolicitudCambio solicitud = new SolicitudCambio();
        solicitud.setId(id);
        solicitud.setCostoEstimado(new BigDecimal(cost));
        solicitud.setEstado(SolicitudCambio.EstadoSolicitud.PENDIENTE_EVALUACION);
        return solicitud;
    }

    private Usuario usuario(Usuario.RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setRol(rol);
        return usuario;
    }
}
