package dto;

public record SimpleCartItemDTO(
        Integer variantId,
        Integer qty,
        String startDate,
        String endDate
) {
}
