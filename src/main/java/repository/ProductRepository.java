package repository;

import model.Product;
import java.util.List;

public interface ProductRepository extends Repository<Product> {

    List<Product> findNew(int limit, int days);
    List<Product> search(String q, String gender, int limit);
    List<Product> searchPaged(String q, String gender, int limit, int offset);
    long countSearch(String q, String gender);
    List<Product> findFiltered(String gender, List<Integer> categoryIds, Double minPrice,
                                      Double maxPrice, int limit, int offset);
    long countFiltered(String gender, List<Integer> categoryIds, Double minPrice,
                              Double maxPrice);
    List<Product> findByInterests(List<Integer> categoryIds,
                                  String gender,
                                  int limit);
    Object[] getMinMaxPrice(String gender, List<Integer> categoryIds);
}
