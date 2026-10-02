package com.vect.vect.repository;

import com.vect.vect.entity.Componente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ComponenteRepository extends JpaRepository<Componente, UUID> {
}
