package service;

import dto.ProductDTO;
import model.Product;
import repository.ProductRepository;
import repository.impl.ProductRepositoryImpl;

import java.util.List;
import java.util.stream.Collectors;

public class ProductService extends BaseService<Product> {

    private final ProductRepository productRepository;

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

    private List<ProductDTO> toProductDTOs(List<Product> products) {
        return products.stream()
                .map(ProductDTO::from)
                .collect(Collectors.toList());
    }
}