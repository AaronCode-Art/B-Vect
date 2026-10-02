package com.vect.vect.repository;

import com.vect.vect.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EspecialidadRepository extends JpaRepository<Especialidad, UUID> {

    List<Especialidad> findAllByOrderByNombreAsc();
}
