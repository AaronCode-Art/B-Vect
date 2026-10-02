package com.vect.vect.repository;

import com.vect.vect.entity.HistorialIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistorialIncidenciaRepository extends JpaRepository<HistorialIncidencia, Long> {

    List<HistorialIncidencia> findAllByIncidencia_IdOrderByCreadoEnDesc(UUID incidenciaId);

    List<HistorialIncidencia> findAllByRealizadoPor_IdOrderByCreadoEnDesc(UUID usuarioId);

    List<HistorialIncidencia> findAllByIncidencia_TecnicoAsignado_IdOrderByCreadoEnDesc(UUID tecnicoId);
}
