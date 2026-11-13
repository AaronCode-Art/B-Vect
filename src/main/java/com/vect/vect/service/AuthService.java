package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.LoginRequest;
import com.vect.vect.dto.request.VerifyMfaRequest;
import com.vect.vect.dto.response.AuthFlowResponse;
import com.vect.vect.dto.response.AuthResponse;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.AuthMapper;
import com.vect.vect.repository.UsuarioRepository;
import com.vect.vect.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final MfaService mfaService;
    private final long accessExpirationMs;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthMapper authMapper, MfaService mfaService,
                       @Value("${jwt.access-expiration-ms}") long accessExpirationMs) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.mfaService = mfaService;
        this.accessExpirationMs = accessExpirationMs;
    }

    public AuthFlowResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(request.correo())
            .orElseThrow(() -> ApiException.unauthorized("CREDENCIALES_INVALIDAS", "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getHashContrasena())) {
            throw ApiException.unauthorized("CREDENCIALES_INVALIDAS", "Credenciales inválidas");
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw ApiException.unauthorized("USUARIO_INACTIVO", "Usuario inactivo");
        }

        if (usuario.getMfaLockedUntil() != null && usuario.getMfaLockedUntil().isAfter(LocalDateTime.now())) {
            throw ApiException.unauthorized("MFA_TEMPORALMENTE_BLOQUEADO",
                "Se excedieron los intentos. Espera 15 minutos e inténtalo de nuevo.");
        }

        if (usuario.getMfaSecretEncrypted() == null) {
            String secret = mfaService.generateSecret();
            String challenge = jwtService.generateMfaChallengeToken(
                usuario.getId(), usuario.getCorreo(), true, secret);
            return AuthFlowResponse.setupRequired(challenge,
                mfaService.provisioningUri(secret, usuario.getCorreo(), "VECT"));
        }

        String challenge = jwtService.generateMfaChallengeToken(
            usuario.getId(), usuario.getCorreo(), false, null);
        return AuthFlowResponse.codeRequired(challenge);
    }

    @Transactional
    public AuthFlowResponse verificarMfa(VerifyMfaRequest request) {
        var claims = parseMfaChallenge(request.challengeToken());
        UUID userId = jwtService.getUserId(claims);
        Usuario usuario = usuarioRepository.findById(userId)
            .filter(candidate -> Boolean.TRUE.equals(candidate.getActivo()))
            .orElseThrow(() -> ApiException.unauthorized("MFA_DESAFIO_INVALIDO", "El desafío MFA no es válido"));
        if (usuario.getMfaLockedUntil() != null && usuario.getMfaLockedUntil().isAfter(LocalDateTime.now())) {
            throw ApiException.unauthorized("MFA_TEMPORALMENTE_BLOQUEADO",
                "Se excedieron los intentos. Espera 15 minutos e inténtalo de nuevo.");
        }

        String tokenType = claims.get("token_type", String.class);
        boolean setup = "mfa_setup".equals(tokenType);
        if (setup && usuario.getMfaSecretEncrypted() != null) {
            throw ApiException.unauthorized("MFA_DESAFIO_INVALIDO", "La autenticación ya está configurada");
        }
        if (!setup && usuario.getMfaSecretEncrypted() == null) {
            throw ApiException.unauthorized("MFA_DESAFIO_INVALIDO", "Debes configurar la autenticación primero");
        }

        String secret = setup ? claims.get("mfa_secret", String.class)
            : mfaService.decrypt(usuario.getMfaSecretEncrypted());
        var matchedStep = mfaService.matchingStep(secret, request.code(), Instant.now());
        if (matchedStep.isEmpty()) {
            usuarioRepository.recordMfaFailure(userId);
            throw ApiException.unauthorized("CODIGO_MFA_INVALIDO", "El código de autenticación no es válido");
        }

        if (setup) {
            usuario.setMfaSecretEncrypted(mfaService.encrypt(secret));
        } else if (usuarioRepository.consumeMfaStep(userId, matchedStep.getAsLong()) != 1) {
            usuarioRepository.recordMfaFailure(userId);
            throw ApiException.unauthorized("CODIGO_MFA_INVALIDO", "El código ya fue usado o expiró");
        }
        usuario.setMfaLastAcceptedStep(matchedStep.getAsLong());
        usuario.setMfaFailedAttempts(0);
        usuario.setMfaLockedUntil(null);
        usuario.setUltimoAccesoEn(LocalDateTime.now());
        usuarioRepository.save(usuario);
        return AuthFlowResponse.authenticated(crearAuthResponse(usuario));
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
        if (!Boolean.TRUE.equals(claims.get("mfa_verified", Boolean.class))) {
            throw ApiException.unauthorized("REFRESH_TOKEN_INVALIDO",
                "Debes volver a iniciar sesión con autenticación de dos factores");
        }

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> ApiException.unauthorized("REFRESH_TOKEN_INVALIDO", "El token de renovación no es válido"));
        if (!Boolean.TRUE.equals(usuario.getActivo())
                || usuario.getMfaSecretEncrypted() == null
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

    private AuthResponse crearAuthResponse(Usuario usuario) {
        return new AuthResponse(
            jwtService.generateAccessToken(usuario.getId(), usuario.getCorreo()),
            jwtService.generateRefreshToken(usuario.getId(), usuario.getCorreo()),
            "Bearer",
            accessExpirationMs,
            authMapper.toDto(usuario)
        );
    }

    private io.jsonwebtoken.Claims parseMfaChallenge(String token) {
        try {
            return jwtService.parseMfaChallengeToken(token);
        } catch (RuntimeException exception) {
            throw ApiException.unauthorized("MFA_DESAFIO_INVALIDO",
                "El desafío MFA expiró o no es válido. Inicia sesión de nuevo.");
        }
    }
}
