package com.vect.vect.repository;

import com.vect.vect.entity.Activo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivoRepository extends JpaRepository<Activo, UUID> {

    List<Activo> findAllByOrderByCodigoAsc();
}
