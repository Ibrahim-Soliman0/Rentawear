package repository;

import model.ProductImage;

import java.util.List;

public interface ProductImageRepository extends Repository<ProductImage> {

    // All images for a product ordered by color then id,
    // service layer groups these into a LinkedHashMap by color
    List<ProductImage> findByProductId(int productId);

    // All images for one specific color — used when checking whether
    // an uploaded image is the first for its color
    List<ProductImage> findByProductIdAndColor(int productId, String color);

    // Lowest-id image per color, used to build swatch previews in product listing pages
    // without loading the full image list
    @SuppressWarnings("unchecked")
    List<ProductImage> findPrimaryPerColor(int productId);

    // Count images for a product+color — upload servlet calls this
    // before inserting to decide whether to update Product.imageUrl
    long countByProductIdAndColor(int productId, String color);

    // Bulk delete by color — called by productImageService.deleteColorImages()
    void deleteByProductIdAndColor(int productId, String color);
}
