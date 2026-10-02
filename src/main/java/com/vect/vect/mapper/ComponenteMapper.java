package com.vect.vect.mapper;

import com.vect.vect.dto.response.ComponenteActivoDTO;
import com.vect.vect.dto.response.ComponenteDTO;
import com.vect.vect.dto.response.MovimientoStockDTO;
import com.vect.vect.entity.Componente;
import com.vect.vect.entity.ComponenteActivo;
import com.vect.vect.entity.MovimientoStock;
import com.vect.vect.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComponenteMapper {

    ComponenteDTO toDto(Componente componente);

    @Mapping(target = "componenteCodigo", source = "componente.codigo")
    @Mapping(target = "componenteNombre", source = "componente.nombre")
    @Mapping(target = "componenteId", source = "componente.id")
    @Mapping(target = "realizadoPorNombre", expression = "java(nombreCompleto(movimiento.getRealizadoPor()))")
    MovimientoStockDTO toMovimientoDto(MovimientoStock movimiento);

    @Mapping(target = "activoId", source = "activo.id")
    @Mapping(target = "componenteId", source = "componente.id")
    @Mapping(target = "componenteCodigo", source = "componente.codigo")
    @Mapping(target = "componenteNombre", source = "componente.nombre")
    ComponenteActivoDTO toActivoDto(ComponenteActivo componenteActivo);

    default String nombreCompleto(Usuario usuario) {
        return usuario == null ? null : usuario.getNombres() + " " + usuario.getApellidos();
    }
}
