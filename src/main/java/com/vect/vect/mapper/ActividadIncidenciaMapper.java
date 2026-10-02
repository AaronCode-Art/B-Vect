package com.vect.vect.mapper;

import com.vect.vect.dto.response.ActividadIncidenciaDTO;
import com.vect.vect.entity.ActividadIncidencia;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActividadIncidenciaMapper {

    @Mapping(target = "incidenciaId", source = "incidencia.id")
    @Mapping(target = "tecnicoId", source = "tecnico.id")
    @Mapping(target = "tecnicoNombre", expression = "java(nombreCompleto(actividad.getTecnico()))")
    ActividadIncidenciaDTO toDto(ActividadIncidencia actividad);

    default String nombreCompleto(Usuario usuario) {
        return usuario == null ? null : usuario.getNombres() + " " + usuario.getApellidos();
    }
}
