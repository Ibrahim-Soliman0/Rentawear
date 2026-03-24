package service;

import entity.Product;
import entity.ProductImage;
import repository.ProductImageRepository;
import util.ImagePathUtil;
import util.ImageProcessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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

    public void deleteByProductId(int productId) {
        imageRepo.deleteByProductId(productId);
    }

    public String saveColorImage(Product product, String encodedColor,
                                 InputStream inputStream, String webappRoot)
            throws IOException {

        String uuid    = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String basePath = ImagePathUtil.base(product.getId(), encodedColor, uuid);
        String absDir   = ImagePathUtil.absoluteDir(webappRoot, product.getId(), encodedColor);

        // Process and write image files
        ImageProcessor.process(inputStream, new File(absDir), uuid);

        // Delete old image for this color if exists
        imageRepo.findByProductIdAndColor(product.getId(), encodedColor)
                .stream()
                .findFirst()
                .ifPresent(existing -> {
                    ImageProcessor.deleteAll(existing.getImageUrl(), webappRoot);
                    imageRepo.delete(existing);
                });

        // Save new ProductImage entity
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setColor(encodedColor);
        productImage.setImageUrl(basePath);
        imageRepo.save(productImage);

        return basePath;
    }


    public List<ProductImage> findByProductIdAndColor(int productId, String color) {
        return imageRepo.findByProductIdAndColor(productId, color);
    }
}