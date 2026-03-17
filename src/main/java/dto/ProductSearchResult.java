package dto;

import java.util.List;

public record ProductSearchResult(
        List<ProductSearchDTO> products,
        long                   total,
        int                    page,
        int                    pageSize
) {}