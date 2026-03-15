package dto;

import java.time.LocalDate;
import java.util.List;

public record UserProfileDTO(
        Integer id,
        String name,
        String email,
        String job,
        LocalDate birthday,
        List<PaymentCardDTO> paymentCards
) {
}
