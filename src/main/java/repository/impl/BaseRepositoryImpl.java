package repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import repository.Repository;
import util.JPAUtil;

public abstract class BaseRepositoryImpl<T> implements Repository<T> {

    private final Class<T> entityClass;

    protected BaseRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T findById(Integer id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.find(entityClass, id);
        }
    }

    @Override
    public T save(T entity) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();
                T managedEntity = em.merge(entity);
                tx.commit();
                return managedEntity;
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void delete(T entity) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();
                T managedEntity = em.merge(entity);
                em.remove(managedEntity);
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw new RuntimeException(e);
            }
        }
    }
}