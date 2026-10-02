package com.vect.vect.repository;

import com.vect.vect.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UbicacionRepository extends JpaRepository<Ubicacion, UUID> {

    List<Ubicacion> findAllByOrderByNombreAsc();
}
