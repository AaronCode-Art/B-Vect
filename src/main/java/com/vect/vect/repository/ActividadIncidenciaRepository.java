package com.vect.vect.repository;

import com.vect.vect.entity.ActividadIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActividadIncidenciaRepository extends JpaRepository<ActividadIncidencia, UUID> {

    List<ActividadIncidencia> findAllByIncidencia_IdOrderByCreadoEnDesc(UUID incidenciaId);

    List<ActividadIncidencia> findAllByTecnico_IdOrderByCreadoEnDesc(UUID tecnicoId);
}
