package com.vect.vect.controller;

import com.vect.vect.dto.request.ParametroSistemaRequest;
import com.vect.vect.dto.response.ParametroSistemaDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.ParametroSistemaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parametros")
@PreAuthorize("hasRole('ADMIN')")
public class ParametroSistemaController {

    private final ParametroSistemaService service;

    public ParametroSistemaController(ParametroSistemaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ParametroSistemaDTO> listar() {
        return service.listar();
    }

    @PutMapping("/{clave}")
    public ParametroSistemaDTO actualizar(@PathVariable String clave,
            @Valid @RequestBody ParametroSistemaRequest request, Authentication authentication) {
        return service.actualizar(clave, request.valor(), (Usuario) authentication.getPrincipal());
    }
}
