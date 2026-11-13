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
    void managerCanCreateUsersWithAnyRole() {
        Usuario gerencia = usuario(Usuario.RolUsuario.GERENCIA);
        UsuarioRequest request = new UsuarioRequest("Nombre", "Apellido", "user@example.com",
            null, Usuario.RolUsuario.ADMIN, true, "Password123");
        when(usuarioRepository.existsByCorreoIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(
            new UsuarioDTO(null, "Nombre", "Apellido", "user@example.com", null, "ADMIN", true, true, null));

        service.crear(request, gerencia);

        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(Usuario.RolUsuario.ADMIN);
    }

    @Test
    void supervisorCannotUpdateNonTechnicianUsers() {
        Usuario supervisor = usuario(Usuario.RolUsuario.SUPERVISOR);
        Usuario empleado = usuario(Usuario.RolUsuario.EMPLEADO);
        UsuarioRequest request = new UsuarioRequest("Nombre", "Apellido", "user@example.com",
            null, Usuario.RolUsuario.EMPLEADO, true, null);
        when(usuarioRepository.findById(any())).thenReturn(java.util.Optional.of(empleado));

        assertThatThrownBy(() -> service.actualizar(java.util.UUID.randomUUID(), request, supervisor))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("No tiene permiso");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void supervisorCannotChangeTechnicianActiveStatus() {
        Usuario supervisor = usuario(Usuario.RolUsuario.SUPERVISOR);
        Usuario tecnico = usuario(Usuario.RolUsuario.TECNICO);
        tecnico.setActivo(true);
        UsuarioRequest request = new UsuarioRequest("Nombre", "Apellido", "user@example.com",
            null, Usuario.RolUsuario.TECNICO, false, null);
        when(usuarioRepository.findById(any())).thenReturn(java.util.Optional.of(tecnico));

        assertThatThrownBy(() -> service.actualizar(java.util.UUID.randomUUID(), request, supervisor))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("No tiene permiso");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void managerCanChangeAnyUserRole() {
        Usuario gerencia = usuario(Usuario.RolUsuario.GERENCIA);
        Usuario adminUser = usuario(Usuario.RolUsuario.ADMIN);
        adminUser.setCorreo("admin@example.com");
        UsuarioRequest request = new UsuarioRequest("Nombre", "Apellido", "admin@example.com",
            null, Usuario.RolUsuario.EMPLEADO, true, null);
        when(usuarioRepository.findById(any())).thenReturn(java.util.Optional.of(adminUser));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(
            new UsuarioDTO(adminUser.getId(), "Nombre", "Apellido", "admin@example.com", null,
                "EMPLEADO", true, true, null));

        service.actualizar(adminUser.getId(), request, gerencia);

        assertThat(adminUser.getRol()).isEqualTo(Usuario.RolUsuario.EMPLEADO);
        verify(usuarioRepository).save(adminUser);
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

    @Test
    void managerCanResetAnotherUsersMfaAndRequireEnrollmentAgain() {
        Usuario manager = usuario(Usuario.RolUsuario.GERENCIA);
        Usuario target = usuario(Usuario.RolUsuario.EMPLEADO);
        target.setMfaSecretEncrypted("encrypted-secret");
        target.setMfaLastAcceptedStep(123L);
        target.setMfaFailedAttempts(3);
        target.setMfaLockedUntil(java.time.LocalDateTime.now());
        when(usuarioRepository.findById(any())).thenReturn(java.util.Optional.of(target));

        service.reiniciarMfa(java.util.UUID.randomUUID(), manager);

        assertThat(target.getMfaSecretEncrypted()).isNull();
        assertThat(target.getMfaLastAcceptedStep()).isNull();
        assertThat(target.getMfaFailedAttempts()).isZero();
        assertThat(target.getMfaLockedUntil()).isNull();
    }

    @Test
    void supervisorCannotResetMfa() {
        Usuario supervisor = usuario(Usuario.RolUsuario.SUPERVISOR);

        assertThatThrownBy(() -> service.reiniciarMfa(java.util.UUID.randomUUID(), supervisor))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Solo Administración o Gerencia");

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void userCannotResetTheirOwnMfa() {
        Usuario admin = usuario(Usuario.RolUsuario.ADMIN);
        admin.setId(java.util.UUID.randomUUID());

        assertThatThrownBy(() -> service.reiniciarMfa(admin.getId(), admin))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Otro usuario");

        verifyNoInteractions(usuarioRepository);
    }

    private Usuario usuario(Usuario.RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        return usuario;
    }
}
