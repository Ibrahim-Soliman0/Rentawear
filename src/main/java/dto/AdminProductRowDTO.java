package dto;

public record AdminProductRowDTO(
        ProductCoreDTO core,
        boolean inStock
) {}
