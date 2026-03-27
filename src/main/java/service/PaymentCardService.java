package service;

import dto.PaymentCardDTO;
import entity.PaymentCard;
import entity.User;
import entity.enums.CardType;
import mapper.PaymentCardMapper;
import org.mapstruct.factory.Mappers;
import repository.PaymentCardRepository;
import repository.impl.PaymentCardRepositoryImpl;

public class PaymentCardService extends BaseService<PaymentCard> {

    private final PaymentCardRepository paymentCardRepository;
    private final UserService userService = new UserService();
    private final PaymentCardMapper mapper = Mappers.getMapper(PaymentCardMapper.class);

    public PaymentCardService() {
        this(new PaymentCardRepositoryImpl());
    }

    public PaymentCardService(PaymentCardRepository paymentCardRepository) {
        super(paymentCardRepository);
        this.paymentCardRepository = paymentCardRepository;
    }

    /**
     * Validates, builds and persists a new payment card for the given user.
     * Returns the saved card as a DTO.
     * Throws IllegalArgumentException with a user-facing message on validation failure.
     */
    public PaymentCardDTO addCard(Integer userId,
                                  String cardNumber,
                                  String cardholderName,
                                  String expiryMonth,
                                  String expiryYear,
                                  String cvv,
                                  String cardTypeRaw) {

        /* ── Validation ── */
        if (isBlank(cardNumber)) throw new IllegalArgumentException("Card number is required.");
        if (isBlank(cardholderName)) throw new IllegalArgumentException("Cardholder name is required.");
        if (isBlank(expiryMonth)) throw new IllegalArgumentException("Expiry month is required.");
        if (isBlank(expiryYear)) throw new IllegalArgumentException("Expiry year is required.");
        if (isBlank(cvv)) throw new IllegalArgumentException("CVV is required.");

        cardNumber = cardNumber.replaceAll("\\s+", "");

        if (!cardNumber.matches("\\d{13,19}"))
            throw new IllegalArgumentException("Invalid card number.");
        if (!expiryMonth.matches("0[1-9]|1[0-2]"))
            throw new IllegalArgumentException("Invalid expiry month.");
        if (!expiryYear.matches("\\d{2,4}"))
            throw new IllegalArgumentException("Invalid expiry year.");

        /* ── Resolve card type ── */
        CardType cardType;
        try {
            cardType = CardType.valueOf(cardTypeRaw.toUpperCase());
        } catch (Exception e) {
            cardType = detectCardType(cardNumber);
        }

        /* ── Load user ── */
        User user = userService.getById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        /* ── Build & persist ── */
        PaymentCard card = new PaymentCard();
        card.setUser(user);
        card.setCardNumber(cardNumber);
        card.setCardholderName(cardholderName.trim());
        card.setCardType(cardType);
        card.setExpiryMonth(expiryMonth);
        card.setExpiryYear(expiryYear);
        card.setCvv(cvv);

        return mapper.toDto(repository.save(card));
    }

    /**
     * Verifies ownership and deletes the card.
     * Throws IllegalArgumentException if not found or not owned by the user.
     */
    public void removeCard(Integer cardId, Integer userId) {
        PaymentCard card = getById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found."));

        if (!card.getUser().getId().equals(userId))
            throw new IllegalArgumentException("You do not own this card.");

        delete(card);
    }

    /* ── Helpers ── */

    private CardType detectCardType(String number) {
        if (number.startsWith("4")) return CardType.VISA;
        if (number.matches("^5[1-5].*") || number.matches("^2[2-7].*")) return CardType.MASTERCARD;
        if (number.matches("^3[47].*")) return CardType.AMERICAN_EXPRESS;
        return CardType.OTHER;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}