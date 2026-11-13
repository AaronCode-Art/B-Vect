package com.vect.vect.service;

import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.SolicitudCambio;
import com.vect.vect.entity.Usuario;
import com.vect.vect.repository.ComponenteRepository;
import com.vect.vect.repository.IncidenciaRepository;
import com.vect.vect.repository.SolicitudCambioRepository;
import com.vect.vect.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    @Test
    void buildsDashboardMetricsFromAggregateQueriesWithoutLoadingEntities() {
        IncidenciaRepository incidencias = mock(IncidenciaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        SolicitudCambioRepository solicitudes = mock(SolicitudCambioRepository.class);
        ComponenteRepository componentes = mock(ComponenteRepository.class);
        when(incidencias.contarTotalPorEstado()).thenReturn(List.of(
            new Object[]{Incidencia.EstadoIncidencia.REGISTRADA, 2L},
            new Object[]{Incidencia.EstadoIncidencia.ASIGNADA, 3L},
            new Object[]{Incidencia.EstadoIncidencia.EN_ATENCION, 4L},
            new Object[]{Incidencia.EstadoIncidencia.EN_ESPERA_USUARIO, 1L},
            new Object[]{Incidencia.EstadoIncidencia.RESUELTA, 5L},
            new Object[]{Incidencia.EstadoIncidencia.CERRADA, 6L}
        ));
        when(usuarios.countByRolAndActivoTrue(Usuario.RolUsuario.TECNICO)).thenReturn(7L);
        when(solicitudes.countByEstadoIn(List.of(
            SolicitudCambio.EstadoSolicitud.PENDIENTE_EVALUACION,
            SolicitudCambio.EstadoSolicitud.PENDIENTE_GERENCIA))).thenReturn(8L);
        when(componentes.countBajoStock()).thenReturn(9L);

        var result = new DashboardService(incidencias, usuarios, solicitudes, componentes).resumen();

        assertThat(result.totalIncidencias()).isEqualTo(21);
        assertThat(result.abiertas()).isEqualTo(5);
        assertThat(result.enProceso()).isEqualTo(5);
        assertThat(result.resueltas()).isEqualTo(5);
        assertThat(result.cerradas()).isEqualTo(6);
        assertThat(result.tecnicosDisponibles()).isEqualTo(7);
        assertThat(result.solicitudesPendientes()).isEqualTo(8);
        assertThat(result.componentesBajoStock()).isEqualTo(9);
        verify(incidencias, never()).findAll();
        verify(usuarios, never()).findAll();
        verify(solicitudes, never()).findAll();
        verify(componentes, never()).findAll();
    }
}
