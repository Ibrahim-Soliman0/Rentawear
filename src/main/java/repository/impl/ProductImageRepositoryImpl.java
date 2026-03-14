package repository.impl;

import entity.ProductImage;
import repository.ProductImageRepository;

public class ProductImageRepositoryImpl extends BaseRepositoryImpl<ProductImage> implements ProductImageRepository {

    public ProductImageRepositoryImpl() {
        super(ProductImage.class);
    }
}