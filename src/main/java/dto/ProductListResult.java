package dto;

import java.util.List;

public record ProductListResult(
        List<ProductCardDTO> products,
        long                 total,
        PriceRangeDTO        priceRange,
        int                  page,
        int                  pageSize
) {}
