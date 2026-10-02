package com.vect.vect.repository;

import com.vect.vect.entity.MensajeConversacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MensajeConversacionRepository extends JpaRepository<MensajeConversacion, UUID> {

    List<MensajeConversacion> findAllByConversacion_IdAndEliminadoEnIsNullOrderByCreadoEnAsc(UUID conversacionId);
}
