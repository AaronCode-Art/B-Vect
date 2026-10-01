package com.vect.vect.service;

import com.cloudinary.Cloudinary;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.EvidenciaMapper;
import com.vect.vect.repository.EvidenciaRepository;
import com.vect.vect.repository.IncidenciaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EvidenciaServiceCloudinaryTest {

    @Test
    void rejectsUploadWhenCloudinaryCredentialsAreNotConfigured() {
        EvidenciaRepository evidenciaRepository = mock(EvidenciaRepository.class);
        IncidenciaRepository incidenciaRepository = mock(IncidenciaRepository.class);
        Cloudinary cloudinary = mock(Cloudinary.class);
        UUID actorId = UUID.randomUUID();
        Usuario actor = new Usuario();
        actor.setId(actorId);
        actor.setRol(Usuario.RolUsuario.EMPLEADO);

        Incidencia incidencia = new Incidencia();
        incidencia.setEstado(Incidencia.EstadoIncidencia.REGISTRADA);
        incidencia.setReportante(actor);
        when(incidenciaRepository.findById(any())).thenReturn(Optional.of(incidencia));

        EvidenciaService service = new EvidenciaService(evidenciaRepository, incidenciaRepository,
            cloudinary, mock(DatabaseActorContext.class), mock(EvidenciaMapper.class),
            "vect/evidencias", "", "", "");
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo", "captura.png", "image/png", new byte[] {1, 2, 3});

        assertThatThrownBy(() -> service.subir(UUID.randomUUID(), archivo, actor))
            .isInstanceOf(ApiException.class)
            .satisfies(error -> assertThat(((ApiException) error).getStatus().value()).isEqualTo(503));

        verifyNoInteractions(cloudinary);
        verifyNoInteractions(evidenciaRepository);
    }
}
