package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.CategoriaTicketDAO;
import com.serviclient.crm.entity.CategoriaTicketEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoriaTicketDAOImpl implements CategoriaTicketDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<CategoriaTicketEntity> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(CategoriaTicketEntity.class, id));
    }

    @Override
    public List<CategoriaTicketEntity> findByEmpresaId(Long empresaId) {
        TypedQuery<CategoriaTicketEntity> query = entityManager.createQuery(
                "SELECT c FROM CategoriaTicketEntity c WHERE c.empresa.id = :empresaId", CategoriaTicketEntity.class);
        query.setParameter("empresaId", empresaId);
        return query.getResultList();
    }

    @Override
    public Optional<CategoriaTicketEntity> findByEmpresaIdAndNombre(Long empresaId, String nombre) {
        TypedQuery<CategoriaTicketEntity> query = entityManager.createQuery(
                "SELECT c FROM CategoriaTicketEntity c WHERE c.empresa.id = :empresaId AND c.nombre = :nombre", CategoriaTicketEntity.class);
        query.setParameter("empresaId", empresaId);
        query.setParameter("nombre", nombre);
        return query.getResultStream().findFirst();
    }

    @Override
    @Transactional
    public CategoriaTicketEntity save(CategoriaTicketEntity categoria) {
        if (categoria.getId() == null) {
            entityManager.persist(categoria);
            return categoria;
        } else {
            return entityManager.merge(categoria);
        }
    }

    @Override
    public List<CategoriaTicketEntity> findAll() {
        return entityManager.createQuery("SELECT c FROM CategoriaTicketEntity c", CategoriaTicketEntity.class).getResultList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(CategoriaTicketEntity categoria) {
        if (categoria != null) {
            CategoriaTicketEntity toRemove = entityManager.contains(categoria) ? categoria : entityManager.merge(categoria);
            entityManager.remove(toRemove);
        }
    }
}
