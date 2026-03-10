package service;

import model.Product;
import repository.ProductRepository;
import repository.impl.ProductRepositoryImpl;

public class ProductService extends BaseService<Product> {

    private final ProductRepository productRepository;

    public ProductService() {
        this(new ProductRepositoryImpl());
    }

    public ProductService(ProductRepository productRepository) {
        super(productRepository);
        this.productRepository = productRepository;
    }
}