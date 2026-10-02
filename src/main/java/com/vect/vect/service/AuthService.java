package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.LoginRequest;
import com.vect.vect.dto.response.AuthResponse;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.AuthMapper;
import com.vect.vect.repository.UsuarioRepository;
import com.vect.vect.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final long accessExpirationMs;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthMapper authMapper,
                       @Value("${jwt.access-expiration-ms}") long accessExpirationMs) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.accessExpirationMs = accessExpirationMs;
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(request.correo())
            .orElseThrow(() -> ApiException.unauthorized("CREDENCIALES_INVALIDAS", "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getHashContrasena())) {
            throw ApiException.unauthorized("CREDENCIALES_INVALIDAS", "Credenciales inválidas");
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw ApiException.unauthorized("USUARIO_INACTIVO", "Usuario inactivo");
        }

        usuario.setUltimoAccesoEn(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String accessToken = jwtService.generateAccessToken(usuario.getId(), usuario.getCorreo());
        String refreshToken = jwtService.generateRefreshToken(usuario.getId(), usuario.getCorreo());

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            accessExpirationMs,
            authMapper.toDto(usuario)
        );
    }

    public AuthResponse refresh(String refreshToken) {
        Claims claims;
        UUID userId;
        try {
            claims = jwtService.parseRefreshToken(refreshToken);
            userId = jwtService.getUserId(claims);
        } catch (RuntimeException exception) {
            throw ApiException.unauthorized("REFRESH_TOKEN_INVALIDO", "El token de renovación no es válido");
        }

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> ApiException.unauthorized("REFRESH_TOKEN_INVALIDO", "El token de renovación no es válido"));
        if (!Boolean.TRUE.equals(usuario.getActivo())
                || !usuario.getCorreo().equalsIgnoreCase(jwtService.getEmail(claims))) {
            throw ApiException.unauthorized("REFRESH_TOKEN_INVALIDO", "El token de renovación no es válido");
        }

        String newAccessToken = jwtService.generateAccessToken(usuario.getId(), usuario.getCorreo());
        String newRefreshToken = jwtService.generateRefreshToken(usuario.getId(), usuario.getCorreo());

        return new AuthResponse(
            newAccessToken,
            newRefreshToken,
            "Bearer",
            accessExpirationMs,
            authMapper.toDto(usuario)
        );
    }
}
