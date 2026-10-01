package com.vect.vect.mapper;

import com.vect.vect.dto.response.IncidenciaDTO;
import com.vect.vect.dto.response.SolicitudCambioDTO;
import com.vect.vect.dto.response.UsuarioDTO;
import com.vect.vect.dto.response.ActivoDTO;
import com.vect.vect.dto.response.AuthResponse;
import com.vect.vect.dto.response.EvidenciaDTO;
import com.vect.vect.dto.response.MensajeChatDTO;
import com.vect.vect.entity.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTests {

    private final IncidenciaMapper incidenciaMapper = Mappers.getMapper(IncidenciaMapper.class);
    private final SolicitudCambioMapper solicitudMapper = Mappers.getMapper(SolicitudCambioMapper.class);
    private final UsuarioMapper usuarioMapper = Mappers.getMapper(UsuarioMapper.class);
    private final ActivoMapper activoMapper = Mappers.getMapper(ActivoMapper.class);
    private final AuthMapper authMapper = Mappers.getMapper(AuthMapper.class);
    private final EvidenciaMapper evidenciaMapper = Mappers.getMapper(EvidenciaMapper.class);
    private final MensajeChatMapper mensajeMapper = Mappers.getMapper(MensajeChatMapper.class);

    @Test
    void mapsIncidenciaAndNullableRelations() {
        Incidencia incidencia = new Incidencia();
        incidencia.setId(UUID.randomUUID());
        incidencia.setCanal(Incidencia.CanalIncidencia.PORTAL_WEB);
        incidencia.setImpacto(Incidencia.NivelPrioridad.ALTO);
        incidencia.setUrgencia(Incidencia.NivelPrioridad.MEDIO);
        incidencia.setPrioridad(Incidencia.NivelPrioridad.ALTO);
        incidencia.setEstado(Incidencia.EstadoIncidencia.ASIGNADA);
        incidencia.setCategoria(null);
        incidencia.setUbicacion(null);
        incidencia.setReportante(null);
        incidencia.setTecnicoAsignado(null);

        IncidenciaDTO dto = incidenciaMapper.toDto(incidencia);

        assertThat(dto.id()).isEqualTo(incidencia.getId());
        assertThat(dto.canal()).isEqualTo("PORTAL_WEB");
        assertThat(dto.prioridad()).isEqualTo("ALTO");
        assertThat(dto.estado()).isEqualTo("ASIGNADA");
        assertThat(dto.categoriaNombre()).isNull();
        assertThat(dto.reportanteNombre()).isNull();
    }

    @Test
    void mapsSolicitudCambioReferences() {
        Incidencia incidencia = new Incidencia();
        incidencia.setId(UUID.randomUUID());
        incidencia.setCodigo("INC-TEST");
        Usuario tecnico = new Usuario();
        tecnico.setNombres("Ada");
        tecnico.setApellidos("Lovelace");
        SolicitudCambio solicitud = new SolicitudCambio();
        solicitud.setId(UUID.randomUUID());
        solicitud.setIncidencia(incidencia);
        solicitud.setSolicitadoPor(tecnico);
        solicitud.setCostoEstimado(new BigDecimal("125.50"));
        solicitud.setEstado(SolicitudCambio.EstadoSolicitud.PENDIENTE_EVALUACION);

        SolicitudCambioDTO dto = solicitudMapper.toDto(solicitud);

        assertThat(dto.incidenciaCodigo()).isEqualTo("INC-TEST");
        assertThat(dto.tipo()).isEqualTo("EQUIPO");
        assertThat(dto.solicitadoPorNombre()).isEqualTo("Ada Lovelace");
        assertThat(dto.estado()).isEqualTo("PENDIENTE_EVALUACION");
    }

    @Test
    void usuarioDtoNeverContainsPasswordHash() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setCorreo("persona@example.com");
        usuario.setRol(Usuario.RolUsuario.EMPLEADO);
        usuario.setHashContrasena("do-not-return-this");

        UsuarioDTO dto = usuarioMapper.toDto(usuario);

        assertThat(dto.correo()).isEqualTo("persona@example.com");
        assertThat(dto.rol()).isEqualTo("EMPLEADO");
        assertThat(dto.toString()).doesNotContain("do-not-return-this");
    }

    @Test
    void activoMapperDoesNotRequireOptionalRelations() {
        Activo activo = new Activo();
        activo.setId(UUID.randomUUID());
        activo.setCodigo("ACT-001");
        activo.setEstado(Activo.EstadoActivo.OPERATIVO);

        ActivoDTO dto = activoMapper.toDto(activo);

        assertThat(dto.codigo()).isEqualTo("ACT-001");
        assertThat(dto.estado()).isEqualTo(Activo.EstadoActivo.OPERATIVO);
        assertThat(dto.usuarioAsignadoId()).isNull();
        assertThat(dto.ubicacionNombre()).isNull();
    }

    @Test
    void mapsAuthUserWithoutPasswordFields() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNombres("Ada");
        usuario.setApellidos("Lovelace");
        usuario.setCorreo("ada@example.com");
        usuario.setRol(Usuario.RolUsuario.TECNICO);
        usuario.setHashContrasena("secret-hash");

        AuthResponse.UsuarioDTO dto = authMapper.toDto(usuario);

        assertThat(dto.nombres()).isEqualTo("Ada");
        assertThat(dto.rol()).isEqualTo("TECNICO");
        assertThat(dto.toString()).doesNotContain("secret-hash");
    }

    @Test
    void mapsEvidenceWithProvidedExternalUrl() {
        Evidencia evidencia = new Evidencia();
        evidencia.setId(UUID.randomUUID());
        evidencia.setNombreArchivo("foto.png");

        EvidenciaDTO dto = evidenciaMapper.toDto(evidencia, "https://example.com/foto.png");

        assertThat(dto.nombreArchivo()).isEqualTo("foto.png");
        assertThat(dto.url()).isEqualTo("https://example.com/foto.png");
        assertThat(dto.incidenciaId()).isNull();
    }

    @Test
    void mapsIncidentChatMessage() {
        Incidencia incidencia = new Incidencia();
        incidencia.setId(UUID.randomUUID());
        Usuario remitente = new Usuario();
        remitente.setNombres("Ada");
        remitente.setApellidos("Lovelace");
        MensajeIncidencia mensaje = new MensajeIncidencia();
        mensaje.setId(UUID.randomUUID());
        mensaje.setIncidencia(incidencia);
        mensaje.setRemitente(remitente);
        mensaje.setCanal(MensajeIncidencia.CanalMensaje.EMPLEADO_TECNICO);
        mensaje.setVisibilidad(MensajeIncidencia.VisibilidadMensaje.PUBLICO);
        mensaje.setCuerpo("Hola");

        MensajeChatDTO dto = mensajeMapper.toDto(mensaje);

        assertThat(dto.incidenciaId()).isEqualTo(incidencia.getId());
        assertThat(dto.remitenteNombre()).isEqualTo("Ada Lovelace");
        assertThat(dto.contenido()).isEqualTo("Hola");
        assertThat(dto.visibilidad()).isEqualTo("PUBLICO");
    }
}
