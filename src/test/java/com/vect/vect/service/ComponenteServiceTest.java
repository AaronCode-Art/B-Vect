package com.vect.vect.service;

import com.vect.vect.dto.request.MovimientoStockRequest;
import com.vect.vect.dto.response.MovimientoStockDTO;
import com.vect.vect.entity.Componente;
import com.vect.vect.entity.MovimientoStock;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.ComponenteMapper;
import com.vect.vect.repository.ComponenteRepository;
import com.vect.vect.repository.MovimientoStockRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ComponenteServiceTest {

    @Test
    void refreshesOnlyTheComponentAfterDatabaseStockFunction() {
        ComponenteRepository componenteRepository = mock(ComponenteRepository.class);
        MovimientoStockRepository movimientoRepository = mock(MovimientoStockRepository.class);
        ComponenteMapper componenteMapper = mock(ComponenteMapper.class);
        DatabaseActorContext actorContext = mock(DatabaseActorContext.class);
        EntityManager entityManager = mock(EntityManager.class);
        Query query = mock(Query.class);

        UUID componenteId = UUID.randomUUID();
        UUID movimientoId = UUID.randomUUID();
        Componente componente = new Componente();
        componente.setId(componenteId);
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setId(movimientoId);
        MovimientoStockDTO dto = mock(MovimientoStockDTO.class);
        Usuario actor = new Usuario();
        actor.setRol(Usuario.RolUsuario.ADMIN);

        when(componenteRepository.findById(componenteId)).thenReturn(Optional.of(componente));
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(movimientoId.toString());
        when(movimientoRepository.findById(movimientoId)).thenReturn(Optional.of(movimiento));
        when(componenteMapper.toMovimientoDto(movimiento)).thenReturn(dto);

        ComponenteService service = new ComponenteService(
            componenteRepository, movimientoRepository, componenteMapper, actorContext, entityManager);
        MovimientoStockDTO result = service.registrarMovimiento(componenteId,
            new MovimientoStockRequest(MovimientoStock.TipoMovimiento.ENTRADA, 1, "Reposición", null, null), actor);

        assertThat(result).isSameAs(dto);
        verify(entityManager).refresh(componente);
        verify(entityManager, never()).clear();
    }
}
