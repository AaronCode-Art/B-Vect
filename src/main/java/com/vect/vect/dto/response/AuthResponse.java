package com.vect.vect.dto.response;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UsuarioDTO usuario
) {
    public record UsuarioDTO(
        UUID id,
        String nombres,
        String apellidos,
        String correo,
        String rol,
        String rolNombre
    ) {}
}
