package com.vect.vect.repository;

import com.vect.vect.entity.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EvidenciaRepository extends JpaRepository<Evidencia, UUID> {

    List<Evidencia> findAllByIncidencia_IdOrderByCreadoEnDesc(UUID incidenciaId);

    List<Evidencia> findAllByOrderByCreadoEnDesc();
}
