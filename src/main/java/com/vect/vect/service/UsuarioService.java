package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.UsuarioRequest;
import com.vect.vect.dto.response.UsuarioDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.UsuarioMapper;
import com.vect.vect.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listar(Usuario actor) {
        return usuarioRepository.findAllByOrderByApellidosAscNombresAsc().stream()
            .filter(usuario -> puedeAdministrar(actor, usuario.getRol()))
            .map(usuarioMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTecnicosDisponibles() {
        return usuarioRepository.findAllByRolAndActivoTrueOrderByApellidosAscNombresAsc(Usuario.RolUsuario.TECNICO)
            .stream().map(usuarioMapper::toDto).toList();
    }

    @Transactional
    public UsuarioDTO crear(UsuarioRequest request, Usuario actor) {
        if (request.contrasena() == null || request.contrasena().isBlank()) {
            throw ApiException.badRequest("CONTRASENA_REQUERIDA", "Debe indicar una contraseña inicial");
        }
        verificarRolAdministrable(actor, request.rol());
        String correo = request.correo().trim().toLowerCase(java.util.Locale.ROOT);
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw ApiException.conflict("CORREO_EN_USO", "Ya existe un usuario con ese correo");
        }
        Usuario usuario = new Usuario();
        actualizarCampos(usuario, request, correo);
        usuario.setHashContrasena(passwordEncoder.encode(request.contrasena()));
        usuario.setDebeCambiarContrasena(true);
        usuario.setCreadoPor(actor.getId());
        return usuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioDTO actualizar(UUID id, UsuarioRequest request, Usuario actor) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado"));
        verificarRolAdministrable(actor, usuario.getRol());
        verificarRolAdministrable(actor, request.rol());
        String correo = request.correo().trim().toLowerCase(java.util.Locale.ROOT);
        if (!correo.equalsIgnoreCase(usuario.getCorreo()) && usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw ApiException.conflict("CORREO_EN_USO", "Ya existe un usuario con ese correo");
        }
        actualizarCampos(usuario, request, correo);
        if (request.contrasena() != null && !request.contrasena().isBlank()) {
            usuario.setHashContrasena(passwordEncoder.encode(request.contrasena()));
            usuario.setDebeCambiarContrasena(true);
        }
        return usuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    @Transactional
    public void desactivar(UUID id, Usuario actor) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado"));
        verificarRolAdministrable(actor, usuario.getRol());
        if (usuario.getId().equals(actor.getId())) {
            throw ApiException.conflict("NO_PUEDE_DESACTIVARSE", "No puede desactivar su propio usuario");
        }
        usuario.setActivo(false);
    }

    private void actualizarCampos(Usuario usuario, UsuarioRequest request, String correo) {
        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(request.telefono() == null || request.telefono().isBlank()
            ? null : request.telefono().trim());
        usuario.setRol(request.rol());
        if (request.activo() != null) {
            usuario.setActivo(request.activo());
        }
    }

    private void verificarRolAdministrable(Usuario actor, Usuario.RolUsuario rol) {
        if (!puedeAdministrar(actor, rol)) {
            throw ApiException.forbidden("ROL_NO_ADMINISTRABLE",
                "No tiene permiso para gestionar usuarios con el rol " + rol);
        }
    }

    private boolean puedeAdministrar(Usuario actor, Usuario.RolUsuario rol) {
        return switch (actor.getRol()) {
            case ADMIN -> true;
            case GERENCIA -> rol == Usuario.RolUsuario.SUPERVISOR
                || rol == Usuario.RolUsuario.TECNICO || rol == Usuario.RolUsuario.EMPLEADO;
            case SUPERVISOR -> rol == Usuario.RolUsuario.TECNICO;
            case TECNICO, EMPLEADO -> false;
        };
    }
}
