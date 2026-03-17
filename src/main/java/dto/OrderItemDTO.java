package dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderItemDTO(
        Integer id,
        String productName,
        String color,
        String size,
        Integer quantity,
        BigDecimal priceAtPurchase,
        LocalDate startDate,
        LocalDate endDate
) {}