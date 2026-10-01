package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.response.IncidenciaDTO;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.IncidenciaMapper;
import com.vect.vect.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidenciaServiceTest {

    @Mock private IncidenciaRepository incidenciaRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private UbicacionRepository ubicacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HistorialIncidenciaRepository historialIncidenciaRepository;
    @Mock private IncidenciaMapper incidenciaMapper;
    @Mock private DatabaseActorContext databaseActorContext;

    private IncidenciaService service;

    @BeforeEach
    void setUp() {
        service = new IncidenciaService(incidenciaRepository, categoriaRepository, ubicacionRepository,
            usuarioRepository, historialIncidenciaRepository, incidenciaMapper, databaseActorContext);
    }

    @Test
    void changesStateOnlyAfterDatabaseTransitionAndSetsTriggerContext() {
        UUID incidenciaId = UUID.randomUUID();
        Usuario supervisor = usuario(Usuario.RolUsuario.SUPERVISOR);
        Incidencia incidencia = new Incidencia();
        incidencia.setId(incidenciaId);
        incidencia.setEstado(Incidencia.EstadoIncidencia.REGISTRADA);
        when(incidenciaRepository.findById(incidenciaId)).thenReturn(Optional.of(incidencia));
        when(incidenciaRepository.existeTransicion("SUPERVISOR", "REGISTRADA", "CERRADA")).thenReturn(true);
        when(incidenciaRepository.save(incidencia)).thenReturn(incidencia);
        when(incidenciaMapper.toDto(incidencia)).thenReturn(null);

        service.cambiarEstado(incidenciaId, Incidencia.EstadoIncidencia.CERRADA, "Duplicada", supervisor);

        assertThat(incidencia.getEstado()).isEqualTo(Incidencia.EstadoIncidencia.CERRADA);
        verify(databaseActorContext).setTransition(supervisor, "Duplicada");
        verify(historialIncidenciaRepository, never()).save(any());
    }

    @Test
    void rejectsTransitionsNotPresentInDatabasePolicy() {
        UUID incidenciaId = UUID.randomUUID();
        Usuario empleado = usuario(Usuario.RolUsuario.EMPLEADO);
        Incidencia incidencia = new Incidencia();
        incidencia.setId(incidenciaId);
        incidencia.setEstado(Incidencia.EstadoIncidencia.REGISTRADA);
        incidencia.setReportante(empleado);
        when(incidenciaRepository.findById(incidenciaId)).thenReturn(Optional.of(incidencia));

        assertThatThrownBy(() -> service.cambiarEstado(incidenciaId,
            Incidencia.EstadoIncidencia.CERRADA, "Cierre", empleado))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Los empleados no pueden cerrar incidencias");

        verify(incidenciaRepository, never()).save(any());
        verify(incidenciaRepository, never()).existeTransicion(any(), any(), any());
        verifyNoInteractions(databaseActorContext);
    }

    @Test
    void calculatesPriorityFromImpactAndUrgencyWithoutInflatingLowValues() {
        assertThat(IncidenciaService.calcularPrioridad(
            Incidencia.NivelPrioridad.BAJO, Incidencia.NivelPrioridad.BAJO))
            .isEqualTo(Incidencia.NivelPrioridad.BAJO);
        assertThat(IncidenciaService.calcularPrioridad(
            Incidencia.NivelPrioridad.MEDIO, Incidencia.NivelPrioridad.MEDIO))
            .isEqualTo(Incidencia.NivelPrioridad.MEDIO);
        assertThat(IncidenciaService.calcularPrioridad(
            Incidencia.NivelPrioridad.CRITICO, Incidencia.NivelPrioridad.CRITICO))
            .isEqualTo(Incidencia.NivelPrioridad.CRITICO);
    }

    @Test
    void formatsIncidentCodeAsPrefixAndFourDigitNumber() {
        assertThat(IncidenciaService.formatearCodigo(22)).isEqualTo("INC-0022");
    }

    @Test
    void preservesAllDigitsWhenIncidentSequenceExceedsFourDigits() {
        assertThat(IncidenciaService.formatearCodigo(10000)).isEqualTo("INC-10000");
    }

    @Test
    void employeeCannotReadAnotherUsersIncidence() {
        UUID incidenciaId = UUID.randomUUID();
        Usuario actor = usuario(Usuario.RolUsuario.EMPLEADO);
        Usuario reportante = usuario(Usuario.RolUsuario.EMPLEADO);
        Incidencia incidencia = new Incidencia();
        incidencia.setId(incidenciaId);
        incidencia.setReportante(reportante);
        when(incidenciaRepository.findById(incidenciaId)).thenReturn(Optional.of(incidencia));

        assertThatThrownBy(() -> service.obtener(incidenciaId, actor))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Incidencia no encontrada");
        verifyNoInteractions(incidenciaMapper);
    }

    private Usuario usuario(Usuario.RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }
}
