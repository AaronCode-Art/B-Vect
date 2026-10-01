package com.vect.vect.dto.request;

import com.vect.vect.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
    @NotBlank @Size(max = 100) String nombres,
    @NotBlank @Size(max = 100) String apellidos,
    @NotBlank @Email @Size(max = 255) String correo,
    @Size(max = 30) String telefono,
    @NotNull Usuario.RolUsuario rol,
    Boolean activo,
    @Size(min = 8, max = 100) String contrasena
) {}
