package service;

import entity.ProductVariant;
import repository.ProductVariantRepository;
import repository.impl.ProductVariantRepositoryImpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Owns variant-level operations only.
// no product or image context needed.
public class ProductVariantService extends BaseService<ProductVariant> {

    private final ProductVariantRepository variantRepo;

    public ProductVariantService() {
        this(new ProductVariantRepositoryImpl());
    }

    public ProductVariantService(ProductVariantRepository variantRepo) {
        super(variantRepo);
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

    /** Returns active (non-deleted) variants for multiple products grouped by product id. */
    public Map<Integer, List<ProductVariant>> getByProductIds(List<Integer> productIds) {
        List<ProductVariant> all = variantRepo.findByProductIds(productIds);
        Map<Integer, List<ProductVariant>> grouped = new LinkedHashMap<>();
        for (ProductVariant v : all) {
            grouped.computeIfAbsent(v.getProduct().getId(), k -> new ArrayList<>())
                    .add(v);
        }
        return grouped;
    }

    /**
     * Returns the IDs of active variants for a given product+color.
     * Used to snapshot which cart_items to purge before soft-deleting.
     */
    public List<Integer> findIdsByColor(int productId, String color) {
        return variantRepo.findIdsByProductIdAndColor(productId, color);
    }

    // ── Soft-delete (safe when order_items may reference these rows) ──────────

    /** Soft-deletes all variants for the given product+color. */
    public void softDeleteColor(int productId, String color) {
        variantRepo.softDeleteByProductIdAndColor(productId, color);
    }

    /** Soft-deletes all variants for the given product. */
    public void softDeleteByProductId(int productId) {
        variantRepo.softDeleteByProductId(productId);
    }

    // ── Legacy hard-delete (no longer called by facade) ───────────────────────

    public void deleteColor(int productId, String color) {
        variantRepo.deleteByProductIdAndColor(productId, color);
    }

    public void deleteByProductId(int productId) {
        variantRepo.deleteByProductId(productId);
    }
}