package com.vect.vect.controller;

import com.vect.vect.dto.request.UsuarioRequest;
import com.vect.vect.dto.response.UsuarioDTO;
import com.vect.vect.dto.response.EspecialidadDTO;
import com.vect.vect.dto.request.AsignarEspecialidadRequest;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.CatalogoService;
import com.vect.vect.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final CatalogoService catalogoService;

    public UsuarioController(UsuarioService usuarioService, CatalogoService catalogoService) {
        this.usuarioService = usuarioService;
        this.catalogoService = catalogoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR')")
    public List<UsuarioDTO> listar(Authentication authentication) {
        return usuarioService.listar((Usuario) authentication.getPrincipal());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR')")
    public ResponseEntity<UsuarioDTO> crear(@Valid @RequestBody UsuarioRequest request, Authentication authentication) {
        return ResponseEntity.status(201).body(usuarioService.crear(request, (Usuario) authentication.getPrincipal()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR')")
    public UsuarioDTO actualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioRequest request,
                                 Authentication authentication) {
        return usuarioService.actualizar(id, request, (Usuario) authentication.getPrincipal());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id, Authentication authentication) {
        usuarioService.desactivar(id, (Usuario) authentication.getPrincipal());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tecnicos/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR')")
    public List<UsuarioDTO> tecnicosDisponibles() {
        return usuarioService.listarTecnicosDisponibles();
    }

    @GetMapping("/{id}/especialidades")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR')")
    public List<EspecialidadDTO> especialidades(@PathVariable UUID id) {
        return catalogoService.especialidadesDeUsuario(id);
    }

    @PostMapping("/{id}/especialidades")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EspecialidadDTO> asignarEspecialidad(@PathVariable UUID id,
            @Valid @RequestBody AsignarEspecialidadRequest request) {
        return catalogoService.agregarEspecialidad(id, request.especialidadId());
    }

    @DeleteMapping("/{id}/especialidades/{especialidadId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EspecialidadDTO> quitarEspecialidad(@PathVariable UUID id,
                                                     @PathVariable UUID especialidadId) {
        return catalogoService.quitarEspecialidad(id, especialidadId);
    }
}
