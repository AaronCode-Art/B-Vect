package com.vect.vect.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.ReglaAutomatizacionRequest;
import com.vect.vect.dto.response.ReglaAutomatizacionDTO;
import com.vect.vect.entity.ReglaAutomatizacion;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.ReglaAutomatizacionMapper;
import com.vect.vect.repository.ReglaAutomatizacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReglaAutomatizacionService {

    private final ReglaAutomatizacionRepository repository;
    private final ReglaAutomatizacionMapper mapper;

    public ReglaAutomatizacionService(ReglaAutomatizacionRepository repository,
                                     ReglaAutomatizacionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ReglaAutomatizacionDTO> listar() {
        return repository.findAllByOrderByPrioridadAscNombreAsc().stream().map(mapper::toDto).toList();
    }

    @Transactional
    public ReglaAutomatizacionDTO crear(ReglaAutomatizacionRequest request, Usuario actor) {
        ReglaAutomatizacion regla = new ReglaAutomatizacion();
        regla.setCreadoPor(actor);
        aplicar(regla, request, true);
        return mapper.toDto(repository.save(regla));
    }

    @Transactional
    public ReglaAutomatizacionDTO actualizar(UUID id, ReglaAutomatizacionRequest request) {
        ReglaAutomatizacion regla = repository.findById(id)
            .orElseThrow(() -> ApiException.notFound("REGLA_NO_ENCONTRADA", "Regla de automatización no encontrada"));
        aplicar(regla, request, false);
        return mapper.toDto(repository.save(regla));
    }

    private void aplicar(ReglaAutomatizacion regla, ReglaAutomatizacionRequest request, boolean nueva) {
        JsonNode condiciones = request.condiciones() == null
            ? JsonNodeFactory.instance.objectNode() : request.condiciones();
        JsonNode acciones = request.acciones() == null
            ? JsonNodeFactory.instance.arrayNode() : request.acciones();
        if (!condiciones.isObject()) {
            throw ApiException.badRequest("CONDICIONES_INVALIDAS", "Las condiciones deben ser un objeto JSON");
        }
        if (!acciones.isArray()) {
            throw ApiException.badRequest("ACCIONES_INVALIDAS", "Las acciones deben ser un arreglo JSON");
        }

        regla.setNombre(request.nombre().trim());
        regla.setDescripcion(request.descripcion());
        regla.setNombreEvento(request.nombreEvento().trim());
        regla.setCondiciones(condiciones.deepCopy());
        regla.setAcciones(acciones.deepCopy());
        if (request.prioridad() != null) {
            regla.setPrioridad(request.prioridad());
        } else if (nueva) {
            regla.setPrioridad(100);
        }
        if (request.activo() != null) {
            regla.setActivo(request.activo());
        } else if (nueva) {
            regla.setActivo(true);
        }
    }
}
