package com.vect.vect.controller;

import com.vect.vect.dto.request.CategoriaRequest;
import com.vect.vect.dto.response.CategoriaDTO;
import com.vect.vect.dto.request.EspecialidadRequest;
import com.vect.vect.dto.request.UsuarioEspecialidadRequest;
import com.vect.vect.dto.response.EspecialidadDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/categorias", "/api/categorias-incidencia"})
public class CategoriaController {

    private final CatalogoService catalogoService;

    public CategoriaController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public List<CategoriaDTO> listar() {
        return catalogoService.listarCategorias();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaDTO> crear(@Valid @RequestBody CategoriaRequest request,
                                                Authentication authentication) {
        Usuario actor = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201).body(catalogoService.crearCategoria(request, actor.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoriaDTO actualizar(@PathVariable UUID id, @Valid @RequestBody CategoriaRequest request) {
        return catalogoService.actualizarCategoria(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoriaDTO desactivar(@PathVariable UUID id) {
        return catalogoService.desactivarCategoria(id);
    }

    @GetMapping("/especialidades")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR', 'TECNICO')")
    public List<EspecialidadDTO> especialidades() {
        return catalogoService.listarEspecialidades();
    }

    @PostMapping("/especialidades")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EspecialidadDTO> crearEspecialidad(@Valid @RequestBody EspecialidadRequest request) {
        return ResponseEntity.status(201).body(catalogoService.crearEspecialidad(request));
    }

    @PutMapping("/especialidades/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EspecialidadDTO actualizarEspecialidad(@PathVariable UUID id,
            @Valid @RequestBody EspecialidadRequest request) {
        return catalogoService.actualizarEspecialidad(id, request);
    }

    @GetMapping("/usuarios/{usuarioId}/especialidades")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR')")
    public List<EspecialidadDTO> especialidadesDeUsuario(@PathVariable UUID usuarioId) {
        return catalogoService.especialidadesDeUsuario(usuarioId);
    }

    @PutMapping("/usuarios/{usuarioId}/especialidades")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EspecialidadDTO> asignarEspecialidades(@PathVariable UUID usuarioId,
            @Valid @RequestBody UsuarioEspecialidadRequest request) {
        return catalogoService.asignarEspecialidades(usuarioId, request.especialidadIds(), request.principalId());
    }
}
