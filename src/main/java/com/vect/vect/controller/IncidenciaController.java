package com.vect.vect.controller;

import com.vect.vect.dto.request.AsignarTecnicoRequest;
import com.vect.vect.dto.request.CambiarEstadoRequest;
import com.vect.vect.dto.request.IncidenciaCreateRequest;
import com.vect.vect.dto.request.ActividadRequest;
import com.vect.vect.dto.response.ActividadIncidenciaDTO;
import com.vect.vect.dto.response.IncidenciaDTO;
import com.vect.vect.dto.response.HistorialIncidenciaDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.ActividadIncidenciaService;
import com.vect.vect.service.HistorialIncidenciaService;
import com.vect.vect.service.IncidenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;
    private final ActividadIncidenciaService actividadService;
    private final HistorialIncidenciaService historialService;

    public IncidenciaController(IncidenciaService incidenciaService,
                                ActividadIncidenciaService actividadService,
                                HistorialIncidenciaService historialService) {
        this.incidenciaService = incidenciaService;
        this.actividadService = actividadService;
        this.historialService = historialService;
    }

    @GetMapping
    public List<IncidenciaDTO> listar(Authentication authentication) {
        return incidenciaService.listar((Usuario) authentication.getPrincipal());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidenciaDTO> obtener(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(incidenciaService.obtener(id, (Usuario) authentication.getPrincipal()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR', 'EMPLEADO')")
    public ResponseEntity<IncidenciaDTO> crear(@Valid @RequestBody IncidenciaCreateRequest request, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201).body(incidenciaService.crear(request, usuario));
    }

    @PostMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR', 'TECNICO', 'EMPLEADO')")
    public ResponseEntity<IncidenciaDTO> cambiarEstado(@PathVariable UUID id, @Valid @RequestBody CambiarEstadoRequest request, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(incidenciaService.cambiarEstado(id, request.estadoCodigo(), request.motivo(), usuario));
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR')")
    public ResponseEntity<IncidenciaDTO> asignarTecnico(@PathVariable UUID id, @Valid @RequestBody AsignarTecnicoRequest request, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(incidenciaService.asignarTecnico(id, request.tecnicoId(), usuario));
    }

    @GetMapping("/{id}/historial")
    public List<HistorialIncidenciaDTO> historial(@PathVariable UUID id, Authentication authentication) {
        return historialService.listar(id, (Usuario) authentication.getPrincipal());
    }

    @GetMapping("/{id}/actividades")
    public List<ActividadIncidenciaDTO> actividades(@PathVariable UUID id, Authentication authentication) {
        return actividadService.listar(id, (Usuario) authentication.getPrincipal());
    }

    @PostMapping("/{id}/actividades")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<ActividadIncidenciaDTO> registrarActividad(@PathVariable UUID id,
            @Valid @RequestBody ActividadRequest request, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201).body(actividadService.registrar(id, request, usuario));
    }
}
