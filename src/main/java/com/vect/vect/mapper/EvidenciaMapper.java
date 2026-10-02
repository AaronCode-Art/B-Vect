package com.vect.vect.mapper;

import com.vect.vect.dto.response.EvidenciaDTO;
import com.vect.vect.entity.Evidencia;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvidenciaMapper {

    @Mapping(target = "incidenciaId", source = "evidencia.incidencia.id")
    @Mapping(target = "subidoPorId", source = "evidencia.subidoPor.id")
    @Mapping(target = "subidoPorNombre", expression = "java(nombreCompleto(evidencia.getSubidoPor()))")
    @Mapping(target = "url", source = "url")
    EvidenciaDTO toDto(Evidencia evidencia, String url);

    default String nombreCompleto(Usuario usuario) {
        return usuario == null ? null : usuario.getNombres() + " " + usuario.getApellidos();
    }
}
