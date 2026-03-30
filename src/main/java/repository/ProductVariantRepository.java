package repository;

import entity.ProductVariant;

import java.util.List;

public interface ProductVariantRepository extends Repository<ProductVariant> {

    // ── Single-product reads ──────────────────────────────────────────────────

    List<ProductVariant> findByProductId(int productId);

    List<String> findDistinctColorsByProductId(int productId);

    List<String> findSizesByProductIdAndColor(int productId, String color);

    /** In-stock sizes only (quantity > 0). */
    List<String> findAvailableSizesByProductIdAndColor(int productId, String color);

    // ── Batch reads ───────────────────────────────────────────────────────────

    List<ProductVariant> findByProductIds(List<Integer> productIds);

    List<Integer> findIdsByProductIdAndColor(int productId, String color);

    void softDeleteByProductIdAndColor(int productId, String color);

    void softDeleteByProductId(int productId);

    void deleteByProductIdAndColor(int productId, String color);

    void deleteByProductId(int productId);
}