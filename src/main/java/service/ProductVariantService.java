package service;

import entity.ProductVariant;
import repository.ProductVariantRepository;
import repository.impl.ProductVariantRepositoryImpl;

import java.util.List;

// ProductVariantService.java
public class ProductVariantService extends BaseService<ProductVariant> {

    private final ProductVariantRepository productVariantRepository;

    public ProductVariantService(ProductVariantRepository productVariantRepository) {
        super(productVariantRepository);
        this.productVariantRepository = productVariantRepository;
    }

    // All variants for a product — used by PDP and servlet default color logic
    public List<ProductVariant> getByProductId(int productId) {
        return productVariantRepository.findByProductId(productId);
    }

    // Distinct color strings — used by upload validation
    public List<String> getDistinctColors(int productId) {
        return productVariantRepository.findDistinctColorsByProductId(productId);
    }

    // Sizes available for a specific color — drives size selector
    // when user switches color in QV or PDP
    public List<String> getSizesForColor(int productId, String color) {
        return productVariantRepository.findSizesByProductIdAndColor(productId, color);
    }

    // Removes all size rows for a color — called alongside deleteColorImages
    // when admin removes a color variant entirely
    public void deleteByColor(int productId, String color) {
        productVariantRepository.deleteByProductIdAndColor(productId, color);
    }
}