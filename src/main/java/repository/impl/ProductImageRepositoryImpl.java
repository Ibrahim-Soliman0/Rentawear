package repository.impl;

import jakarta.persistence.EntityManager;
import entity.ProductImage;
import repository.ProductImageRepository;
import util.JPAUtil;

import java.util.List;

public class ProductImageRepositoryImpl extends BaseRepositoryImpl<ProductImage>
        implements ProductImageRepository {

    public ProductImageRepositoryImpl() {
        super(ProductImage.class);
    }

    // All images for a product ordered by color then id,
    // service layer groups these into a LinkedHashMap by color
    @Override
    public List<ProductImage> findByProductId(int productId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNamedQuery("ProductImage.findByProductId", ProductImage.class)
                    .setParameter("pid", productId)
                    .getResultList();
        }
    }

    // All images for one specific color, used when checking whether
    // an uploaded image is the first for its color
    @Override
    public List<ProductImage> findByProductIdAndColor(int productId, String color) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNamedQuery("ProductImage.findByProductIdAndColor", ProductImage.class)
                    .setParameter("pid",   productId)
                    .setParameter("color", color)
                    .getResultList();
        }
    }

    // Lowest-id image per color, used to build swatch previews in product listing pages
    // without loading the full image list

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductImage> findPrimaryPerColor(int productId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNativeQuery(
                            "SELECT * FROM product_images pi " +
                                    "WHERE pi.product_id = ?1 " +
                                    "AND pi.id = (" +
                                    "   SELECT MIN(pi2.id) FROM product_images pi2 " +
                                    "   WHERE pi2.product_id = pi.product_id " +
                                    "   AND pi2.color = pi.color" +
                                    ") ORDER BY pi.color ASC",
                            ProductImage.class)
                    .setParameter(1, productId)
                    .getResultList();
        }
    }
    // Count images for a product+color, upload servlet calls this
    // before inserting to decide whether to update Product.imageUrl
    @Override
    public long countByProductIdAndColor(int productId, String color) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createNamedQuery("ProductImage.countByProductIdAndColor", Long.class)
                    .setParameter("pid",   productId)
                    .setParameter("color", color)
                    .getSingleResult();
        }
    }

    // Bulk delete by color, called by productImageService.deleteColorImages()
    @Override
    public void deleteByProductIdAndColor(int productId, String color) {
        try {
            em().createNamedQuery("ProductImage.deleteByProductIdAndColor")
                    .setParameter("pid",   productId)
                    .setParameter("color", color)
                    .executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete images for color: " + color, e);
        }
    }
}