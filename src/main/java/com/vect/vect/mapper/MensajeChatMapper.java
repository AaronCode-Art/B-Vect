package com.vect.vect.mapper;

import com.vect.vect.dto.response.MensajeChatDTO;
import com.vect.vect.entity.MensajeIncidencia;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MensajeChatMapper {

    @Mapping(target = "incidenciaId", source = "incidencia.id")
    @Mapping(target = "remitenteId", source = "remitente.id")
    @Mapping(target = "remitenteNombre", expression = "java(nombreCompleto(mensaje.getRemitente()))")
    @Mapping(target = "canal", expression = "java(mensaje.getCanal().name())")
    @Mapping(target = "visibilidad", expression = "java(mensaje.getVisibilidad().name())")
    @Mapping(target = "contenido", source = "cuerpo")
    MensajeChatDTO toDto(MensajeIncidencia mensaje);

    default String nombreCompleto(Usuario usuario) {
        return usuario == null ? null : usuario.getNombres() + " " + usuario.getApellidos();
    }
}
