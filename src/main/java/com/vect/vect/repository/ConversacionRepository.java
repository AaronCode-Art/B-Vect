package com.vect.vect.repository;

import com.vect.vect.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversacionRepository extends JpaRepository<Conversacion, UUID> {

    List<Conversacion> findAllByOrderByActualizadoEnDesc();
}
