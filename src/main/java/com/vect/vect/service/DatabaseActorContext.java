package com.vect.vect.service;

import com.vect.vect.entity.Usuario;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

@Component
public class DatabaseActorContext {

    private final EntityManager entityManager;

    public DatabaseActorContext(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setActor(Usuario actor) {
        entityManager.createNativeQuery("select set_config('app.user_id', :userId, true)")
            .setParameter("userId", actor.getId().toString())
            .getSingleResult();
    }

    public void setTransition(Usuario actor, String motivo) {
        setActor(actor);
        entityManager.createNativeQuery("select set_config('app.motivo_transicion', :motivo, true)")
            .setParameter("motivo", motivo)
            .getSingleResult();
    }
}
