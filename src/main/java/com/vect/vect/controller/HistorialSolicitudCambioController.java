package com.vect.vect.controller;

import com.vect.vect.dto.response.HistorialSolicitudCambioDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.HistorialSolicitudCambioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/historial")
public class HistorialSolicitudCambioController {

    private final HistorialSolicitudCambioService service;

    public HistorialSolicitudCambioController(HistorialSolicitudCambioService service) {
        this.service = service;
    }

    @GetMapping("/aprobaciones")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR')")
    public List<HistorialSolicitudCambioDTO> aprobaciones(
            @RequestParam(required = false) UUID solicitudId, Authentication authentication) {
        return service.listar(solicitudId, (Usuario) authentication.getPrincipal());
    }
}
