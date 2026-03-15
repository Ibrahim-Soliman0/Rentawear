package service;

import dto.ProductImagesDTO;
import entity.Product;
import entity.ProductVariant;
import entity.ProductImage;
import repository.ProductImageRepository;
import repository.ProductRepository;
import repository.ProductVariantRepository;
import util.ImageProcessor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ProductImageService extends BaseService<ProductImage> {

    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    public ProductImageService(ProductImageRepository productImageRepository,
                               ProductVariantRepository productVariantRepository,
                               ProductRepository productRepository) {
        super(productImageRepository);
        this.productImageRepository  = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.productRepository       = productRepository;
    }

    public LinkedHashMap<String, List<ProductImage>> getGroupedByColor(int productId) {
        List<ProductImage> all = productImageRepository.findByProductId(productId);
        LinkedHashMap<String, List<ProductImage>> grouped = new LinkedHashMap<>();
        for (ProductImage img : all) {
            grouped.computeIfAbsent(img.getColor(), k -> new ArrayList<>()).add(img);
        }
        return grouped;
    }

    public void updateProductPrimaryIfNeeded(Product product,
                                             String uploadedColor,
                                             String newBasePath) {
        long imageCount = productImageRepository
                .countByProductIdAndColor(product.getId(), uploadedColor);

        // Not the first image for this color, nothing to update
        if (imageCount != 1) return;

        List<ProductVariant> variants =
                productVariantRepository.findByProductId(product.getId());
        if (variants.isEmpty()) return;

        String defaultColor = variants.get(0).getColor();
        if (uploadedColor != null && uploadedColor.equals(defaultColor)) {
            product.setImageUrl(newBasePath);
            productRepository.save(product);
        }
    }

    // Called when admin removes a color variant entirely.
    // Deletes filesystem files first, then bulk-deletes DB records in one query.
    public void deleteColorImages(int productId, String color, String webappRoot) {
        List<ProductImage> images =
                productImageRepository.findByProductIdAndColor(productId, color);

        for (ProductImage img : images) {
            ImageProcessor.deleteAll(img.getImageUrl(), webappRoot);
        }

        productImageRepository.deleteByProductIdAndColor(productId, color);
    }


    public ProductImagesDTO getImagesDTOForProduct(int productId) {
        Product product = productRepository.findById(productId);
        if (product == null) return null;

        List<ProductVariant> variants =
                productVariantRepository.findByProductId(productId);

        String defaultColor = variants.isEmpty()
                ? null
                : variants.get(0).getColor();

        LinkedHashMap<String, List<ProductImage>> grouped =
                getGroupedByColor(productId);

        return ProductImagesDTO.from(
                productId,
                product.getName(),
                defaultColor,
                grouped);
    }
}