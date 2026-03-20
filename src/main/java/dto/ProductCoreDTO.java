package dto;

// The shared core, assembled once in the service layer
public record ProductCoreDTO(
        int id,
        String name,
        String brand,
        double pricePerDay,
        String imageUrl,
        String gender,
        Integer categoryId,
        String categoryName
) {}