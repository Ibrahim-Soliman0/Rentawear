package repository.impl;

import model.ProductVariant;
import repository.ProductVariantRepository;

public class ProductVariantRepositoryImpl extends BaseRepositoryImpl<ProductVariant> implements ProductVariantRepository {

    public ProductVariantRepositoryImpl() {
        super(ProductVariant.class);
    }
}

