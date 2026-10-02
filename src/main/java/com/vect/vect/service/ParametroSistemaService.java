package com.vect.vect.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.response.ParametroSistemaDTO;
import com.vect.vect.entity.ParametroSistema;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.ParametroSistemaMapper;
import com.vect.vect.repository.ParametroSistemaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParametroSistemaService {

    private final ParametroSistemaRepository repository;
    private final ParametroSistemaMapper mapper;

    public ParametroSistemaService(ParametroSistemaRepository repository, ParametroSistemaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ParametroSistemaDTO> listar() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "clave")).stream().map(mapper::toDto).toList();
    }

    @Transactional
    public ParametroSistemaDTO actualizar(String clave, JsonNode valor, Usuario actor) {
        ParametroSistema parametro = repository.findById(clave)
            .orElseThrow(() -> ApiException.notFound("PARAMETRO_NO_ENCONTRADO", "Parámetro no encontrado"));
        parametro.setValor(valor.deepCopy());
        parametro.setActualizadoPor(actor.getId());
        return mapper.toDto(repository.save(parametro));
    }
}
