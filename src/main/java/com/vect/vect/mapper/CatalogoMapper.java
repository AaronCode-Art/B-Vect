package com.vect.vect.mapper;

import com.vect.vect.dto.response.CategoriaDTO;
import com.vect.vect.dto.response.EspecialidadDTO;
import com.vect.vect.dto.response.UbicacionDTO;
import com.vect.vect.entity.Categoria;
import com.vect.vect.entity.Especialidad;
import com.vect.vect.entity.Ubicacion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CatalogoMapper {

    CategoriaDTO toDto(Categoria categoria);

    UbicacionDTO toDto(Ubicacion ubicacion);

    EspecialidadDTO toDto(Especialidad especialidad);
}
