package service;

import entity.ProductImage;
import repository.ProductImageRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductImageService {

    private final ProductImageRepository imageRepo;

    public ProductImageService(ProductImageRepository imageRepo) {
        this.imageRepo = imageRepo;
    }

    public List<ProductImage> getByProductId(int productId) {
        return imageRepo.findByProductId(productId);
    }

    public List<ProductImage> getPrimaryPerColor(int productId) {
        return imageRepo.findPrimaryPerColor(productId);
    }

    public long countByProductIdAndColor(int productId, String color) {
        return imageRepo.countByProductIdAndColor(productId, color);
    }

    public Map<Integer, List<ProductImage>> getPrimaryPerColorForProducts(
            List<Integer> productIds) {

        List<ProductImage> all = imageRepo.findPrimaryPerColorForProducts(productIds);
        Map<Integer, List<ProductImage>> grouped = new LinkedHashMap<>();
        for (ProductImage img : all) {
            grouped.computeIfAbsent(img.getProduct().getId(), k -> new ArrayList<>())
                    .add(img);
        }
        return grouped;
    }

    // Deletes all images for a colour.
    public void deleteColorImages(int productId, String color) {
        imageRepo.deleteByProductIdAndColor(productId, color);
    }
}