package repository.impl;

import model.ProductImage;
import repository.ProductImageRepository;

public class ProductImageRepositoryImpl extends BaseRepositoryImpl<ProductImage> implements ProductImageRepository {

    public ProductImageRepositoryImpl() {
        super(ProductImage.class);
    }
}