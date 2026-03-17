package dto;

import java.util.List;

public record AdminProductListResult(
        List<AdminProductRowDTO> products,
        long                     total,
        int                      page,
        int                      pageSize
) {}