package repository.impl;

import entity.ProductVariant;
import repository.ProductVariantRepository;

import java.util.List;

public class ProductVariantRepositoryImpl extends BaseRepositoryImpl<ProductVariant>
        implements ProductVariantRepository {

    public ProductVariantRepositoryImpl() {
        super(ProductVariant.class);
    }

    @Override
    public List<ProductVariant> findByProductId(int productId) {
        return em().createNamedQuery("ProductVariant.findByProductId", ProductVariant.class)
                .setParameter("pid", productId)
                .getResultList();
    }

    @Override
    public List<String> findDistinctColorsByProductId(int productId) {
        return em().createNamedQuery(
                        "ProductVariant.findDistinctColorsByProductId", String.class)
                .setParameter("pid", productId)
                .getResultList();
    }

    @Override
    public List<String> findSizesByProductIdAndColor(int productId, String color) {
        return em().createNamedQuery(
                        "ProductVariant.findSizesByProductIdAndColor", String.class)
                .setParameter("pid",   productId)
                .setParameter("color", color)
                .getResultList();
    }

    @Override
    public List<String> findAvailableSizesByProductIdAndColor(int productId, String color) {
        return em().createNamedQuery(
                        "ProductVariant.findAvailableSizesByProductIdAndColor", String.class)
                .setParameter("pid",   productId)
                .setParameter("color", color)
                .getResultList();
    }

    @Override
    public List<ProductVariant> findByProductIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        return em().createNamedQuery("ProductVariant.findByProductIds", ProductVariant.class)
                .setParameter("pids", productIds)
                .getResultList();
    }

    @Override
    public List<Integer> findIdsByProductIdAndColor(int productId, String color) {
        return em().createNamedQuery(
                        "ProductVariant.findIdsByProductIdAndColor", Integer.class)
                .setParameter("pid",   productId)
                .setParameter("color", color)
                .getResultList();
    }

    // ── Soft-delete ───────────────────────────────────────────────────────────

    @Override
    public void softDeleteByProductIdAndColor(int productId, String color) {
        em().createNamedQuery("ProductVariant.softDeleteByProductIdAndColor")
                .setParameter("pid",   productId)
                .setParameter("color", color)
                .executeUpdate();
    }

    @Override
    public void softDeleteByProductId(int productId) {
        em().createNamedQuery("ProductVariant.softDeleteByProductId")
                .setParameter("pid", productId)
                .executeUpdate();
    }

    // ── Legacy hard-delete ────────────────────────────────────────────────────

    @Override
    public void deleteByProductIdAndColor(int productId, String color) {
        em().createNamedQuery("ProductVariant.deleteByProductIdAndColor")
                .setParameter("pid",   productId)
                .setParameter("color", color)
                .executeUpdate();
    }

    @Override
    public void deleteByProductId(int productId) {
        em().createNamedQuery("ProductVariant.deleteByProductId")
                .setParameter("pid", productId)
                .executeUpdate();
    }
}