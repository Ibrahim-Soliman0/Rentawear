package dto;

public record VariantStockDTO(
        int variantId,
        String size,
        int quantity,
        String imageUrl
) {}