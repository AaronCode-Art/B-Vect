package com.vect.vect.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.ReglaAutomatizacionRequest;
import com.vect.vect.entity.ParametroSistema;
import com.vect.vect.entity.ReglaAutomatizacion;
import com.vect.vect.entity.RegistroAuditoria;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.HistorialSolicitudCambioMapper;
import com.vect.vect.mapper.ParametroSistemaMapper;
import com.vect.vect.mapper.ReglaAutomatizacionMapper;
import com.vect.vect.mapper.RegistroAuditoriaMapper;
import com.vect.vect.repository.HistorialSolicitudCambioRepository;
import com.vect.vect.repository.ParametroSistemaRepository;
import com.vect.vect.repository.ReglaAutomatizacionRepository;
import com.vect.vect.repository.RegistroAuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SchemaBackedServicesTest {

    @Test
    void filtersAuditByDateAndRejectsInvalidPage() {
        RegistroAuditoriaRepository repository = mock(RegistroAuditoriaRepository.class);
        RegistroAuditoriaMapper mapper = mock(RegistroAuditoriaMapper.class);
        when(repository.findAll(
                org.mockito.ArgumentMatchers.<Specification<RegistroAuditoria>>any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
        AuditoriaService service = new AuditoriaService(repository, mapper);

        assertThat(service.listar(null, "usuarios", "PUT", LocalDate.parse("2026-01-01"),
            LocalDate.parse("2026-01-31"), 0, 25).getContent()).isEmpty();
        assertThatThrownBy(() -> service.listar(null, null, null, null, null, 0, 201))
            .isInstanceOf(ApiException.class);
        verify(repository).findAll(
            org.mockito.ArgumentMatchers.<Specification<RegistroAuditoria>>any(), any(Pageable.class));
    }

    @Test
    void persistsSuccessfulWriteMetadataToTheExistingAuditTable() {
        RegistroAuditoriaRepository repository = mock(RegistroAuditoriaRepository.class);
        AuditoriaService service = new AuditoriaService(repository, mock(RegistroAuditoriaMapper.class));
        HttpServletRequest request = mock(HttpServletRequest.class);
        Usuario actor = new Usuario();
        actor.setId(UUID.randomUUID());
        when(request.getRequestURI()).thenReturn("/api/incidencias/" + UUID.randomUUID() + "/estado");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("integration-test");

        service.registrarCambio(request, actor);

        var captor = org.mockito.ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUsuarioId()).isEqualTo(actor.getId());
        assertThat(captor.getValue().getTipoEntidad()).isEqualTo("incidencias");
        assertThat(captor.getValue().getAccion()).startsWith("POST /api/incidencias/");
        assertThat(captor.getValue().getDireccionIp().getHostAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    void preventsAutomationConditionsFromBeingNonObjectJson() {
        ReglaAutomatizacionRepository repository = mock(ReglaAutomatizacionRepository.class);
        ReglaAutomatizacionService service = new ReglaAutomatizacionService(repository,
            mock(ReglaAutomatizacionMapper.class));
        ReglaAutomatizacionRequest request = new ReglaAutomatizacionRequest(
            "Asignar prioridad", null, "INCIDENCIA_CREADA",
            JsonNodeFactory.instance.arrayNode(), JsonNodeFactory.instance.arrayNode(), null, null);
        Usuario actor = new Usuario();

        assertThatThrownBy(() -> service.crear(request, actor))
            .isInstanceOf(ApiException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void onlyUpdatesExistingSystemParameters() {
        ParametroSistemaRepository repository = mock(ParametroSistemaRepository.class);
        ParametroSistemaService service = new ParametroSistemaService(repository,
            mock(ParametroSistemaMapper.class));
        Usuario actor = new Usuario();

        when(repository.findById("correo.soporte")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar("correo.soporte",
            JsonNodeFactory.instance.textNode("support@example.com"), actor))
            .isInstanceOf(ApiException.class);
        verify(repository, never()).save(any(ParametroSistema.class));
    }

    @Test
    void deniesApprovalHistoryToEmployee() {
        HistorialSolicitudCambioRepository repository = mock(HistorialSolicitudCambioRepository.class);
        HistorialSolicitudCambioService service = new HistorialSolicitudCambioService(repository,
            mock(HistorialSolicitudCambioMapper.class));
        Usuario empleado = new Usuario();
        empleado.setRol(Usuario.RolUsuario.EMPLEADO);

        assertThatThrownBy(() -> service.listar(null, empleado))
            .isInstanceOf(ApiException.class);
        verifyNoInteractions(repository);
    }
}
