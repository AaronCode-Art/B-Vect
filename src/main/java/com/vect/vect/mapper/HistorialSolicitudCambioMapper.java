package com.vect.vect.mapper;

import com.vect.vect.dto.response.HistorialSolicitudCambioDTO;
import com.vect.vect.entity.HistorialSolicitudCambio;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistorialSolicitudCambioMapper {

    @Mapping(target = "solicitudId", source = "solicitud.id")
    @Mapping(target = "solicitudCodigo", source = "solicitud.codigo")
    @Mapping(target = "realizadoPorId", source = "realizadoPor.id")
    @Mapping(target = "realizadoPorNombre", expression = "java(nombreCompleto(historial.getRealizadoPor()))")
    HistorialSolicitudCambioDTO toDto(HistorialSolicitudCambio historial);

    default String nombreCompleto(Usuario usuario) {
        return usuario == null ? null : usuario.getNombres() + " " + usuario.getApellidos();
    }
}
