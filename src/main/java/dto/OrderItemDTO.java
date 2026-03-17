package dto;

import java.math.BigDecimal;

public record OrderItemDTO(
        Integer id,
        String productName,
        String imageUrl,
        String size,
        String color,
        Integer quantity,
        BigDecimal priceAtPurchase,
        String startDate,
        String endDate
) {}
