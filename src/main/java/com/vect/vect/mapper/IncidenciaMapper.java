package com.vect.vect.mapper;

import com.vect.vect.dto.response.IncidenciaDTO;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncidenciaMapper {

    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "ubicacionNombre", source = "ubicacion.nombre")
    @Mapping(target = "reportanteId", source = "reportante.id")
    @Mapping(target = "reportanteNombre", expression = "java(nombreCompleto(incidencia.getReportante()))")
    @Mapping(target = "tecnicoAsignadoId", source = "tecnicoAsignado.id")
    @Mapping(target = "tecnicoNombre", expression = "java(nombreCompleto(incidencia.getTecnicoAsignado()))")
    IncidenciaDTO toDto(Incidencia incidencia);

    default String nombreCompleto(Usuario usuario) {
        return usuario == null ? null : usuario.getNombres() + " " + usuario.getApellidos();
    }
}
