package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.entity.Usuario;
import com.vect.vect.repository.IncidenciaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

class ReporteServiceTest {

    @Test
    void rejectsUnsupportedReportRanges() {
        IncidenciaRepository repository = mock(IncidenciaRepository.class);
        ReporteService service = new ReporteService(repository);

        assertThatThrownBy(() -> service.generar(0)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> service.generar(367)).isInstanceOf(ApiException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void createsScopedDashboardActivityUsingAggregatesInsteadOfIncidentRows() {
        IncidenciaRepository repository = mock(IncidenciaRepository.class);
        Usuario actor = new Usuario();
        actor.setId(UUID.randomUUID());
        actor.setRol(Usuario.RolUsuario.EMPLEADO);
        ReporteService service = new ReporteService(repository);

        when(repository.contarEstadosVisibles(any(), eq(false), eq(false), eq(actor.getId())))
            .thenReturn(List.of());
        when(repository.contarCategoriasVisibles(any(), eq(false), eq(false), eq(actor.getId())))
            .thenReturn(List.of());
        when(repository.contarPrioridadesVisibles(any(), eq(false), eq(false), eq(actor.getId())))
            .thenReturn(List.of());
        when(repository.contarActividadVisiblePorDia(
                any(), any(), eq(false), eq(false), eq(actor.getId())))
            .thenReturn(List.of());

        var report = service.generar(7, actor);

        assertThat(report.total()).isZero();
        assertThat(report.serieDiaria()).isEmpty();
        verify(repository).contarEstadosVisibles(any(), eq(false), eq(false), eq(actor.getId()));
        verify(repository).contarCategoriasVisibles(any(), eq(false), eq(false), eq(actor.getId()));
        verify(repository).contarPrioridadesVisibles(any(), eq(false), eq(false), eq(actor.getId()));
        verify(repository).contarActividadVisiblePorDia(any(), any(), eq(false), eq(false), eq(actor.getId()));
        verifyNoMoreInteractions(repository);
    }
}
