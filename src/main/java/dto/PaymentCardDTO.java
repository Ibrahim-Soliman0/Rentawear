package dto;

import entity.enums.CardType;

public record PaymentCardDTO(
        Integer id,
        String cardNumber,
        CardType cardType,
        String expiryMonth,
        String expiryYear
) {
}
