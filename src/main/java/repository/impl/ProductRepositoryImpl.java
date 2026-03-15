package repository.impl;

import jakarta.persistence.EntityManager;
import entity.Product;
import entity.enums.Gender;
import entity.Product;
import repository.ProductRepository;
import util.JPAUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ProductRepositoryImpl extends BaseRepositoryImpl<Product>
        implements ProductRepository {

    public ProductRepositoryImpl() {
        super(Product.class);
    }

    /**
     * Find Products added in the last n days
     * */
    @Override
    public List<Product> findNew(int limit, int days) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNamedQuery("Product.findNew", Product.class)
                    .setParameter("cutoff", cutoff)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    @Override
    public List<Product> findFiltered(String gender,
                                      List<Integer> categoryIds,
                                      Double minPrice,
                                      Double maxPrice,
                                      int limit,
                                      int offset) {
        try (EntityManager em = JPAUtil.getEntityManager()) {

            // Category IDs present - use the categories named query
            if (categoryIds != null && !categoryIds.isEmpty()) {
                return em.createNamedQuery("Product.findByCategories", Product.class)
                        .setParameter("ids",      categoryIds)
                        .setParameter("gender",   toGender(gender))
                        .setParameter("minPrice", toBigDecimal(minPrice))
                        .setParameter("maxPrice", toBigDecimal(maxPrice))
                        .setMaxResults(limit)
                        .setFirstResult(offset)
                        .getResultList();
            }

            // No category IDs - use the general filter named query
            return em.createNamedQuery("Product.findFiltered", Product.class)
                    .setParameter("gender",   toGender(gender))
                    .setParameter("minPrice", toBigDecimal(minPrice))
                    .setParameter("maxPrice", toBigDecimal(maxPrice))
                    .setMaxResults(limit)
                    .setFirstResult(offset)
                    .getResultList();
        }
    }

    @Override
    public long countFiltered(String gender,
                              List<Integer> categoryIds,
                              Double minPrice,
                              Double maxPrice) {
        try (EntityManager em = JPAUtil.getEntityManager()) {

            if (categoryIds != null && !categoryIds.isEmpty()) {
                return em.createNamedQuery("Product.countByCategories", Long.class)
                        .setParameter("ids",      categoryIds)
                        .setParameter("gender",   toGender(gender))
                        .setParameter("minPrice", toBigDecimal(minPrice))
                        .setParameter("maxPrice", toBigDecimal(maxPrice))
                        .getSingleResult();
            }

            return em.createNamedQuery("Product.countFiltered", Long.class)
                    .setParameter("gender",   toGender(gender))
                    .setParameter("minPrice", toBigDecimal(minPrice))
                    .setParameter("maxPrice", toBigDecimal(maxPrice))
                    .getSingleResult();
        }
    }

    @Override
    public List<Product> search(String q, String gender, int limit) {
        return searchPaged(q, gender, limit, 0);
    }

    @Override
    public List<Product> searchPaged(String q, String gender, int limit, int offset) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNamedQuery("Product.searchPaged", Product.class)
                    .setParameter("q",      "%" + q.toLowerCase() + "%")
                    .setParameter("gender", toGender(gender))
                    .setMaxResults(limit)
                    .setFirstResult(offset)
                    .getResultList();
        }
    }

    @Override
    public long countSearch(String q, String gender) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNamedQuery("Product.countSearch", Long.class)
                    .setParameter("q",      "%" + q.toLowerCase() + "%")
                    .setParameter("gender", toGender(gender))
                    .getSingleResult();
        }
    }

    @Override
    public List<Product> findByInterests(List<Integer> categoryIds,
                                         String gender,
                                         int limit) {
        if (categoryIds == null || categoryIds.isEmpty()) return List.of();
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNamedQuery("Product.findByInterests", Product.class)
                    .setParameter("ids",    categoryIds)
                    .setParameter("gender", toGender(gender))
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    @Override
    public Object[] getMinMaxPrice(String gender, List<Integer> categoryIds) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Object[] row = (Object[]) em.createQuery(
                        "SELECT MIN(p.basePrice), MAX(p.basePrice) FROM Product p LEFT JOIN p.category c " +
                                "WHERE c.id IN :ids AND (:gender IS NULL OR c.gender = :gender)")
                        .setParameter("ids", categoryIds)
                        .setParameter("gender", toGender(gender))
                        .getSingleResult();
                return row;
            }

            Object[] row = (Object[]) em.createQuery(
                    "SELECT MIN(p.basePrice), MAX(p.basePrice) FROM Product p LEFT JOIN p.category c " +
                            "WHERE (:gender IS NULL OR c.gender = :gender)")
                    .setParameter("gender", toGender(gender))
                    .getSingleResult();
            return row;
        }
    }
    // Helpers

    private Gender toGender(String gender) {
        if (gender == null || gender.isEmpty()) return null;
        try {
            return Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private java.math.BigDecimal toBigDecimal(Double value) {
        return value == null ? null : java.math.BigDecimal.valueOf(value);
    }
}