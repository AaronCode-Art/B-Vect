package com.vect.vect.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioDTO(
    UUID id,
    String nombres,
    String apellidos,
    String correo,
    String telefono,
    String rol,
    boolean activo,
    boolean debeCambiarContrasena,
    LocalDateTime ultimoAccesoEn
) {}
