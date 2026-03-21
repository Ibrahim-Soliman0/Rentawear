package dto;

import java.util.List;

public record SaveProductDTO(
        Integer id,
        String name,
        Integer categoryId,
        double pricePerDay,
        String imageUrl,
        String description,
        List<VariantSaveDTO> variants
) {}