package com.vect.vect.repository;

import com.vect.vect.entity.MiembroConversacion;
import com.vect.vect.entity.MiembroConversacionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MiembroConversacionRepository extends JpaRepository<MiembroConversacion, MiembroConversacionId> {

    List<MiembroConversacion> findAllByUsuario_IdAndSalioEnIsNull(UUID usuarioId);

    List<MiembroConversacion> findAllByConversacion_IdAndSalioEnIsNull(UUID conversacionId);

    Optional<MiembroConversacion> findByConversacion_IdAndUsuario_IdAndSalioEnIsNull(
        UUID conversacionId, UUID usuarioId);
}
