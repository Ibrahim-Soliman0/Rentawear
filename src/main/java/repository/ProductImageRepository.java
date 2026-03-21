package repository;

import entity.ProductImage;

import java.util.List;

public interface ProductImageRepository extends Repository<ProductImage> {

    //Single-product reads
    // All images ordered by id ASC, service groups into imagesByColor map
    List<ProductImage> findByProductId(int productId);

    List<ProductImage> findByProductIdAndColor(int productId, String color);

    // Lowest-id image per colour — used to build swatch previews
    List<ProductImage> findPrimaryPerColor(int productId);

    //Batch reads
    // Lowest-id image per colour for multiple products in one query
    List<ProductImage> findPrimaryPerColorForProducts(List<Integer> productIds);

    // Counts
    long countByProductIdAndColor(int productId, String color);

    //Writes
    void deleteByProductIdAndColor(int productId, String color);

    // Deletes all images for a product
    void deleteByProductId(int productId);
}