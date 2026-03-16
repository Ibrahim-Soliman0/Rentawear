package dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailDTO(
        int id,
        String name,
        String brand,
        BigDecimal pricePerDay,
        String imageBase,
        boolean isNew,
        String description,
        List<String> sizes,
        List<SwatchDTO> swatches,
        List<String> images
) {}
