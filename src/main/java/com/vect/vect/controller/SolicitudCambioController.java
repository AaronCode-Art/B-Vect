package com.vect.vect.controller;

import com.vect.vect.dto.request.EvaluarSolicitudRequest;
import com.vect.vect.dto.request.RechazarSolicitudRequest;
import com.vect.vect.dto.request.SolicitudCambioRequest;
import com.vect.vect.dto.response.SolicitudCambioDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.SolicitudCambioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/solicitudes-cambio")
public class SolicitudCambioController {

    private final SolicitudCambioService solicitudService;

    public SolicitudCambioController(SolicitudCambioService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR', 'TECNICO')")
    public List<SolicitudCambioDTO> listar(Authentication authentication) {
        return solicitudService.listar((Usuario) authentication.getPrincipal());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR', 'TECNICO')")
    public ResponseEntity<SolicitudCambioDTO> obtener(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(solicitudService.obtener(id, (Usuario) authentication.getPrincipal()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR', 'TECNICO')")
    public ResponseEntity<SolicitudCambioDTO> crear(@Valid @RequestBody SolicitudCambioRequest request, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201).body(solicitudService.crear(request, usuario));
    }

    @PostMapping("/{id}/evaluar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<SolicitudCambioDTO> evaluar(@PathVariable UUID id, @Valid @RequestBody EvaluarSolicitudRequest request, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(solicitudService.evaluar(id, request.aprobar(), request.comentario(), request.motivoRechazo(), usuario));
    }

    @PostMapping("/{id}/aprobar-gerencia")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ResponseEntity<SolicitudCambioDTO> aprobarGerencia(@PathVariable UUID id,
                                                               @RequestBody(required = false) EvaluarSolicitudRequest request,
                                                               Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(solicitudService.aprobarGerencia(id,
            request == null ? null : request.comentario(), usuario));
    }

    @PostMapping("/{id}/rechazar-gerencia")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA')")
    public ResponseEntity<SolicitudCambioDTO> rechazarGerencia(@PathVariable UUID id,
                                                                @Valid @RequestBody RechazarSolicitudRequest request,
                                                                Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(solicitudService.rechazarGerencia(id,
            request.motivoRechazo(), usuario));
    }

    @PostMapping("/{id}/ejecutar")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<SolicitudCambioDTO> ejecutar(@PathVariable UUID id, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(solicitudService.ejecutar(id, usuario));
    }
}
