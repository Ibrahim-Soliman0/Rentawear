package repository;

import dto.PriceRangeDTO;
import entity.Product;

import java.util.List;

public interface ProductRepository extends Repository<Product> {

    // Listing / browse
    List<Product> findNew(int limit, int offset, int days, String gender, List<Integer> categoryIds, Double minPrice, Double maxPrice);
    long countNew(int days, String gender, List<Integer> categoryIds, Double minPrice, Double maxPrice);
    List<Product> findFiltered(String gender, List<Integer> categoryIds, Double minPrice, Double maxPrice, int limit, int offset);

    List<Product> findByInterests(List<Integer> categoryIds, String gender, int limit, int offset);

    List<Product> searchFiltered(String q, String gender, List<Integer> categoryIds, Double minPrice, Double maxPrice, int limit, int offset);

    long countFiltered(String gender, List<Integer> categoryIds, Double minPrice, Double maxPrice);

    long countSearchFiltered(String q, String gender, List<Integer> categoryIds,Double minPrice, Double maxPrice);

    long countByInterests(List<Integer> categoryIds, String gender);

    // Aggregates
    PriceRangeDTO getMinMaxPrice(String gender, List<Integer> categoryIds);
}