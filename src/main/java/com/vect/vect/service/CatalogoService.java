package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.CategoriaRequest;
import com.vect.vect.dto.request.EspecialidadRequest;
import com.vect.vect.dto.request.UbicacionRequest;
import com.vect.vect.dto.response.CategoriaDTO;
import com.vect.vect.dto.response.EspecialidadDTO;
import com.vect.vect.dto.response.UbicacionDTO;
import com.vect.vect.entity.Categoria;
import com.vect.vect.entity.Especialidad;
import com.vect.vect.entity.Ubicacion;
import com.vect.vect.mapper.CatalogoMapper;
import com.vect.vect.repository.CategoriaRepository;
import com.vect.vect.repository.EspecialidadRepository;
import com.vect.vect.repository.UbicacionRepository;
import com.vect.vect.repository.UsuarioEspecialidadRepository;
import com.vect.vect.repository.UsuarioRepository;
import com.vect.vect.entity.UsuarioEspecialidad;
import com.vect.vect.entity.UsuarioEspecialidadId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CatalogoService {

    private final CategoriaRepository categoriaRepository;
    private final UbicacionRepository ubicacionRepository;
    private final EspecialidadRepository especialidadRepository;
    private final UsuarioEspecialidadRepository usuarioEspecialidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoMapper catalogoMapper;

    public CatalogoService(CategoriaRepository categoriaRepository, UbicacionRepository ubicacionRepository,
                           EspecialidadRepository especialidadRepository,
                           UsuarioEspecialidadRepository usuarioEspecialidadRepository,
                           UsuarioRepository usuarioRepository, CatalogoMapper catalogoMapper) {
        this.categoriaRepository = categoriaRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.especialidadRepository = especialidadRepository;
        this.usuarioEspecialidadRepository = usuarioEspecialidadRepository;
        this.usuarioRepository = usuarioRepository;
        this.catalogoMapper = catalogoMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
            .map(catalogoMapper::toDto).toList();
    }

    @Transactional
    public CategoriaDTO crearCategoria(CategoriaRequest request, UUID actorId) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre().trim());
        categoria.setDescripcion(request.descripcion());
        categoria.setActivo(request.activo() == null || request.activo());
        categoria.setCreadoPor(actorId);
        return catalogoMapper.toDto(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaDTO actualizarCategoria(UUID id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("CATEGORIA_NO_ENCONTRADA", "Categoría no encontrada"));
        categoria.setNombre(request.nombre().trim());
        categoria.setDescripcion(request.descripcion());
        if (request.activo() != null) {
            categoria.setActivo(request.activo());
        }
        return catalogoMapper.toDto(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaDTO desactivarCategoria(UUID id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("CATEGORIA_NO_ENCONTRADA", "Categoría no encontrada"));
        categoria.setActivo(false);
        return catalogoMapper.toDto(categoriaRepository.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<UbicacionDTO> listarUbicaciones() {
        return ubicacionRepository.findAllByOrderByNombreAsc().stream()
            .filter(Ubicacion::getActivo)
            .map(catalogoMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UbicacionDTO> listarUbicacionesParaGestion() {
        return ubicacionRepository.findAllByOrderByNombreAsc().stream()
            .map(catalogoMapper::toDto).toList();
    }

    @Transactional
    public UbicacionDTO crearUbicacion(UbicacionRequest request) {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setNombre(request.nombre().trim());
        ubicacion.setDireccion(request.direccion());
        ubicacion.setActivo(request.activo() == null || request.activo());
        return catalogoMapper.toDto(ubicacionRepository.save(ubicacion));
    }

    @Transactional
    public UbicacionDTO actualizarUbicacion(UUID id, UbicacionRequest request) {
        Ubicacion ubicacion = ubicacionRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("UBICACION_NO_ENCONTRADA", "Ubicación no encontrada"));
        ubicacion.setNombre(request.nombre().trim());
        ubicacion.setDireccion(request.direccion());
        if (request.activo() != null) {
            ubicacion.setActivo(request.activo());
        }
        return catalogoMapper.toDto(ubicacionRepository.save(ubicacion));
    }

    @Transactional
    public UbicacionDTO desactivarUbicacion(UUID id) {
        Ubicacion ubicacion = ubicacionRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("UBICACION_NO_ENCONTRADA", "Ubicación no encontrada"));
        ubicacion.setActivo(false);
        return catalogoMapper.toDto(ubicacionRepository.save(ubicacion));
    }

    @Transactional(readOnly = true)
    public List<EspecialidadDTO> listarEspecialidades() {
        return especialidadRepository.findAllByOrderByNombreAsc().stream()
            .map(catalogoMapper::toDto).toList();
    }

    @Transactional
    public EspecialidadDTO crearEspecialidad(EspecialidadRequest request) {
        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(request.nombre().trim());
        especialidad.setDescripcion(request.descripcion());
        especialidad.setActivo(request.activo() == null || request.activo());
        return catalogoMapper.toDto(especialidadRepository.save(especialidad));
    }

    @Transactional
    public EspecialidadDTO actualizarEspecialidad(UUID id, EspecialidadRequest request) {
        Especialidad especialidad = especialidadRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("ESPECIALIDAD_NO_ENCONTRADA", "Especialidad no encontrada"));
        especialidad.setNombre(request.nombre().trim());
        especialidad.setDescripcion(request.descripcion());
        if (request.activo() != null) {
            especialidad.setActivo(request.activo());
        }
        return catalogoMapper.toDto(especialidadRepository.save(especialidad));
    }

    @Transactional
    public List<EspecialidadDTO> asignarEspecialidades(UUID usuarioId, List<UUID> especialidadIds, UUID principalId) {
        var usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> ApiException.notFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado"));
        if (usuario.getRol() != com.vect.vect.entity.Usuario.RolUsuario.TECNICO) {
            throw ApiException.badRequest("USUARIO_NO_TECNICO", "Las especialidades solo se asignan a técnicos");
        }
        if (especialidadIds.stream().distinct().count() != especialidadIds.size()) {
            throw ApiException.badRequest("ESPECIALIDAD_DUPLICADA", "La lista contiene especialidades duplicadas");
        }
        if (principalId != null && !especialidadIds.contains(principalId)) {
            throw ApiException.badRequest("ESPECIALIDAD_PRINCIPAL_INVALIDA",
                "La especialidad principal debe estar incluida en la lista");
        }
        var especialidades = especialidadRepository.findAllById(especialidadIds);
        if (especialidades.size() != especialidadIds.size() || especialidades.stream().anyMatch(e -> !e.getActivo())) {
            throw ApiException.badRequest("ESPECIALIDAD_INVALIDA",
                "Una o más especialidades no existen o están inactivas");
        }
        usuarioEspecialidadRepository.deleteAll(usuarioEspecialidadRepository.findAllByUsuario_Id(usuarioId));
        for (Especialidad especialidad : especialidades) {
            UsuarioEspecialidad asignacion = new UsuarioEspecialidad();
            UsuarioEspecialidadId id = new UsuarioEspecialidadId();
            id.setUsuarioId(usuarioId);
            id.setEspecialidadId(especialidad.getId());
            asignacion.setId(id);
            asignacion.setUsuario(usuario);
            asignacion.setEspecialidad(especialidad);
            asignacion.setEsPrincipal(especialidad.getId().equals(principalId));
            usuarioEspecialidadRepository.save(asignacion);
        }
        return especialidades.stream().map(catalogoMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EspecialidadDTO> especialidadesDeUsuario(UUID usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw ApiException.notFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado");
        }
        return usuarioEspecialidadRepository.findAllByUsuario_Id(usuarioId).stream()
            .map(UsuarioEspecialidad::getEspecialidad).map(catalogoMapper::toDto).toList();
    }

    @Transactional
    public List<EspecialidadDTO> agregarEspecialidad(UUID usuarioId, UUID especialidadId) {
        var asignaciones = usuarioEspecialidadRepository.findAllByUsuario_Id(usuarioId);
        var ids = asignaciones.stream().map(item -> item.getEspecialidad().getId()).collect(
            java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        if (ids.contains(especialidadId)) {
            return especialidadesDeUsuario(usuarioId);
        }
        ids.add(especialidadId);
        UUID principalId = asignaciones.stream().filter(UsuarioEspecialidad::getEsPrincipal)
            .map(item -> item.getEspecialidad().getId()).findFirst().orElse(especialidadId);
        return asignarEspecialidades(usuarioId, ids, principalId);
    }

    @Transactional
    public List<EspecialidadDTO> quitarEspecialidad(UUID usuarioId, UUID especialidadId) {
        var asignaciones = usuarioEspecialidadRepository.findAllByUsuario_Id(usuarioId);
        if (asignaciones.isEmpty()) {
            if (!usuarioRepository.existsById(usuarioId)) {
                throw ApiException.notFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado");
            }
            throw ApiException.notFound("ESPECIALIDAD_USUARIO_NO_ENCONTRADA",
                "El usuario no tiene asignada esa especialidad");
        }
        var eliminada = asignaciones.stream()
            .filter(item -> item.getEspecialidad().getId().equals(especialidadId))
            .findFirst()
            .orElseThrow(() -> ApiException.notFound("ESPECIALIDAD_USUARIO_NO_ENCONTRADA",
                "El usuario no tiene asignada esa especialidad"));
        boolean eraPrincipal = Boolean.TRUE.equals(eliminada.getEsPrincipal());
        usuarioEspecialidadRepository.delete(eliminada);
        var restantes = asignaciones.stream()
            .filter(item -> !item.getEspecialidad().getId().equals(especialidadId))
            .toList();
        if (eraPrincipal && !restantes.isEmpty()) {
            UsuarioEspecialidad nuevaPrincipal = restantes.get(0);
            nuevaPrincipal.setEsPrincipal(true);
            usuarioEspecialidadRepository.save(nuevaPrincipal);
        }
        return restantes.stream().map(UsuarioEspecialidad::getEspecialidad)
            .map(catalogoMapper::toDto).toList();
    }
}
