package com.vect.vect.controller;

import com.vect.vect.dto.request.ComponenteRequest;
import com.vect.vect.dto.request.MovimientoStockRequest;
import com.vect.vect.dto.response.ComponenteDTO;
import com.vect.vect.dto.response.MovimientoStockDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.ComponenteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/componentes")
public class ComponenteController {

    private final ComponenteService componenteService;

    public ComponenteController(ComponenteService componenteService) {
        this.componenteService = componenteService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'TECNICO')")
    public List<ComponenteDTO> listar() {
        return componenteService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'TECNICO')")
    public ComponenteDTO obtener(@PathVariable UUID id) {
        return componenteService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ResponseEntity<ComponenteDTO> crear(@Valid @RequestBody ComponenteRequest request,
                                                Authentication authentication) {
        return ResponseEntity.status(201).body(componenteService.crear(request, (Usuario) authentication.getPrincipal()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ComponenteDTO actualizar(@PathVariable UUID id, @Valid @RequestBody ComponenteRequest request) {
        return componenteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ComponenteDTO desactivar(@PathVariable UUID id) {
        return componenteService.desactivar(id);
    }

    @GetMapping({"/movimientos-stock", "/{id}/movimientos"})
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'TECNICO')")
    public List<MovimientoStockDTO> movimientos(@PathVariable(required = false) UUID id,
                                                @RequestParam(required = false) UUID componenteId,
                                                Authentication authentication) {
        UUID filtroId = id == null ? componenteId : id;
        return componenteService.listarMovimientos(filtroId, (Usuario) authentication.getPrincipal());
    }

    @PostMapping({"/{id}/movimientos-stock", "/{id}/movimientos"})
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ResponseEntity<MovimientoStockDTO> registrarMovimiento(@PathVariable UUID id,
            @Valid @RequestBody MovimientoStockRequest request, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201).body(componenteService.registrarMovimiento(id, request, usuario));
    }
}
