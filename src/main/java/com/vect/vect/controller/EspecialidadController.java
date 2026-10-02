package com.vect.vect.controller;

import com.vect.vect.dto.request.EspecialidadRequest;
import com.vect.vect.dto.response.EspecialidadDTO;
import com.vect.vect.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    private final CatalogoService catalogoService;

    public EspecialidadController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR', 'TECNICO')")
    public List<EspecialidadDTO> listar() {
        return catalogoService.listarEspecialidades();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EspecialidadDTO> crear(@Valid @RequestBody EspecialidadRequest request) {
        return ResponseEntity.status(201).body(catalogoService.crearEspecialidad(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EspecialidadDTO actualizar(@PathVariable UUID id, @Valid @RequestBody EspecialidadRequest request) {
        return catalogoService.actualizarEspecialidad(id, request);
    }
}
