package com.vect.vect.repository;

import com.vect.vect.entity.MensajeIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MensajeIncidenciaRepository extends JpaRepository<MensajeIncidencia, UUID> {

    List<MensajeIncidencia> findAllByIncidencia_IdAndCanalOrderByCreadoEnAsc(
        UUID incidenciaId, MensajeIncidencia.CanalMensaje canal);

    List<MensajeIncidencia> findAllByCanalOrderByCreadoEnAsc(MensajeIncidencia.CanalMensaje canal);
}
