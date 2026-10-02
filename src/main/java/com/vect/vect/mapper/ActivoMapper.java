package com.vect.vect.mapper;

import com.vect.vect.dto.response.ActivoDTO;
import com.vect.vect.entity.Activo;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivoMapper {

    @Mapping(target = "usuarioAsignadoId", source = "usuarioAsignado.id")
    @Mapping(target = "usuarioAsignadoNombre", expression = "java(nombreCompleto(activo.getUsuarioAsignado()))")
    @Mapping(target = "ubicacionId", source = "ubicacion.id")
    @Mapping(target = "ubicacionNombre", source = "ubicacion.nombre")
    ActivoDTO toDto(Activo activo);

    default String nombreCompleto(Usuario usuario) {
        return usuario == null ? null : usuario.getNombres() + " " + usuario.getApellidos();
    }
}
