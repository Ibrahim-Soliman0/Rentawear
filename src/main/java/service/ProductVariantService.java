package service;

import model.ProductVariant;
import repository.ProductVariantRepository;
import repository.impl.ProductVariantRepositoryImpl;

public class ProductVariantService extends BaseService<ProductVariant> {

    private final ProductVariantRepository productVariantRepository;

    public ProductVariantService() {
        this(new ProductVariantRepositoryImpl());
    }

    public ProductVariantService(ProductVariantRepository productVariantRepository) {
        super(productVariantRepository);
        this.productVariantRepository = productVariantRepository;
    }
}