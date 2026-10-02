package com.vect.vect.controller;

import com.vect.vect.dto.response.MensajeChatDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.MensajeIncidenciaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat-supervision")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR')")
public class ChatSupervisionController {

    private final MensajeIncidenciaService mensajeService;

    public ChatSupervisionController(MensajeIncidenciaService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping("/tecnico-empleado")
    public List<MensajeChatDTO> tecnicoEmpleado(Authentication authentication) {
        return mensajeService.listarChatTecnicoEmpleado((Usuario) authentication.getPrincipal());
    }
}
