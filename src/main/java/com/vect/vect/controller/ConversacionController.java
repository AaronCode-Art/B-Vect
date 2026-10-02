package com.vect.vect.controller;

import com.vect.vect.dto.request.ConversacionCreateRequest;
import com.vect.vect.dto.request.MensajeConversacionRequest;
import com.vect.vect.dto.response.ConversacionDTO;
import com.vect.vect.dto.response.ConversacionParticipanteDTO;
import com.vect.vect.dto.response.MensajeConversacionDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.ConversacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversaciones")
public class ConversacionController {

    private final ConversacionService conversacionService;

    public ConversacionController(ConversacionService conversacionService) {
        this.conversacionService = conversacionService;
    }

    @GetMapping
    public List<ConversacionDTO> listar(Authentication authentication) {
        return conversacionService.listar((Usuario) authentication.getPrincipal());
    }

    @GetMapping("/participantes")
    public List<ConversacionParticipanteDTO> listarParticipantes(Authentication authentication) {
        return conversacionService.listarParticipantes((Usuario) authentication.getPrincipal());
    }

    @PostMapping
    public ResponseEntity<ConversacionDTO> crear(@Valid @RequestBody ConversacionCreateRequest request,
                                                  Authentication authentication) {
        Usuario actor = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201).body(conversacionService.crear(request, actor));
    }

    @GetMapping("/{id}/mensajes")
    public List<MensajeConversacionDTO> mensajes(@PathVariable UUID id, Authentication authentication) {
        return conversacionService.listarMensajes(id, (Usuario) authentication.getPrincipal());
    }

    @PostMapping("/{id}/mensajes")
    public ResponseEntity<MensajeConversacionDTO> enviar(@PathVariable UUID id,
            @Valid @RequestBody MensajeConversacionRequest request, Authentication authentication) {
        Usuario actor = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201).body(conversacionService.enviar(id, request, actor));
    }

    @PostMapping("/{id}/mensajes/archivo")
    public ResponseEntity<MensajeConversacionDTO> enviarArchivo(@PathVariable UUID id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) String cuerpo,
            Authentication authentication) throws java.io.IOException {
        Usuario actor = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(201)
            .body(conversacionService.enviarArchivo(id, archivo, cuerpo, actor));
    }
}
