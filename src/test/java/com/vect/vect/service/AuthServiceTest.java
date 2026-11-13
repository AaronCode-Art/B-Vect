package com.vect.vect.service;

import com.vect.vect.dto.request.LoginRequest;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.AuthMapper;
import com.vect.vect.repository.UsuarioRepository;
import com.vect.vect.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthMapper authMapper;
    @Mock private MfaService mfaService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, passwordEncoder, jwtService, authMapper, mfaService, 900_000L);
    }

    @Test
    void loginRequiresEnrollmentForAccountWithoutMfa() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByCorreoIgnoreCase("user@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "password-hash")).thenReturn(true);
        when(mfaService.generateSecret()).thenReturn("BASE32SECRET");
        when(jwtService.generateMfaChallengeToken(usuario.getId(), usuario.getCorreo(), true, "BASE32SECRET"))
            .thenReturn("setup-challenge");
        when(mfaService.provisioningUri("BASE32SECRET", usuario.getCorreo(), "VECT"))
            .thenReturn("otpauth://totp/VECT:user%40example.com?secret=BASE32SECRET");

        var response = authService.login(new LoginRequest("user@example.com", "password"));

        assertThat(response.status()).isEqualTo("MFA_SETUP_REQUIRED");
        assertThat(response.challengeToken()).isEqualTo("setup-challenge");
        assertThat(response.otpauthUri()).startsWith("otpauth://totp/");
        assertThat(response.auth()).isNull();
    }

    @Test
    void loginRequiresCodeForAccountAlreadyEnrolled() {
        Usuario usuario = usuario();
        usuario.setMfaSecretEncrypted("encrypted-secret");
        when(usuarioRepository.findByCorreoIgnoreCase("user@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "password-hash")).thenReturn(true);
        when(jwtService.generateMfaChallengeToken(usuario.getId(), usuario.getCorreo(), false, null))
            .thenReturn("code-challenge");

        var response = authService.login(new LoginRequest("user@example.com", "password"));

        assertThat(response.status()).isEqualTo("MFA_CODE_REQUIRED");
        assertThat(response.challengeToken()).isEqualTo("code-challenge");
        assertThat(response.otpauthUri()).isNull();
        assertThat(response.auth()).isNull();
    }

    @Test
    void lockedAccountCannotRestartMfaEnrollmentToBypassLockout() {
        Usuario usuario = usuario();
        usuario.setMfaLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findByCorreoIgnoreCase("user@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "password-hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "password")))
            .hasMessageContaining("Espera 15 minutos");

        verifyNoInteractions(mfaService);
    }

    @Test
    void refreshRequiresMfaToRemainEnrolled() {
        UUID userId = UUID.randomUUID();
        var claims = mock(io.jsonwebtoken.Claims.class);
        Usuario usuario = usuario();
        usuario.setMfaSecretEncrypted(null);
        when(jwtService.parseRefreshToken("refresh-token")).thenReturn(claims);
        when(jwtService.getUserId(claims)).thenReturn(userId);
        when(claims.get("mfa_verified", Boolean.class)).thenReturn(true);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.refresh("refresh-token"))
            .hasMessageContaining("token de renovación");

        verify(jwtService, never()).generateAccessToken(any(), any());
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setCorreo("user@example.com");
        usuario.setActivo(true);
        usuario.setHashContrasena("password-hash");
        return usuario;
    }
}
