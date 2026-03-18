package repository;

import entity.ProductVariant;

import java.util.List;

public interface ProductVariantRepository extends Repository<ProductVariant> {

    //Single-product reads
    List<ProductVariant> findByProductId(int productId);

    List<String> findDistinctColorsByProductId(int productId);

    List<String> findSizesByProductIdAndColor(int productId, String color);

    // In-stock sizes only
    List<String> findAvailableSizesByProductIdAndColor(int productId, String color);

    // Batch reads
    List<ProductVariant> findByProductIds(List<Integer> productIds);

    // Writes
    // Deletes all size rows for a colour — called when removing a colour variant
    void deleteByProductIdAndColor(int productId, String color);

    // Deletes all variants for a product
    void deleteByProductId(int productId);
}