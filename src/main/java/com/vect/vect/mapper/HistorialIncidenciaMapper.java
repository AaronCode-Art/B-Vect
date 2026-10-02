package com.vect.vect.mapper;

import com.vect.vect.dto.response.HistorialIncidenciaDTO;
import com.vect.vect.entity.HistorialIncidencia;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistorialIncidenciaMapper {

    @Mapping(target = "realizadoPorId", source = "realizadoPor.id")
    @Mapping(target = "realizadoPorNombre", expression = "java(nombreCompleto(historial.getRealizadoPor()))")
    HistorialIncidenciaDTO toDto(HistorialIncidencia historial);

    default String nombreCompleto(Usuario usuario) {
        return usuario == null ? null : usuario.getNombres() + " " + usuario.getApellidos();
    }
}
