package com.vect.vect.repository;

import com.vect.vect.entity.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long>,
        JpaSpecificationExecutor<RegistroAuditoria> {
}
