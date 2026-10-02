package com.vect.vect.controller;

import com.vect.vect.dto.request.ActivoRequest;
import com.vect.vect.dto.request.InstalarComponenteRequest;
import com.vect.vect.dto.response.ActivoDTO;
import com.vect.vect.dto.response.ComponenteActivoDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.ActivoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.UUID;

@RestController
@RequestMapping("/api/activos")
public class ActivoController {

    private final ActivoService activoService;

    public ActivoController(ActivoService activoService) {
        this.activoService = activoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public List<ActivoDTO> listar() {
        return activoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ActivoDTO obtener(@PathVariable UUID id) {
        return activoService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ResponseEntity<ActivoDTO> crear(@Valid @RequestBody ActivoRequest request, Authentication authentication) {
        return ResponseEntity.status(201).body(activoService.crear(request, (Usuario) authentication.getPrincipal()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ActivoDTO actualizar(@PathVariable UUID id, @Valid @RequestBody ActivoRequest request) {
        return activoService.actualizar(id, request);
    }

    @GetMapping("/{id}/componentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public List<ComponenteActivoDTO> componentes(@PathVariable UUID id) {
        return activoService.listarComponentes(id);
    }

    @PostMapping("/{id}/componentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ResponseEntity<ComponenteActivoDTO> instalarComponente(@PathVariable UUID id,
            @Valid @RequestBody InstalarComponenteRequest request, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201).body(activoService.instalarComponente(id, request, usuario));
    }

    @DeleteMapping("/{id}/componentes/{instalacionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ResponseEntity<Void> retirarComponente(@PathVariable UUID id, @PathVariable UUID instalacionId) {
        activoService.retirarComponente(id, instalacionId);
        return ResponseEntity.noContent().build();
    }
}
