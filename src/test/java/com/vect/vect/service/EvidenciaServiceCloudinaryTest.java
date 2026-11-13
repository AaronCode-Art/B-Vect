package com.vect.vect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Url;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.response.EvidenciaDTO;
import com.vect.vect.entity.Evidencia;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.EvidenciaMapper;
import com.vect.vect.repository.EvidenciaRepository;
import com.vect.vect.repository.IncidenciaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EvidenciaServiceCloudinaryTest {

    @Test
    void employeeCanSeeEvidenceUploadedByTheAssignedTechnician() {
        EvidenciaRepository evidenciaRepository = mock(EvidenciaRepository.class);
        IncidenciaRepository incidenciaRepository = mock(IncidenciaRepository.class);
        Cloudinary cloudinary = mock(Cloudinary.class);
        EvidenciaMapper mapper = mock(EvidenciaMapper.class);
        Usuario empleado = new Usuario();
        empleado.setId(UUID.randomUUID());
        empleado.setRol(Usuario.RolUsuario.EMPLEADO);
        Usuario tecnico = new Usuario();
        tecnico.setId(UUID.randomUUID());
        tecnico.setRol(Usuario.RolUsuario.TECNICO);
        Incidencia incidencia = new Incidencia();
        incidencia.setReportante(empleado);
        incidencia.setTecnicoAsignado(tecnico);
        Evidencia evidencia = new Evidencia();
        evidencia.setSubidoPor(tecnico);
        evidencia.setIncidencia(incidencia);
        evidencia.setClaveAlmacenamiento("evidencia-tecnico");
        EvidenciaDTO dto = new EvidenciaDTO(UUID.randomUUID(), UUID.randomUUID(), tecnico.getId(),
            "Técnico", "captura.png", "image/png", 12L, "https://example.com/captura.png", null, null);
        Url cloudinaryUrl = mock(Url.class);
        when(incidenciaRepository.findById(any())).thenReturn(Optional.of(incidencia));
        when(evidenciaRepository.findAllByIncidencia_IdOrderByCreadoEnDesc(any())).thenReturn(List.of(evidencia));
        when(cloudinary.url()).thenReturn(cloudinaryUrl);
        when(cloudinaryUrl.secure(true)).thenReturn(cloudinaryUrl);
        when(cloudinaryUrl.resourceType("image")).thenReturn(cloudinaryUrl);
        when(cloudinaryUrl.generate("evidencia-tecnico")).thenReturn(dto.url());
        when(mapper.toDto(evidencia, dto.url())).thenReturn(dto);

        EvidenciaService service = new EvidenciaService(evidenciaRepository, incidenciaRepository,
            cloudinary, mock(DatabaseActorContext.class), mapper,
            "vect/evidencias", "cloud", "key", "secret");

        assertThat(service.listar(UUID.randomUUID(), empleado)).containsExactly(dto);
    }

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
