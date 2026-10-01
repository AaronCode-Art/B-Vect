package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.repository.IncidenciaRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ReporteServiceTest {

    @Test
    void rejectsUnsupportedReportRanges() {
        IncidenciaRepository repository = mock(IncidenciaRepository.class);
        ReporteService service = new ReporteService(repository);

        assertThatThrownBy(() -> service.generar(0)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> service.generar(367)).isInstanceOf(ApiException.class);
        verifyNoInteractions(repository);
    }
}
