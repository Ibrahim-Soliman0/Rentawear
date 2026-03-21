package dto;

public record AdminProductRowDTO(
        ProductCoreDTO core,
        boolean inStock,
        int totalStock  // ← add this

) {}
