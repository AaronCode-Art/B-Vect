package com.vect.vect.repository;

import com.vect.vect.entity.ReglaAutomatizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReglaAutomatizacionRepository extends JpaRepository<ReglaAutomatizacion, UUID> {

    List<ReglaAutomatizacion> findAllByOrderByPrioridadAscNombreAsc();
}
