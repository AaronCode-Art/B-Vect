package com.vect.vect.service;

import com.cloudinary.Cloudinary;
import com.vect.vect.dto.request.ConversacionCreateRequest;
import com.vect.vect.dto.response.ConversacionDTO;
import com.vect.vect.entity.Conversacion;
import com.vect.vect.entity.MensajeConversacion;
import com.vect.vect.entity.MiembroConversacion;
import com.vect.vect.entity.Usuario;
import com.vect.vect.repository.ConversacionRepository;
import com.vect.vect.repository.MensajeConversacionRepository;
import com.vect.vect.repository.MiembroConversacionRepository;
import com.vect.vect.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversacionServiceTest {

    @Mock private ConversacionRepository conversacionRepository;
    @Mock private MiembroConversacionRepository miembroRepository;
    @Mock private MensajeConversacionRepository mensajeRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private Cloudinary cloudinary;

    private ConversacionService service;

    @BeforeEach
    void setUp() {
        service = new ConversacionService(conversacionRepository, miembroRepository, mensajeRepository,
            usuarioRepository, cloudinary, "cloud", "key", "secret");
    }

    @Test
    void allowsEmployeeToCreateGroupWithParticipantsFromAnyRole() {
        Usuario employeeCreator = usuario(Usuario.RolUsuario.EMPLEADO);
        Usuario employeeParticipant = usuario(Usuario.RolUsuario.EMPLEADO);
        Usuario technicianParticipant = usuario(Usuario.RolUsuario.TECNICO);
        when(usuarioRepository.findById(employeeParticipant.getId())).thenReturn(Optional.of(employeeParticipant));
        when(usuarioRepository.findById(technicianParticipant.getId())).thenReturn(Optional.of(technicianParticipant));
        when(conversacionRepository.save(any(Conversacion.class))).thenAnswer(invocation -> {
            Conversacion conversation = invocation.getArgument(0);
            conversation.setId(UUID.randomUUID());
            return conversation;
        });
        when(miembroRepository.findAllByConversacion_IdAndSalioEnIsNull(any())).thenReturn(List.of());

        ConversacionDTO created = service.crear(new ConversacionCreateRequest(
            Conversacion.TipoConversacion.GRUPAL, "Soporte general",
            List.of(employeeParticipant.getId(), technicianParticipant.getId())), employeeCreator);

        ArgumentCaptor<Conversacion> conversationCaptor = ArgumentCaptor.forClass(Conversacion.class);
        verify(conversacionRepository).save(conversationCaptor.capture());
        assertThat(conversationCaptor.getValue().getTipo()).isEqualTo(Conversacion.TipoConversacion.GRUPAL);
        verify(miembroRepository, times(3)).save(any(MiembroConversacion.class));
        assertThat(created.id()).isNotNull();
    }

    @Test
    void allowsEmployeeToJoinAnExistingGroupConversation() {
        Usuario employee = usuario(Usuario.RolUsuario.EMPLEADO);
        Conversacion conversation = new Conversacion();
        conversation.setId(UUID.randomUUID());
        conversation.setTipo(Conversacion.TipoConversacion.GRUPAL);
        MiembroConversacion membership = new MiembroConversacion();
        membership.setConversacion(conversation);
        when(miembroRepository.findByConversacion_IdAndUsuario_IdAndSalioEnIsNull(
            conversation.getId(), employee.getId())).thenReturn(Optional.of(membership));

        assertThat(service.listarMensajes(conversation.getId(), employee)).isEmpty();
        verify(mensajeRepository).findAllByConversacion_IdAndEliminadoEnIsNullOrderByCreadoEnAsc(conversation.getId());
    }

    @Test
    void returnsConversationMessagesToEveryMemberRole() {
        Usuario employee = usuario(Usuario.RolUsuario.EMPLEADO);
        Usuario technician = usuario(Usuario.RolUsuario.TECNICO);
        Conversacion conversation = new Conversacion();
        conversation.setId(UUID.randomUUID());
        MiembroConversacion membership = new MiembroConversacion();
        membership.setConversacion(conversation);
        MensajeConversacion message = new MensajeConversacion();
        message.setId(UUID.randomUUID());
        message.setConversacion(conversation);
        message.setRemitente(technician);
        message.setCuerpo("Actualización del caso");
        when(miembroRepository.findByConversacion_IdAndUsuario_IdAndSalioEnIsNull(
            conversation.getId(), employee.getId())).thenReturn(Optional.of(membership));
        when(mensajeRepository.findAllByConversacion_IdAndEliminadoEnIsNullOrderByCreadoEnAsc(conversation.getId()))
            .thenReturn(List.of(message));

        assertThat(service.listarMensajes(conversation.getId(), employee))
            .extracting(com.vect.vect.dto.response.MensajeConversacionDTO::cuerpo)
            .containsExactly("Actualización del caso");
    }

    @Test
    void rejectsUnsupportedFileTypesBeforeCallingCloudStorage() {
        Usuario member = usuario(Usuario.RolUsuario.TECNICO);
        Conversacion conversation = new Conversacion();
        conversation.setTipo(Conversacion.TipoConversacion.PRIVADA);
        MiembroConversacion membership = new MiembroConversacion();
        membership.setConversacion(conversation);
        when(miembroRepository.findByConversacion_IdAndUsuario_IdAndSalioEnIsNull(
            conversation.getId(), member.getId())).thenReturn(Optional.of(membership));
        MockMultipartFile file = new MockMultipartFile(
            "archivo", "script.exe", "application/octet-stream", "unsafe".getBytes());

        assertThatThrownBy(() -> service.enviarArchivo(conversation.getId(), file, null, member))
            .isInstanceOf(com.vect.vect.common.exception.ApiException.class)
            .hasMessageContaining("Adjunte una imagen");

        verifyNoInteractions(cloudinary);
        verify(mensajeRepository, never()).save(any());
    }

    private Usuario usuario(Usuario.RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNombres("Usuario");
        usuario.setApellidos(rol.name());
        usuario.setCorreo(rol.name().toLowerCase() + "@example.test");
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }
}
