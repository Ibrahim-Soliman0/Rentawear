package dto;

import java.util.List;
import java.util.Map;

public record ProductCardDTO(
        ProductCoreDTO core,
        boolean isNew,
        List<ColorSwatchDTO> swatches,
        Map<String, String> primaryImageByColor
) {}