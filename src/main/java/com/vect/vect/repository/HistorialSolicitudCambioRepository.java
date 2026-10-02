package com.vect.vect.repository;

import com.vect.vect.entity.HistorialSolicitudCambio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistorialSolicitudCambioRepository extends JpaRepository<HistorialSolicitudCambio, Long> {

    List<HistorialSolicitudCambio> findAllByOrderByCreadoEnDesc();

    List<HistorialSolicitudCambio> findAllBySolicitud_IdOrderByCreadoEnDesc(UUID solicitudId);
}
