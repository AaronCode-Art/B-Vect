package com.vect.vect.repository;

import com.vect.vect.entity.ComponenteActivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ComponenteActivoRepository extends JpaRepository<ComponenteActivo, UUID> {

    List<ComponenteActivo> findAllByActivo_IdAndRetiradoEnIsNullOrderByInstaladoEnDesc(UUID activoId);

    boolean existsByActivo_IdAndComponente_IdAndRetiradoEnIsNull(UUID activoId, UUID componenteId);
}
