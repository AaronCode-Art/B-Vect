package com.vect.vect.mapper;

import com.vect.vect.dto.response.SolicitudCambioDTO;
import com.vect.vect.entity.SolicitudCambio;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudCambioMapper {

    @Mapping(target = "incidenciaId", source = "incidencia.id")
    @Mapping(target = "incidenciaCodigo", source = "incidencia.codigo")
    @Mapping(target = "tipo", expression = "java(solicitud.getComponente() != null ? \"COMPONENTE\" : \"EQUIPO\")")
    @Mapping(target = "componenteNombre", source = "componente.nombre")
    @Mapping(target = "activoCodigo", source = "activo.codigo")
    @Mapping(target = "solicitadoPorNombre", expression = "java(nombreCompleto(solicitud.getSolicitadoPor()))")
    SolicitudCambioDTO toDto(SolicitudCambio solicitud);

    default String nombreCompleto(Usuario usuario) {
        return usuario == null ? null : usuario.getNombres() + " " + usuario.getApellidos();
    }
}
