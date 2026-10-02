package com.vect.vect.controller;

import com.vect.vect.dto.request.UbicacionRequest;
import com.vect.vect.dto.response.UbicacionDTO;
import com.vect.vect.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/ubicaciones", "/api/sedes"})
public class UbicacionController {

    private final CatalogoService catalogoService;

    public UbicacionController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public List<UbicacionDTO> listar() {
        return catalogoService.listarUbicaciones();
    }

    @GetMapping("/gestion")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public List<UbicacionDTO> listarParaGestion() {
        return catalogoService.listarUbicacionesParaGestion();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ResponseEntity<UbicacionDTO> crear(@Valid @RequestBody UbicacionRequest request) {
        return ResponseEntity.status(201).body(catalogoService.crearUbicacion(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public UbicacionDTO actualizar(@PathVariable UUID id, @Valid @RequestBody UbicacionRequest request) {
        return catalogoService.actualizarUbicacion(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public UbicacionDTO desactivar(@PathVariable UUID id) {
        return catalogoService.desactivarUbicacion(id);
    }
}
