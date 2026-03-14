package repository.impl;

import jakarta.persistence.EntityManager;
import util.EntityManagerContext;
import repository.Repository;

import java.util.List;

import java.util.List;

public abstract class BaseRepositoryImpl<T> implements Repository<T> {

    private final Class<T> entityClass;

    protected BaseRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected EntityManager em() {
        return EntityManagerContext.get();
    }

    @Override
    public T findById(Integer id) {
        return em().find(entityClass, id);
    }

    @Override
    public T save(T entity) {
        T managedEntity = em().merge(entity);
        em().flush();
        return managedEntity;
    }

    @Override
    public void delete(T entity) {
        T managedEntity = entity;

        if (!em().contains(entity)) {
            managedEntity = em().merge(entity);
        }

        em().remove(managedEntity);
    }

    public List<T> findAll() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                    .getResultList();
        }
    }

    public List<T> findAll() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                    .getResultList();
        }
    }
}