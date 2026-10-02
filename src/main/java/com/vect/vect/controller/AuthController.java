package com.vect.vect.controller;

import com.vect.vect.dto.request.LoginRequest;
import com.vect.vect.dto.request.RefreshTokenRequest;
import com.vect.vect.dto.response.AuthResponse;
import com.vect.vect.dto.response.UsuarioDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.UsuarioMapper;
import com.vect.vect.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioMapper usuarioMapper;

    public AuthController(AuthService authService, UsuarioMapper usuarioMapper) {
        this.authService = authService;
        this.usuarioMapper = usuarioMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @GetMapping("/me")
    public UsuarioDTO me(Authentication authentication) {
        return usuarioMapper.toDto((Usuario) authentication.getPrincipal());
    }
}
