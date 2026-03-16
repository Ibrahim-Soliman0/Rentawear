package dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductListDTO(
        int id,
        String name,
        String brand,
        BigDecimal pricePerDay,
        String imageBase,
        boolean isNew,
        List<SwatchDTO> swatches
) {}
