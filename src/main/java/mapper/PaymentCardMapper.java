package mapper;

import dto.PaymentCardDTO;
import entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PaymentCardMapper {

    @Mapping(target = "cardNumber", qualifiedByName = "mask")
    PaymentCardDTO toDto(PaymentCard card);

    @org.mapstruct.Named("mask")
    default String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "*".repeat(cardNumber.length() - 4) + last4;
    }
}