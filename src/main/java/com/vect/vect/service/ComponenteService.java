package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.ComponenteRequest;
import com.vect.vect.dto.request.MovimientoStockRequest;
import com.vect.vect.dto.response.ComponenteDTO;
import com.vect.vect.dto.response.MovimientoStockDTO;
import com.vect.vect.entity.Componente;
import com.vect.vect.entity.MovimientoStock;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.ComponenteMapper;
import com.vect.vect.repository.ComponenteRepository;
import com.vect.vect.repository.MovimientoStockRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ComponenteService {

    private final ComponenteRepository componenteRepository;
    private final MovimientoStockRepository movimientoRepository;
    private final ComponenteMapper componenteMapper;
    private final DatabaseActorContext databaseActorContext;
    private final EntityManager entityManager;

    public ComponenteService(ComponenteRepository componenteRepository, MovimientoStockRepository movimientoRepository,
                             ComponenteMapper componenteMapper, DatabaseActorContext databaseActorContext,
                             EntityManager entityManager) {
        this.componenteRepository = componenteRepository;
        this.movimientoRepository = movimientoRepository;
        this.componenteMapper = componenteMapper;
        this.databaseActorContext = databaseActorContext;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<ComponenteDTO> listar() {
        return componenteRepository.findAll().stream().map(componenteMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ComponenteDTO obtener(UUID id) {
        return componenteMapper.toDto(buscar(id));
    }

    @Transactional
    public ComponenteDTO crear(ComponenteRequest request, Usuario actor) {
        Componente componente = new Componente();
        actualizarCampos(componente, request);
        componente.setCreadoPor(actor.getId());
        componente.setStockActual(0);
        return componenteMapper.toDto(componenteRepository.save(componente));
    }

    @Transactional
    public ComponenteDTO actualizar(UUID id, ComponenteRequest request) {
        Componente componente = buscar(id);
        actualizarCampos(componente, request);
        return componenteMapper.toDto(componenteRepository.save(componente));
    }

    @Transactional
    public ComponenteDTO desactivar(UUID id) {
        Componente componente = buscar(id);
        componente.setActivo(false);
        return componenteMapper.toDto(componenteRepository.save(componente));
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockDTO> listarMovimientos(UUID componenteId, Usuario actor) {
        if (componenteId != null) {
            buscar(componenteId);
            return movimientoRepository.findAllByComponente_IdOrderByCreadoEnDesc(componenteId)
                .stream().map(componenteMapper::toMovimientoDto).toList();
        }
        if (actor.getRol() == Usuario.RolUsuario.TECNICO) {
            return movimientoRepository.findAll().stream()
                .filter(m -> m.getRealizadoPor().getId().equals(actor.getId()))
                .map(componenteMapper::toMovimientoDto)
                .toList();
        }
        return movimientoRepository.findAllByOrderByCreadoEnDesc().stream()
            .map(componenteMapper::toMovimientoDto).toList();
    }

    @Transactional
    public MovimientoStockDTO registrarMovimiento(UUID componenteId, MovimientoStockRequest request, Usuario actor) {
        if (actor.getRol() != Usuario.RolUsuario.ADMIN && actor.getRol() != Usuario.RolUsuario.GERENCIA) {
            throw ApiException.forbidden("ACCESO_DENEGADO", "Solo ADMIN y GERENCIA pueden registrar movimientos");
        }
        Componente componente = buscar(componenteId);
        databaseActorContext.setActor(actor);

        Object movimientoId = entityManager.createNativeQuery("""
                select (vect.registrar_movimiento_stock(
                    cast(:componenteId as uuid),
                    cast(:tipo as vect.tipo_movimiento_stock),
                    cast(:cantidad as integer),
                    cast(:motivo as text),
                    cast(:tipoReferencia as varchar),
                    cast(:referenciaId as uuid)
                )).id
                """)
            .setParameter("componenteId", componenteId)
            .setParameter("tipo", request.tipoMovimiento().name())
            .setParameter("cantidad", request.cantidad())
            .setParameter("motivo", request.motivo().trim())
            .setParameter("tipoReferencia", request.tipoReferencia())
            .setParameter("referenciaId", request.referenciaId())
            .getSingleResult();

        UUID id = UUID.fromString(movimientoId.toString());
        entityManager.refresh(componente);
        MovimientoStock movimiento = movimientoRepository.findById(id)
            .orElseThrow(() -> ApiException.conflict("MOVIMIENTO_NO_REGISTRADO",
                "La base de datos no devolvió el movimiento creado"));
        return componenteMapper.toMovimientoDto(movimiento);
    }

    private void actualizarCampos(Componente componente, ComponenteRequest request) {
        componente.setCodigo(request.codigo().trim());
        componente.setNombre(request.nombre().trim());
        componente.setTipoComponente(request.tipoComponente().trim());
        componente.setDescripcion(request.descripcion());
        componente.setCostoUnitario(request.costoUnitario());
        componente.setStockMinimo(request.stockMinimo());
        componente.setActivo(request.activo());
    }

    private Componente buscar(UUID id) {
        return componenteRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("COMPONENTE_NO_ENCONTRADO", "Componente no encontrado"));
    }
}
