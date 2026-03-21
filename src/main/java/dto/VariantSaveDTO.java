package dto;

public record VariantSaveDTO(
        Integer variantId,
        String color,
        String size,
        int quantity
) {}