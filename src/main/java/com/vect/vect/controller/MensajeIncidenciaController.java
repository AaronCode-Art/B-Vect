package com.vect.vect.controller;

import com.vect.vect.dto.request.MensajeChatRequest;
import com.vect.vect.dto.response.MensajeChatDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.MensajeIncidenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidencias/{id}/chat")
public class MensajeIncidenciaController {

    private final MensajeIncidenciaService mensajeService;

    public MensajeIncidenciaController(MensajeIncidenciaService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping
    public List<MensajeChatDTO> listar(@PathVariable UUID id, @RequestParam com.vect.vect.entity.MensajeIncidencia.CanalMensaje canal,
                                       Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return mensajeService.listarMensajes(id, canal, usuario);
    }

    @PostMapping
    public ResponseEntity<MensajeChatDTO> enviar(@PathVariable UUID id, @Valid @RequestBody MensajeChatRequest request, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201).body(mensajeService.enviarMensaje(id, request, usuario));
    }
}
