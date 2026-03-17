package dto;

import java.time.LocalDate;

public record CartItemDTO(
        ProductCoreDTO core,     // name, brand, price, image all here
        int cartItemId,
        int variantId,
        String color,
        String size,
        LocalDate startDate,
        LocalDate endDate,
        int rentalDays,
        double totalPrice
) {}