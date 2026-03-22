package repository.impl;

import dto.PriceRangeDTO;
import entity.Product;
import entity.enums.Gender;
import jakarta.persistence.TypedQuery;
import repository.ProductRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;

public class ProductRepositoryImpl extends BaseRepositoryImpl<Product>
        implements ProductRepository {

    public ProductRepositoryImpl() {
        super(Product.class);
    }

    @Override
    public List<Product> findNew(int limit, int days,
                                 String gender, List<Integer> categoryIds) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        if (categoryIds != null && !categoryIds.isEmpty()) {
            return pagedFetch("Product.findNewByCategoriesIds",
                    q -> q.setParameter("cutoff", cutoff)
                            .setParameter("ids",    categoryIds)
                            .setParameter("gender", toGender(gender)),
                    limit, 0);
        }
        return pagedFetch("Product.findNewFilteredIds",
                q -> q.setParameter("cutoff", cutoff)
                        .setParameter("gender", toGender(gender)),
                limit, 0);
    }

    @Override
    public List<Product> findFiltered(String gender, List<Integer> categoryIds,
                                      Double minPrice, Double maxPrice,
                                      int limit, int offset) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            return pagedFetch("Product.findByCategoriesIds",
                    q -> q.setParameter("ids",      categoryIds)
                            .setParameter("gender",   toGender(gender))
                            .setParameter("minPrice", toBigDecimal(minPrice))
                            .setParameter("maxPrice", toBigDecimal(maxPrice)),
                    limit, offset);
        }
        return pagedFetch("Product.findFilteredIds",
                q -> q.setParameter("gender",   toGender(gender))
                        .setParameter("minPrice", toBigDecimal(minPrice))
                        .setParameter("maxPrice", toBigDecimal(maxPrice)),
                limit, offset);
    }

    @Override
    public List<Product> findByInterests(List<Integer> categoryIds,
                                         String gender, int limit, int offset) {
        if (categoryIds == null || categoryIds.isEmpty()) return List.of();
        return pagedFetch("Product.findByInterestsIds",
                q -> q.setParameter("ids",    categoryIds)
                        .setParameter("gender", toGender(gender)),
                limit, offset);
    }

    @Override
    public List<Product> searchFiltered(String q, String gender,
                                        List<Integer> categoryIds,
                                        Double minPrice, Double maxPrice,
                                        int limit, int offset) {
        String pattern = "%" + q.toLowerCase() + "%";
        if (categoryIds != null && !categoryIds.isEmpty()) {
            return pagedFetch("Product.searchByCategoriesIds",
                    query -> query
                            .setParameter("q",        pattern)
                            .setParameter("ids",      categoryIds)
                            .setParameter("gender",   toGender(gender))
                            .setParameter("minPrice", toBigDecimal(minPrice))
                            .setParameter("maxPrice", toBigDecimal(maxPrice)),
                    limit, offset);
        }
        return pagedFetch("Product.searchFilteredIds",
                query -> query
                        .setParameter("q",        pattern)
                        .setParameter("gender",   toGender(gender))
                        .setParameter("minPrice", toBigDecimal(minPrice))
                        .setParameter("maxPrice", toBigDecimal(maxPrice)),
                limit, offset);
    }

    @Override
    public long countNew(int days, String gender, List<Integer> categoryIds) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        if (categoryIds != null && !categoryIds.isEmpty()) {
            return em().createNamedQuery("Product.countNewByCategories", Long.class)
                    .setParameter("cutoff", cutoff)
                    .setParameter("ids",    categoryIds)
                    .setParameter("gender", toGender(gender))
                    .getSingleResult();
        }
        return em().createNamedQuery("Product.countNewFiltered", Long.class)
                .setParameter("cutoff", cutoff)
                .setParameter("gender", toGender(gender))
                .getSingleResult();
    }

    @Override
    public long countFiltered(String gender, List<Integer> categoryIds,
                              Double minPrice, Double maxPrice) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            return em().createNamedQuery("Product.countByCategories", Long.class)
                    .setParameter("ids",      categoryIds)
                    .setParameter("gender",   toGender(gender))
                    .setParameter("minPrice", toBigDecimal(minPrice))
                    .setParameter("maxPrice", toBigDecimal(maxPrice))
                    .getSingleResult();
        }
        return em().createNamedQuery("Product.countFiltered", Long.class)
                .setParameter("gender",   toGender(gender))
                .setParameter("minPrice", toBigDecimal(minPrice))
                .setParameter("maxPrice", toBigDecimal(maxPrice))
                .getSingleResult();
    }

    @Override
    public long countSearchFiltered(String q, String gender,
                                    List<Integer> categoryIds,
                                    Double minPrice, Double maxPrice) {
        String pattern = "%" + q.toLowerCase() + "%";
        if (categoryIds != null && !categoryIds.isEmpty()) {
            return em().createNamedQuery("Product.countSearchFilteredByCategories", Long.class)
                    .setParameter("q",        pattern)
                    .setParameter("ids",      categoryIds)
                    .setParameter("gender",   toGender(gender))
                    .setParameter("minPrice", toBigDecimal(minPrice))
                    .setParameter("maxPrice", toBigDecimal(maxPrice))
                    .getSingleResult();
        }
        return em().createNamedQuery("Product.countSearchFiltered", Long.class)
                .setParameter("q",        pattern)
                .setParameter("gender",   toGender(gender))
                .setParameter("minPrice", toBigDecimal(minPrice))
                .setParameter("maxPrice", toBigDecimal(maxPrice))
                .getSingleResult();
    }

    @Override
    public long countByInterests(List<Integer> categoryIds, String gender) {
        if (categoryIds == null || categoryIds.isEmpty()) return 0;
        return em().createNamedQuery("Product.countByInterests", Long.class)
                .setParameter("ids",    categoryIds)
                .setParameter("gender", toGender(gender))
                .getSingleResult();
    }

    @Override
    public PriceRangeDTO getMinMaxPrice(String gender, List<Integer> categoryIds) {
        Object[] row;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            row = (Object[]) em().createNamedQuery("Product.getMinMaxPriceByCategories")
                    .setParameter("ids",    categoryIds)
                    .setParameter("gender", toGender(gender))
                    .getSingleResult();
        } else {
            row = (Object[]) em().createNamedQuery("Product.getMinMaxPrice")
                    .setParameter("gender", toGender(gender))
                    .getSingleResult();
        }

        if (row == null || (row[0] == null && row[1] == null)) {
            return new PriceRangeDTO(null, null);
        }

        // Defensive cast, aggregate result type is dialect-dependent.
        // Handles both BigDecimal (most dialects) and Double (some drivers).
        Double min = toDouble(row[0]);
        Double max = toDouble(row[1]);
        return new PriceRangeDTO(min, max);
    }

    //Two-step pagination
    //
    // Step 1 ->  ID query: lightweight index scan, LIMIT/OFFSET applied in SQL.
    // Step 2 -> entity fetch: JOIN FETCH on the page-sized ID list, no LIMIT.
    //

    private List<Product> pagedFetch(String idQueryName,
                                     Consumer<TypedQuery<Integer>> setup,
                                     int limit, int offset) {
        TypedQuery<Integer> idQuery = em().createNamedQuery(idQueryName, Integer.class);
        setup.accept(idQuery);

        List<Integer> ids = idQuery
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        if (ids.isEmpty()) return List.of();

        return em().createNamedQuery("Product.findByIds", Product.class)
                .setParameter("ids", ids)
                .getResultList();
    }


    private Gender toGender(String gender) {
        if (gender == null || gender.isBlank()) return null;
        try {
            return Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private Double toDouble(Object value) {
        if (value == null)              return null;
        if (value instanceof BigDecimal bd) return bd.doubleValue();
        if (value instanceof Double d)      return d;
        if (value instanceof Number n)      return n.doubleValue();
        return null;
    }
}