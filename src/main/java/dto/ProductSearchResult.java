package dto;

import java.util.List;

public record ProductSearchResult(
        List<ProductSearchDTO> products,
        long                   total,
        PriceRangeDTO          priceRange,
        int                    page,
        int                    pageSize
) {}