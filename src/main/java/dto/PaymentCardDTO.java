package dto;

public record PaymentCardDTO(
        Integer id,
        String cardNumber,
        String cardType
) {
}
