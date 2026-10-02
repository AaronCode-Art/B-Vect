package com.vect.vect.mapper;

import com.vect.vect.dto.response.RegistroAuditoriaDTO;
import com.vect.vect.entity.RegistroAuditoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegistroAuditoriaMapper {

    @Mapping(target = "direccionIp", expression = "java(registro.getDireccionIp() == null ? null : registro.getDireccionIp().getHostAddress())")
    RegistroAuditoriaDTO toDto(RegistroAuditoria registro);
}
