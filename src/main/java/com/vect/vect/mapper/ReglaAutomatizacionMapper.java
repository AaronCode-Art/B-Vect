package com.vect.vect.mapper;

import com.vect.vect.dto.response.ReglaAutomatizacionDTO;
import com.vect.vect.entity.ReglaAutomatizacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReglaAutomatizacionMapper {

    @Mapping(target = "creadoPorId", source = "creadoPor.id")
    ReglaAutomatizacionDTO toDto(ReglaAutomatizacion regla);
}
