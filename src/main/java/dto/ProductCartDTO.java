package dto;

import java.math.BigDecimal;

public record ProductCartDTO(
        int id,
        String name,
        String brand,
        BigDecimal pricePerDay,
        String imageBase,
        String dates, // "YYYY-MM-DD/YYYY-MM-DD" or JSON object string
        int qty
) {}
