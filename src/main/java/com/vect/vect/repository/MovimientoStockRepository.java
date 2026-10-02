package com.vect.vect.repository;

import com.vect.vect.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, UUID> {

    List<MovimientoStock> findAllByOrderByCreadoEnDesc();

    List<MovimientoStock> findAllByComponente_IdOrderByCreadoEnDesc(UUID componenteId);
}
