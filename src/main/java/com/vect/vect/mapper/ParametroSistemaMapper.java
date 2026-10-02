package com.vect.vect.mapper;

import com.vect.vect.dto.response.ParametroSistemaDTO;
import com.vect.vect.entity.ParametroSistema;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParametroSistemaMapper {

    ParametroSistemaDTO toDto(ParametroSistema parametro);
}
