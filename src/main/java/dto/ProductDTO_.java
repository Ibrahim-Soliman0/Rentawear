package dto;

import java.util.List;
import java.util.Map;

@Deprecated
public record ProductDTO_(
        int id,
        String name,
        String brand,
        String description,
        double basePrice,
        int categoryId,
        boolean isNew,
        String imageUrl,                              // default image
        List<ColorSwatchDTO> swatches,                // reuses existing DTO
        Map<String, List<String>> sizesByColor,       // all sizes per color
        Map<String, List<String>> availableSizesByColor, // in-stock only
        Map<String, List<String>> imagesByColor       // image URLs per color
) {}