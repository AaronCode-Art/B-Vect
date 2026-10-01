package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.UsuarioRequest;
import com.vect.vect.dto.response.UsuarioDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.UsuarioMapper;
import com.vect.vect.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private PasswordEncoder passwordEncoder;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, usuarioMapper, passwordEncoder);
    }

    @Test
    void supervisorCannotCreatePrivilegedUsers() {
        Usuario supervisor = usuario(Usuario.RolUsuario.SUPERVISOR);
        UsuarioRequest request = new UsuarioRequest("Nombre", "Apellido", "user@example.com",
            null, Usuario.RolUsuario.GERENCIA, true, "Password123");

        assertThatThrownBy(() -> service.crear(request, supervisor))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("No tiene permiso");

        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    void managerCanOnlyManageOperationalRoles() {
        Usuario gerencia = usuario(Usuario.RolUsuario.GERENCIA);
        UsuarioRequest request = new UsuarioRequest("Nombre", "Apellido", "user@example.com",
            null, Usuario.RolUsuario.ADMIN, true, "Password123");

        assertThatThrownBy(() -> service.crear(request, gerencia))
            .isInstanceOf(ApiException.class);

        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    void createsUsersWithNormalizedEmailAndEncodedPassword() {
        Usuario admin = usuario(Usuario.RolUsuario.ADMIN);
        UsuarioRequest request = new UsuarioRequest(" Ada ", " Lovelace ", " ADA@EXAMPLE.COM ",
            "123", Usuario.RolUsuario.TECNICO, true, "Password123");
        when(usuarioRepository.existsByCorreoIgnoreCase("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(
            new UsuarioDTO(null, "Ada", "Lovelace", "ada@example.com", "123", "TECNICO", true, true, null));

        service.crear(request, admin);

        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getCorreo()).isEqualTo("ada@example.com");
        assertThat(captor.getValue().getHashContrasena()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getDebeCambiarContrasena()).isTrue();
    }

    private Usuario usuario(Usuario.RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        return usuario;
    }
}
