package com.vect.vect.repository;

import com.vect.vect.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    List<Usuario> findAllByOrderByApellidosAscNombresAsc();
    List<Usuario> findAllByActivoTrueOrderByApellidosAscNombresAsc();
    List<Usuario> findAllByRolAndActivoTrueOrderByApellidosAscNombresAsc(Usuario.RolUsuario rol);
    long countByRolAndActivoTrue(Usuario.RolUsuario rol);
    boolean existsByCorreoIgnoreCase(String correo);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = """
        UPDATE vect.usuarios
        SET mfa_failed_attempts = mfa_failed_attempts + 1,
            mfa_locked_until = CASE
                WHEN mfa_failed_attempts + 1 >= 5 THEN CURRENT_TIMESTAMP + INTERVAL '15 minutes'
                ELSE mfa_locked_until
            END
        WHERE id = :id
        """, nativeQuery = true)
    void recordMfaFailure(@Param("id") UUID id);

    @Modifying
    @Query(value = """
        UPDATE vect.usuarios
        SET mfa_last_accepted_step = :step,
            mfa_failed_attempts = 0,
            mfa_locked_until = NULL
        WHERE id = :id
          AND (mfa_last_accepted_step IS NULL OR mfa_last_accepted_step < :step)
          AND (mfa_locked_until IS NULL OR mfa_locked_until <= CURRENT_TIMESTAMP)
        """, nativeQuery = true)
    int consumeMfaStep(@Param("id") UUID id, @Param("step") long step);
}
