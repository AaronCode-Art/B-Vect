package com.vect.vect.repository;

import com.vect.vect.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    List<Usuario> findAllByOrderByApellidosAscNombresAsc();
    List<Usuario> findAllByActivoTrueOrderByApellidosAscNombresAsc();
    List<Usuario> findAllByRolAndActivoTrueOrderByApellidosAscNombresAsc(Usuario.RolUsuario rol);
    boolean existsByCorreoIgnoreCase(String correo);
}
