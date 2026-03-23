package dto;

public record CartItemDTO(
        ProductCoreDTO core,     // name, brand, price, image all here
        int cartItemId,
        int variantId,
        String color,
        String size,
        String startDate,
        String endDate,
        int rentalDays,
        double totalPrice,
        int inventoryQty,
        int qty
) {}