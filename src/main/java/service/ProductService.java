package service;

import dto.PriceRangeDTO;
import dto.ProductFilterDTO;
import entity.Product;
import repository.ProductRepository;

import java.util.List;

// Owns product-level queries only.
// Does not know about variants, images, or DTOs — those belong to the facade.
public class ProductService {

    private static final int NEW_DAYS = 30;

    private final ProductRepository productRepo;

    public ProductService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public Product getById(int productId) {
        return productRepo.findById(productId);
    }

    public List<Product> findNew(ProductFilterDTO f) {
        return productRepo.findNew(
                f.pageSize(), f.offset(), NEW_DAYS,
                f.gender(), f.categoryIds(),
                f.minPrice(), f.maxPrice());
    }

    public List<Product> findFiltered(ProductFilterDTO f) {
        return productRepo.findFiltered(
                f.gender(), f.categoryIds(),
                f.minPrice(), f.maxPrice(),
                f.pageSize(), f.offset());
    }

    public List<Product> findByInterests(ProductFilterDTO f) {
        return productRepo.findFiltered(
                null,              // gender — null so cross-gender interests work
                f.interestIds(),   // use interestIds as the category list
                f.minPrice(),
                f.maxPrice(),
                f.pageSize(),
                f.offset());
    }

    public List<Product> searchFiltered(ProductFilterDTO f) {
        return productRepo.searchFiltered(
                f.searchQuery(), f.gender(),
                f.categoryIds(),
                f.minPrice(), f.maxPrice(),
                f.pageSize(), f.offset());
    }

    public long countByInterests(ProductFilterDTO f) {
        return productRepo.countFiltered(
                null,
                f.interestIds(),
                f.minPrice(),
                f.maxPrice());
    }

    public long countNew(ProductFilterDTO f) {
        return productRepo.countNew(NEW_DAYS, f.gender(), f.categoryIds(),
                f.minPrice(), f.maxPrice());
    }

    public long countFiltered(ProductFilterDTO f) {
        return productRepo.countFiltered(
                f.gender(), f.categoryIds(),
                f.minPrice(), f.maxPrice());
    }

    public long countSearchFiltered(ProductFilterDTO f) {
        return productRepo.countSearchFiltered(
                f.searchQuery(), f.gender(),
                f.categoryIds(),
                f.minPrice(), f.maxPrice());
    }

    public PriceRangeDTO getMinMaxPriceForInterests(ProductFilterDTO f) {
        return productRepo.getMinMaxPrice(null, f.interestIds());
    }

    public PriceRangeDTO getMinMaxPrice(ProductFilterDTO f) {
        return productRepo.getMinMaxPrice(f.gender(), f.categoryIds());
    }

    public void delete(int productId) {
        Product product = productRepo.findById(productId);
        if (product != null) {
            productRepo.delete(product);
        }
    }

    public Product save(Product product) { return productRepo.save(product); }
}