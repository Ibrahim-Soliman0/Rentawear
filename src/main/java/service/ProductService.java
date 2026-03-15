package service;

import dto.PriceRangeDTO;
import dto.ProductDTO;
import entity.Product;
import repository.ProductRepository;
import repository.impl.ProductRepositoryImpl;

import java.util.List;
import java.util.stream.Collectors;

public class ProductService extends BaseService<Product> {

    private final ProductRepository productRepository;

    public ProductService() {
        this(new ProductRepositoryImpl());
    }

    public ProductService(ProductRepository productRepository) {
        super(productRepository);
        this.productRepository = productRepository;
    }

    public List<ProductDTO> getNew(int limit, int days) {
        return toProductDTOs(productRepository.findNew(limit, days));
    }

    public List<ProductDTO> getFiltered(String gender,
                                        List<Integer> categoryIds,
                                        Double minPrice,
                                        Double maxPrice,
                                        int limit,
                                        int offset) {
        return toProductDTOs(
                productRepository.findFiltered(
                        gender, categoryIds, minPrice, maxPrice, limit, offset));
    }

    public long countFiltered(String gender,
                              List<Integer> categoryIds,
                              Double minPrice,
                              Double maxPrice) {
        return productRepository.countFiltered(gender, categoryIds, minPrice, maxPrice);
    }

    public List<ProductDTO> search(String q, String gender, int limit) {
        return toProductDTOs(productRepository.search(q, gender, limit));
    }

    public List<ProductDTO> searchPaged(String q, String gender, int limit, int offset) {
        return toProductDTOs(productRepository.searchPaged(q, gender, limit, offset));
    }

    public long countSearch(String q, String gender) {
        return productRepository.countSearch(q, gender);
    }

    public List<ProductDTO> getByInterests(List<Integer> categoryIds,String gender, int limit) {
        return toProductDTOs(productRepository.findByInterests(categoryIds,gender, limit));
    }

    public PriceRangeDTO getPriceRange(String gender, List<Integer> categoryIds) {
        Object[] row = productRepository.getMinMaxPrice(gender, categoryIds);
        if (row == null) return new PriceRangeDTO(null, null);
        Double min = row[0] == null ? null : ((java.math.BigDecimal) row[0]).doubleValue();
        Double max = row[1] == null ? null : ((java.math.BigDecimal) row[1]).doubleValue();
        return new PriceRangeDTO(min, max);
    }

    private List<ProductDTO> toProductDTOs(List<Product> products) {
        return products.stream()
                .map(ProductDTO::from)
                .collect(Collectors.toList());
    }
}