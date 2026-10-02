package com.vect.vect.controller;

import com.vect.vect.dto.request.ReglaAutomatizacionRequest;
import com.vect.vect.dto.response.ReglaAutomatizacionDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.ReglaAutomatizacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/reglas-automatizacion")
@PreAuthorize("hasRole('ADMIN')")
public class ReglaAutomatizacionController {

    private final ReglaAutomatizacionService service;

    public ReglaAutomatizacionController(ReglaAutomatizacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReglaAutomatizacionDTO> listar() {
        return service.listar();
    }

    @PostMapping
    public ResponseEntity<ReglaAutomatizacionDTO> crear(
            @Valid @RequestBody ReglaAutomatizacionRequest request, Authentication authentication) {
        return ResponseEntity.status(201)
            .body(service.crear(request, (Usuario) authentication.getPrincipal()));
    }

    @PutMapping("/{id}")
    public ReglaAutomatizacionDTO actualizar(@PathVariable UUID id,
            @Valid @RequestBody ReglaAutomatizacionRequest request) {
        return service.actualizar(id, request);
    }
}
