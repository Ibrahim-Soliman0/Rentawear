package repository.impl;

import entity.ProductImage;
import repository.ProductImageRepository;

import java.util.List;

public class ProductImageRepositoryImpl extends BaseRepositoryImpl<ProductImage>
        implements ProductImageRepository {

    public ProductImageRepositoryImpl() {
        super(ProductImage.class);
    }

    @Override
    public List<ProductImage> findByProductId(int productId) {
        return em().createNamedQuery("ProductImage.findByProductId", ProductImage.class)
                .setParameter("pid", productId)
                .getResultList();
    }

    @Override
    public List<ProductImage> findByProductIdAndColor(int productId, String color) {
        return em().createNamedQuery("ProductImage.findByProductIdAndColor", ProductImage.class)
                .setParameter("pid",   productId)
                .setParameter("color", color)
                .getResultList();
    }

    @Override
    public List<ProductImage> findPrimaryPerColor(int productId) {
        return em().createNamedQuery("ProductImage.findPrimaryPerColor", ProductImage.class)
                .setParameter("pid", productId)
                .getResultList();
    }

    @Override
    public List<ProductImage> findPrimaryPerColorForProducts(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        return em().createNamedQuery(
                        "ProductImage.findPrimaryPerColorForProducts", ProductImage.class)
                .setParameter("pids", productIds)
                .getResultList();
    }

    @Override
    public long countByProductIdAndColor(int productId, String color) {
        return em().createNamedQuery("ProductImage.countByProductIdAndColor", Long.class)
                .setParameter("pid",   productId)
                .setParameter("color", color)
                .getSingleResult();
    }

    @Override
    public void deleteByProductIdAndColor(int productId, String color) {
        em().createNamedQuery("ProductImage.deleteByProductIdAndColor")
                .setParameter("pid",   productId)
                .setParameter("color", color)
                .executeUpdate();
    }
}