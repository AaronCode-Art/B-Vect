package com.vect.vect.controller;

import com.vect.vect.dto.response.HistorialIncidenciaDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.HistorialIncidenciaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    private final HistorialIncidenciaService historialService;

    public HistorialController(HistorialIncidenciaService historialService) {
        this.historialService = historialService;
    }

    @GetMapping("/tecnicos/{tecnicoId}/incidencias")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR', 'TECNICO')")
    public List<HistorialIncidenciaDTO> historialTecnico(@PathVariable UUID tecnicoId,
                                                          Authentication authentication) {
        return historialService.listarDelTecnico(tecnicoId, (Usuario) authentication.getPrincipal());
    }
}
