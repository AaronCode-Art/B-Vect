package com.vect.vect.repository;

import com.vect.vect.entity.SolicitudCambio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SolicitudCambioRepository extends JpaRepository<SolicitudCambio, UUID> {

    List<SolicitudCambio> findAllByOrderByCreadoEnDesc();

    List<SolicitudCambio> findAllBySolicitadoPor_IdOrderByCreadoEnDesc(UUID solicitanteId);
}
