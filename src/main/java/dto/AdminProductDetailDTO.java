package dto;

import java.util.List;
import java.util.Map;

public record AdminProductDetailDTO(
        ProductCoreDTO core,
        String description,
        boolean isNew,
        Map<String, List<VariantStockDTO>> stockByColor
) {}
