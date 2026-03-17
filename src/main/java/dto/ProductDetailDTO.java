package dto;

import java.util.List;
import java.util.Map;

public record ProductDetailDTO(
        ProductCoreDTO               core,
        String                       description,
        List<ColorSwatchDTO>         swatches,
        Map<String, List<String>>    sizesByColor,
        Map<String, List<String>>    availableSizesByColor,
        Map<String, List<String>>    imagesByColor
) {}
