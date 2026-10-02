package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.ActivoRequest;
import com.vect.vect.dto.request.InstalarComponenteRequest;
import com.vect.vect.dto.request.MovimientoStockRequest;
import com.vect.vect.dto.response.ActivoDTO;
import com.vect.vect.dto.response.ComponenteActivoDTO;
import com.vect.vect.entity.Activo;
import com.vect.vect.entity.Componente;
import com.vect.vect.entity.ComponenteActivo;
import com.vect.vect.entity.Usuario;
import com.vect.vect.entity.MovimientoStock;
import com.vect.vect.mapper.ActivoMapper;
import com.vect.vect.mapper.ComponenteMapper;
import com.vect.vect.repository.ActivoRepository;
import com.vect.vect.repository.ComponenteActivoRepository;
import com.vect.vect.repository.ComponenteRepository;
import com.vect.vect.repository.UbicacionRepository;
import com.vect.vect.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ActivoService {

    private final ActivoRepository activoRepository;
    private final ComponenteRepository componenteRepository;
    private final ComponenteActivoRepository componenteActivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final ActivoMapper activoMapper;
    private final ComponenteMapper componenteMapper;
    private final ComponenteService componenteService;

    public ActivoService(ActivoRepository activoRepository, ComponenteRepository componenteRepository,
                         ComponenteActivoRepository componenteActivoRepository, UsuarioRepository usuarioRepository,
                         UbicacionRepository ubicacionRepository, ActivoMapper activoMapper,
                         ComponenteMapper componenteMapper, ComponenteService componenteService) {
        this.activoRepository = activoRepository;
        this.componenteRepository = componenteRepository;
        this.componenteActivoRepository = componenteActivoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.activoMapper = activoMapper;
        this.componenteMapper = componenteMapper;
        this.componenteService = componenteService;
    }

    @Transactional(readOnly = true)
    public List<ActivoDTO> listar() {
        return activoRepository.findAllByOrderByCodigoAsc().stream().map(activoMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ActivoDTO obtener(UUID id) {
        return activoMapper.toDto(buscar(id));
    }

    @Transactional
    public ActivoDTO crear(ActivoRequest request, Usuario actor) {
        Activo activo = new Activo();
        actualizarCampos(activo, request);
        activo.setCreador(actor);
        return activoMapper.toDto(activoRepository.save(activo));
    }

    @Transactional
    public ActivoDTO actualizar(UUID id, ActivoRequest request) {
        Activo activo = buscar(id);
        actualizarCampos(activo, request);
        return activoMapper.toDto(activoRepository.save(activo));
    }

    @Transactional(readOnly = true)
    public List<ComponenteActivoDTO> listarComponentes(UUID activoId) {
        buscar(activoId);
        return componenteActivoRepository
            .findAllByActivo_IdAndRetiradoEnIsNullOrderByInstaladoEnDesc(activoId)
            .stream().map(componenteMapper::toActivoDto).toList();
    }

    @Transactional
    public ComponenteActivoDTO instalarComponente(UUID activoId, InstalarComponenteRequest request, Usuario actor) {
        Activo activo = buscar(activoId);
        Componente componente = componenteRepository.findById(request.componenteId())
            .orElseThrow(() -> ApiException.notFound("COMPONENTE_NO_ENCONTRADO", "Componente no encontrado"));
        if (!Boolean.TRUE.equals(componente.getActivo())) {
            throw ApiException.conflict("COMPONENTE_INACTIVO", "No se puede instalar un componente inactivo");
        }
        if (componenteActivoRepository.existsByActivo_IdAndComponente_IdAndRetiradoEnIsNull(activoId, componente.getId())) {
            throw ApiException.conflict("COMPONENTE_YA_INSTALADO", "El componente ya está instalado en este activo");
        }

        componenteService.registrarMovimiento(componente.getId(),
            new MovimientoStockRequest(MovimientoStock.TipoMovimiento.SALIDA, 1,
                "Instalación en activo " + activo.getCodigo(), "ACTIVO", activo.getId()), actor);

        ComponenteActivo instalacion = new ComponenteActivo();
        instalacion.setActivo(activo);
        instalacion.setComponente(componente);
        instalacion.setNumeroSerie(request.numeroSerie());
        instalacion.setInstaladoPor(actor.getId());
        return componenteMapper.toActivoDto(componenteActivoRepository.save(instalacion));
    }

    @Transactional
    public void retirarComponente(UUID activoId, UUID instalacionId) {
        buscar(activoId);
        ComponenteActivo instalacion = componenteActivoRepository.findById(instalacionId)
            .filter(item -> item.getActivo().getId().equals(activoId))
            .orElseThrow(() -> ApiException.notFound("INSTALACION_NO_ENCONTRADA", "Instalación no encontrada"));
        if (instalacion.getRetiradoEn() != null) {
            throw ApiException.conflict("COMPONENTE_YA_RETIRADO", "El componente ya fue retirado");
        }
        instalacion.setRetiradoEn(java.time.LocalDateTime.now());
    }

    private void actualizarCampos(Activo activo, ActivoRequest request) {
        activo.setCodigo(request.codigo().trim());
        activo.setNombre(request.nombre().trim());
        activo.setNumeroSerie(limpiar(request.numeroSerie()));
        activo.setEstado(request.estado());
        activo.setFechaCompra(request.fechaCompra());
        activo.setNotas(request.notas());
        activo.setUsuarioAsignado(request.usuarioAsignadoId() == null ? null
            : usuarioRepository.findById(request.usuarioAsignadoId())
                .orElseThrow(() -> ApiException.notFound("USUARIO_NO_ENCONTRADO", "Usuario asignado no encontrado")));
        activo.setUbicacion(request.ubicacionId() == null ? null
            : ubicacionRepository.findById(request.ubicacionId())
                .orElseThrow(() -> ApiException.notFound("UBICACION_NO_ENCONTRADA", "Ubicación no encontrada")));
    }

    private Activo buscar(UUID id) {
        return activoRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("ACTIVO_NO_ENCONTRADO", "Activo no encontrado"));
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
