package com.vect.vect.repository;

import com.vect.vect.entity.UsuarioEspecialidad;
import com.vect.vect.entity.UsuarioEspecialidadId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UsuarioEspecialidadRepository extends JpaRepository<UsuarioEspecialidad, UsuarioEspecialidadId> {

    List<UsuarioEspecialidad> findAllByUsuario_Id(UUID usuarioId);
}
