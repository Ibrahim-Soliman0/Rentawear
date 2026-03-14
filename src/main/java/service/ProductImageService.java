package service;

import entity.ProductImage;
import repository.ProductImageRepository;
import repository.impl.ProductImageRepositoryImpl;

public class ProductImageService extends BaseService<ProductImage> {

    private final ProductImageRepository productImageRepository;

    public ProductImageService() {
        this(new ProductImageRepositoryImpl());
    }

    public ProductImageService(ProductImageRepository productImageRepository) {
        super(productImageRepository);
        this.productImageRepository = productImageRepository;
    }
}