package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.TicketAdjuntoDAO;
import com.serviclient.crm.entity.TicketAdjunto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class TicketAdjuntoDAOImpl implements TicketAdjuntoDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TicketAdjunto> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(TicketAdjunto.class, id));
    }

    @Override
    public List<TicketAdjunto> findByTicketId(Long ticketId) {
        TypedQuery<TicketAdjunto> query = entityManager.createQuery(
                "SELECT a FROM TicketAdjunto a WHERE a.ticket.id = :ticketId", TicketAdjunto.class);
        query.setParameter("ticketId", ticketId);
        return query.getResultList();
    }

    @Override
    @Transactional
    public TicketAdjunto save(TicketAdjunto adjunto) {
        if (adjunto.getId() == null) {
            entityManager.persist(adjunto);
            return adjunto;
        } else {
            return entityManager.merge(adjunto);
        }
    }

    @Override
    public List<TicketAdjunto> findAll() {
        return entityManager.createQuery("SELECT a FROM TicketAdjunto a", TicketAdjunto.class).getResultList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(TicketAdjunto adjunto) {
        if (adjunto != null) {
            TicketAdjunto toRemove = entityManager.contains(adjunto) ? adjunto : entityManager.merge(adjunto);
            entityManager.remove(toRemove);
        }
    }
}
