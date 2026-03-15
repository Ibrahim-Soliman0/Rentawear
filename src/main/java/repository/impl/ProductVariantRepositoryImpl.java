package repository.impl;

import jakarta.persistence.EntityManager;
import entity.ProductVariant;
import repository.ProductVariantRepository;
import util.JPAUtil;

import java.util.List;

public class ProductVariantRepositoryImpl extends BaseRepositoryImpl<ProductVariant>
        implements ProductVariantRepository {

    public ProductVariantRepositoryImpl() {
        super(ProductVariant.class);
    }

    // All variants for a product , used by PDP and QV size selectors
    @Override
    public List<ProductVariant> findByProductId(int productId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNamedQuery("ProductVariant.findByProductId", ProductVariant.class)
                    .setParameter("pid", productId)
                    .getResultList();
        }
    }

    // Distinct color strings used by upload validation to confirm
    // a color exists before accepting image uploads for it
    @Override
    public List<String> findDistinctColorsByProductId(int productId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNamedQuery(
                            "ProductVariant.findDistinctColorsByProductId", String.class)
                    .setParameter("pid", productId)
                    .getResultList();
        }
    }

    // Sizes for one specific color
    @Override
    public List<String> findSizesByProductIdAndColor(int productId, String color) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNamedQuery(
                            "ProductVariant.findSizesByProductIdAndColor", String.class)
                    .setParameter("pid",   productId)
                    .setParameter("color", color)
                    .getResultList();
        }
    }


    // Deletes all size rows for a color  called by deleteColorImages in service
    @Override
    public void deleteByProductIdAndColor(int productId, String color) {
        try {
            em().createNamedQuery("ProductVariant.deleteByProductIdAndColor")
                    .setParameter("pid",   productId)
                    .setParameter("color", color)
                    .executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete variants for color: " + color, e);
        }
    }
}

