package com.vect.vect.mapper;

import com.vect.vect.dto.response.UsuarioDTO;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioDTO toDto(Usuario usuario);
}
