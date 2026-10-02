package com.vect.vect.controller;

import com.vect.vect.dto.response.EvidenciaDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.EvidenciaService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/evidencias")
public class EvidenciaGlobalController {

    private final EvidenciaService evidenciaService;

    public EvidenciaGlobalController(EvidenciaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @GetMapping
    public List<EvidenciaDTO> listarTodas(Authentication authentication) {
        return evidenciaService.listarTodas((Usuario) authentication.getPrincipal());
    }
}
