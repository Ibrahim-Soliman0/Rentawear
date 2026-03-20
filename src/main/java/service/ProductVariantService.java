package service;

import entity.ProductVariant;
import repository.ProductVariantRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Owns variant-level operations only.
// Grouping helpers live here because they operate purely on variant data
// no product or image context needed.
public class ProductVariantService {

    private final ProductVariantRepository variantRepo;

    public ProductVariantService(ProductVariantRepository variantRepo) {
        this.variantRepo = variantRepo;
    }


    public List<ProductVariant> getByProductId(int productId) {
        return variantRepo.findByProductId(productId);
    }

    public List<String> getDistinctColors(int productId) {
        return variantRepo.findDistinctColorsByProductId(productId);
    }

    public List<String> getSizesByColor(int productId, String color) {
        return variantRepo.findSizesByProductIdAndColor(productId, color);
    }

    public List<String> getAvailableSizesByColor(int productId, String color) {
        return variantRepo.findAvailableSizesByProductIdAndColor(productId, color);
    }


    // Returns variants for multiple products grouped by product id.
    public Map<Integer, List<ProductVariant>> getByProductIds(List<Integer> productIds) {
        List<ProductVariant> all = variantRepo.findByProductIds(productIds);
        Map<Integer, List<ProductVariant>> grouped = new LinkedHashMap<>();
        for (ProductVariant v : all) {
            grouped.computeIfAbsent(v.getProduct().getId(), k -> new ArrayList<>())
                    .add(v);
        }
        return grouped;
    }

    public void deleteColor(int productId, String color) {
        variantRepo.deleteByProductIdAndColor(productId, color);
    }

    public void deleteByProductId(int productId) {
        variantRepo.deleteByProductId(productId);
    }
}