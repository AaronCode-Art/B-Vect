package com.vect.vect.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresEnumsTest {

    @Test
    void enumValuesMatchNeonSchema() {
        assertThat(Incidencia.CanalIncidencia.values()).containsExactly(
            Incidencia.CanalIncidencia.PORTAL_WEB,
            Incidencia.CanalIncidencia.CORREO,
            Incidencia.CanalIncidencia.TELEFONO,
            Incidencia.CanalIncidencia.PRESENCIAL);
        assertThat(Incidencia.EstadoIncidencia.values()).containsExactly(
            Incidencia.EstadoIncidencia.REGISTRADA,
            Incidencia.EstadoIncidencia.ASIGNADA,
            Incidencia.EstadoIncidencia.EN_ATENCION,
            Incidencia.EstadoIncidencia.EN_ESPERA_USUARIO,
            Incidencia.EstadoIncidencia.EN_ESPERA_REPUESTO,
            Incidencia.EstadoIncidencia.RESUELTA,
            Incidencia.EstadoIncidencia.CERRADA);
        assertThat(Incidencia.NivelPrioridad.values()).containsExactly(
            Incidencia.NivelPrioridad.BAJO, Incidencia.NivelPrioridad.MEDIO,
            Incidencia.NivelPrioridad.ALTO, Incidencia.NivelPrioridad.CRITICO);
        assertThat(Usuario.RolUsuario.values()).containsExactly(
            Usuario.RolUsuario.ADMIN, Usuario.RolUsuario.GERENCIA, Usuario.RolUsuario.SUPERVISOR,
            Usuario.RolUsuario.TECNICO, Usuario.RolUsuario.EMPLEADO);
        assertThat(Activo.EstadoActivo.values()).containsExactly(
            Activo.EstadoActivo.OPERATIVO, Activo.EstadoActivo.EN_MANTENIMIENTO, Activo.EstadoActivo.BAJA);
        assertThat(SolicitudCambio.EstadoSolicitud.values()).containsExactly(
            SolicitudCambio.EstadoSolicitud.PENDIENTE_EVALUACION,
            SolicitudCambio.EstadoSolicitud.PENDIENTE_GERENCIA,
            SolicitudCambio.EstadoSolicitud.APROBADA,
            SolicitudCambio.EstadoSolicitud.RECHAZADA,
            SolicitudCambio.EstadoSolicitud.EJECUTADA);
        assertThat(MensajeIncidencia.CanalMensaje.values()).containsExactly(
            MensajeIncidencia.CanalMensaje.EMPLEADO_TECNICO, MensajeIncidencia.CanalMensaje.GRUPO_OPERATIVO);
        assertThat(MensajeIncidencia.VisibilidadMensaje.values()).containsExactly(
            MensajeIncidencia.VisibilidadMensaje.PUBLICO, MensajeIncidencia.VisibilidadMensaje.INTERNO);
        assertThat(Conversacion.TipoConversacion.values()).containsExactly(
            Conversacion.TipoConversacion.PRIVADA, Conversacion.TipoConversacion.GRUPAL);
        assertThat(MovimientoStock.TipoMovimiento.values()).containsExactly(
            MovimientoStock.TipoMovimiento.ENTRADA, MovimientoStock.TipoMovimiento.SALIDA,
            MovimientoStock.TipoMovimiento.AJUSTE);
    }
}
