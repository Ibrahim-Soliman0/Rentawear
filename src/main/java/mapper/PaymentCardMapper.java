package mapper;

import dto.PaymentCardDTO;
import entity.PaymentCard;
import org.mapstruct.Mapper;

@Mapper()
public interface PaymentCardMapper {

    PaymentCardDTO toDto(PaymentCard card);

    PaymentCard toEntity(PaymentCardDTO dto);
}