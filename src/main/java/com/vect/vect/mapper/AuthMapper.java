package com.vect.vect.mapper;

import com.vect.vect.dto.response.AuthResponse;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "rol", expression = "java(usuario.getRol().name())")
    @Mapping(target = "rolNombre", expression = "java(usuario.getRol().name())")
    AuthResponse.UsuarioDTO toDto(Usuario usuario);
}
