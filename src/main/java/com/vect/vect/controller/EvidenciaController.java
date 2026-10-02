package com.vect.vect.controller;

import com.vect.vect.dto.response.EvidenciaDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.EvidenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidencias/{id}/evidencias")
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    public EvidenciaController(EvidenciaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @GetMapping
    public List<EvidenciaDTO> listar(@PathVariable UUID id, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return evidenciaService.listar(id, usuario);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR', 'TECNICO', 'EMPLEADO')")
    public ResponseEntity<EvidenciaDTO> subir(@PathVariable UUID id, @RequestParam("archivo") MultipartFile archivo, Authentication authentication) throws IOException {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201).body(evidenciaService.subir(id, archivo, usuario));
    }
}
