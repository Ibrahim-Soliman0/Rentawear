package service;

import dto.PaymentCardDTO;
import entity.PaymentCard;
import entity.User;
import entity.enums.CardType;
import mapper.PaymentCardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.PaymentCardRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * ─────────────────────────────────────────────────────────────────────────────
 *  PaymentCardService Test Suite
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  PaymentCardService depends on three collaborators:
 *    1. PaymentCardRepository (for persisting and retrieving payment cards)
 *    2. UserService (for loading and validating users)
 *    3. PaymentCardMapper (MapStruct mapper for DTO conversion)
 *
 *  We build PaymentCardService manually in @BeforeEach, passing all mocks.
 *  This gives us full control over all dependencies.
 *
 *  Test organization:
 *    - @Nested classes group tests by method
 *    - @BeforeEach provides fresh mocks and test data
 *    - ArgumentCaptor captures arguments passed to mocks for verification
 *    - Comprehensive validation testing for card data
 *    - Security testing for ownership verification
 * ─────────────────────────────────────────────────────────────────────────────
 */

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    // ── Mocks (fake objects we control) ──────────────────────────────────────

    @Mock private PaymentCardRepository paymentCardRepository;
    @Mock private UserService userService;
    @Mock private PaymentCardMapper paymentCardMapper;

    // ── System under test (built manually so we control all dependencies) ─────

    private PaymentCardService paymentCardService;

    // ── Shared test data ──────────────────────────────────────────────────────

    private User testUser;
    private PaymentCard testCard;
    private PaymentCardDTO testCardDTO;

    @BeforeEach
    void setUp() {
        paymentCardService = new PaymentCardService(
                paymentCardRepository,
                userService,
                paymentCardMapper
        );

        // ── Build a test user ────────────────────────────────────────────────

        testUser = new User();
        testUser.setId(1);
        testUser.setName("Jane Smith");
        testUser.setEmail("jane@example.com");

        // ── Build a test payment card ────────────────────────────────────────

        testCard = new PaymentCard();
        testCard.setId(10);
        testCard.setCardNumber("4111111111111111");
        testCard.setCardholderName("Jane Smith");
        testCard.setCardType(CardType.VISA);
        testCard.setExpiryMonth("12");
        testCard.setExpiryYear("2025");
        testCard.setCvv("123");
        testCard.setUser(testUser);

        // ── Build a test card DTO ────────────────────────────────────────────

        testCardDTO = new PaymentCardDTO(
                10,
                "4111111111111111",
                CardType.VISA,
                "12",
                "2025"
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  addCard()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addCard()")
    class AddCard {

        @Test
        @DisplayName("should save a valid VISA card with automatic type detection")
        void addCard_validVISA_savesAndReturnsDTO() {
            // ARRANGE
            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(testCard);
            when(paymentCardMapper.toDto(testCard)).thenReturn(testCardDTO);

            // ACT
            PaymentCardDTO result = paymentCardService.addCard(
                    1,
                    "4111111111111111",
                    "Jane Smith",
                    "12",
                    "2025",
                    "123",
                    "VISA"
            );

            // ASSERT
            assertNotNull(result);
            assertEquals(10, result.id());
            assertEquals("4111111111111111", result.cardNumber());
            assertEquals(CardType.VISA, result.cardType());
            verify(paymentCardRepository, times(1)).save(any(PaymentCard.class));
            verify(paymentCardMapper, times(1)).toDto(testCard);
        }

        @Test
        @DisplayName("should detect VISA card type automatically from card number")
        void addCard_VISANumber_detectsTypeFromPrefix() {
            // ARRANGE
            PaymentCard capturedCard = new PaymentCard();
            capturedCard.setId(11);
            capturedCard.setCardNumber("4111111111111111");
            capturedCard.setCardholderName("Jane Smith");
            capturedCard.setCardType(CardType.VISA);
            capturedCard.setExpiryMonth("12");
            capturedCard.setExpiryYear("2025");

            PaymentCardDTO detectedCardDTO = new PaymentCardDTO(
                    11,
                    "4111111111111111",
                    CardType.VISA,
                    "12",
                    "2025"
            );

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(capturedCard);
            when(paymentCardMapper.toDto(capturedCard)).thenReturn(detectedCardDTO);

            // ACT - pass invalid card type to force auto-detection
            PaymentCardDTO result = paymentCardService.addCard(
                    1,
                    "4111111111111111",
                    "Jane Smith",
                    "12",
                    "2025",
                    "123",
                    "INVALID_TYPE"
            );

            // ASSERT
            assertNotNull(result);
            assertEquals(CardType.VISA, result.cardType());
            verify(paymentCardRepository, times(1)).save(any(PaymentCard.class));
        }

        @Test
        @DisplayName("should detect MASTERCARD type from 51-55 prefix")
        void addCard_MastercardPrefix51_detectsMastercard() {
            // ARRANGE
            PaymentCard mastercardCard = new PaymentCard();
            mastercardCard.setId(12);
            mastercardCard.setCardNumber("5555555555554444");
            mastercardCard.setCardholderName("Jane Smith");
            mastercardCard.setCardType(CardType.MASTERCARD);
            mastercardCard.setExpiryMonth("12");
            mastercardCard.setExpiryYear("2025");

            PaymentCardDTO mastercardDTO = new PaymentCardDTO(
                    12,
                    "5555555555554444",
                    CardType.MASTERCARD,
                    "12",
                    "2025"
            );

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(mastercardCard);
            when(paymentCardMapper.toDto(mastercardCard)).thenReturn(mastercardDTO);

            // ACT
            PaymentCardDTO result = paymentCardService.addCard(
                    1,
                    "5555555555554444",
                    "Jane Smith",
                    "12",
                    "2025",
                    "123",
                    "INVALID"
            );

            // ASSERT
            assertNotNull(result);
            assertEquals(CardType.MASTERCARD, result.cardType());
        }

        @Test
        @DisplayName("should detect MASTERCARD from 22-27 prefix")
        void addCard_MastercardPrefix22_detectsMastercard() {
            // ARRANGE
            PaymentCard mastercardCard = new PaymentCard();
            mastercardCard.setId(13);
            mastercardCard.setCardNumber("2221000000000009");
            mastercardCard.setCardholderName("Jane Smith");
            mastercardCard.setCardType(CardType.MASTERCARD);
            mastercardCard.setExpiryMonth("12");
            mastercardCard.setExpiryYear("2025");

            PaymentCardDTO mastercardDTO = new PaymentCardDTO(
                    13,
                    "2221000000000009",
                    CardType.MASTERCARD,
                    "12",
                    "2025"
            );

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(mastercardCard);
            when(paymentCardMapper.toDto(mastercardCard)).thenReturn(mastercardDTO);

            // ACT
            PaymentCardDTO result = paymentCardService.addCard(
                    1,
                    "2221000000000009",
                    "Jane Smith",
                    "12",
                    "2025",
                    "123",
                    "INVALID"
            );

            // ASSERT
            assertNotNull(result);
            assertEquals(CardType.MASTERCARD, result.cardType());
        }

        @Test
        @DisplayName("should detect AMERICAN_EXPRESS from 34 or 37 prefix")
        void addCard_AmexPrefix34_detectsAmex() {
            // ARRANGE
            PaymentCard amexCard = new PaymentCard();
            amexCard.setId(14);
            amexCard.setCardNumber("378282246310005");
            amexCard.setCardholderName("Jane Smith");
            amexCard.setCardType(CardType.AMERICAN_EXPRESS);
            amexCard.setExpiryMonth("12");
            amexCard.setExpiryYear("2025");

            PaymentCardDTO amexDTO = new PaymentCardDTO(
                    14,
                    "378282246310005",
                    CardType.AMERICAN_EXPRESS,
                    "12",
                    "2025"
            );

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(amexCard);
            when(paymentCardMapper.toDto(amexCard)).thenReturn(amexDTO);

            // ACT
            PaymentCardDTO result = paymentCardService.addCard(
                    1,
                    "378282246310005",
                    "Jane Smith",
                    "12",
                    "2025",
                    "123",
                    "INVALID"
            );

            // ASSERT
            assertNotNull(result);
            assertEquals(CardType.AMERICAN_EXPRESS, result.cardType());
        }

        @Test
        @DisplayName("should resolve to OTHER when card type not recognized")
        void addCard_UnrecognizedPrefix_defaultsToOther() {
            // ARRANGE
            PaymentCard otherCard = new PaymentCard();
            otherCard.setId(15);
            otherCard.setCardNumber("6011111111111117");
            otherCard.setCardholderName("Jane Smith");
            otherCard.setCardType(CardType.OTHER);
            otherCard.setExpiryMonth("12");
            otherCard.setExpiryYear("2025");

            PaymentCardDTO otherDTO = new PaymentCardDTO(
                    15,
                    "6011111111111117",
                    CardType.OTHER,
                    "12",
                    "2025"
            );

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(otherCard);
            when(paymentCardMapper.toDto(otherCard)).thenReturn(otherDTO);

            // ACT
            PaymentCardDTO result = paymentCardService.addCard(
                    1,
                    "6011111111111117",
                    "Jane Smith",
                    "12",
                    "2025",
                    "123",
                    "INVALID"
            );

            // ASSERT
            assertNotNull(result);
            assertEquals(CardType.OTHER, result.cardType());
        }

        @Test
        @DisplayName("should strip whitespace from card number")
        void addCard_cardNumberWithWhitespace_stripped() {
            // ARRANGE
            String cardNumberWithSpaces = "4111 1111 1111 1111";
            ArgumentCaptor<PaymentCard> captor = ArgumentCaptor.forClass(PaymentCard.class);

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(testCard);
            when(paymentCardMapper.toDto(testCard)).thenReturn(testCardDTO);

            // ACT
            paymentCardService.addCard(
                    1,
                    cardNumberWithSpaces,
                    "Jane Smith",
                    "12",
                    "2025",
                    "123",
                    "VISA"
            );

            // ASSERT
            verify(paymentCardRepository).save(captor.capture());
            assertEquals("4111111111111111", captor.getValue().getCardNumber());
        }

        @Test
        @DisplayName("should trim cardholder name before saving")
        void addCard_cardholderNameWithWhitespace_trimmed() {
            // ARRANGE
            String nameWithSpaces = "  Jane Smith  ";
            ArgumentCaptor<PaymentCard> captor = ArgumentCaptor.forClass(PaymentCard.class);

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(testCard);
            when(paymentCardMapper.toDto(testCard)).thenReturn(testCardDTO);

            // ACT
            paymentCardService.addCard(
                    1,
                    "4111111111111111",
                    nameWithSpaces,
                    "12",
                    "2025",
                    "123",
                    "VISA"
            );

            // ASSERT
            verify(paymentCardRepository).save(captor.capture());
            assertEquals("Jane Smith", captor.getValue().getCardholderName());
        }

        @Test
        @DisplayName("should throw exception when card number is null")
        void addCard_nullCardNumber_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            null,
                            "Jane Smith",
                            "12",
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when card number is blank")
        void addCard_blankCardNumber_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "   ",
                            "Jane Smith",
                            "12",
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when card number has less than 13 digits")
        void addCard_tooShortCardNumber_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "411111111111",  // 12 digits
                            "Jane Smith",
                            "12",
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when card number has more than 19 digits")
        void addCard_tooLongCardNumber_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "41111111111111111111",  // 20 digits
                            "Jane Smith",
                            "12",
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when card number contains non-digit characters")
        void addCard_nonNumericCardNumber_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "4111 AAAA AAAA AAAA",
                            "Jane Smith",
                            "12",
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when cardholder name is null")
        void addCard_nullCardholderName_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "4111111111111111",
                            null,
                            "12",
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when cardholder name is blank")
        void addCard_blankCardholderName_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "4111111111111111",
                            "   ",
                            "12",
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when expiry month is null")
        void addCard_nullExpiryMonth_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "4111111111111111",
                            "Jane Smith",
                            null,
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when expiry month is invalid (00)")
        void addCard_invalidExpiryMonth00_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "4111111111111111",
                            "Jane Smith",
                            "00",
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when expiry month is invalid (13)")
        void addCard_invalidExpiryMonth13_throwsException() {

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "4111111111111111",
                            "Jane Smith",
                            "13",
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when expiry year is invalid (1 digit)")
        void addCard_invalidExpiryYear1Digit_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "4111111111111111",
                            "Jane Smith",
                            "12",
                            "5",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when expiry year is invalid (5 digits)")
        void addCard_invalidExpiryYear5Digits_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "4111111111111111",
                            "Jane Smith",
                            "12",
                            "20255",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when CVV is null")
        void addCard_nullCVV_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "4111111111111111",
                            "Jane Smith",
                            "12",
                            "2025",
                            null,
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when CVV is blank")
        void addCard_blankCVV_throwsException() {
            // ARRANGE

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            1,
                            "4111111111111111",
                            "Jane Smith",
                            "12",
                            "2025",
                            "   ",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when user does not exist")
        void addCard_userNotFound_throwsException() {
            // ARRANGE
            when(userService.getById(999)).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.addCard(
                            999,
                            "4111111111111111",
                            "Jane Smith",
                            "12",
                            "2025",
                            "123",
                            "VISA"
                    )
            );
            verify(paymentCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("should accept 2-digit year format")
        void addCard_twoDigitYear_accepted() {
            // ARRANGE
            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(testCard);
            when(paymentCardMapper.toDto(testCard)).thenReturn(testCardDTO);

            // ACT
            PaymentCardDTO result = paymentCardService.addCard(
                    1,
                    "4111111111111111",
                    "Jane Smith",
                    "12",
                    "25",  // 2-digit year
                    "123",
                    "VISA"
            );

            // ASSERT
            assertNotNull(result);
            verify(paymentCardRepository, times(1)).save(any(PaymentCard.class));
        }

        @Test
        @DisplayName("should accept 4-digit year format")
        void addCard_fourDigitYear_accepted() {
            // ARRANGE
            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(testCard);
            when(paymentCardMapper.toDto(testCard)).thenReturn(testCardDTO);

            // ACT
            PaymentCardDTO result = paymentCardService.addCard(
                    1,
                    "4111111111111111",
                    "Jane Smith",
                    "12",
                    "2025",  // 4-digit year
                    "123",
                    "VISA"
            );

            // ASSERT
            assertNotNull(result);
            verify(paymentCardRepository, times(1)).save(any(PaymentCard.class));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  removeCard()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("removeCard()")
    class RemoveCard {

        @Test
        @DisplayName("should successfully remove card owned by user")
        void removeCard_validCardAndUser_removesCard() {
            // ARRANGE
            when(paymentCardRepository.findById(10)).thenReturn(testCard);

            // ACT
            paymentCardService.removeCard(10, 1);

            // ASSERT
            verify(paymentCardRepository, times(1)).findById(10);
            verify(paymentCardRepository, times(1)).delete(testCard);
        }

        @Test
        @DisplayName("should throw exception when card does not exist")
        void removeCard_cardNotFound_throwsException() {
            // ARRANGE
            when(paymentCardRepository.findById(999)).thenReturn(null);

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.removeCard(999, 1)
            );
            verify(paymentCardRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should throw exception when user does not own the card")
        void removeCard_differentUser_throwsException() {
            // ARRANGE
            when(paymentCardRepository.findById(10)).thenReturn(testCard);

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.removeCard(10, 999)  // Different userId
            );
            verify(paymentCardRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should not delete card if ownership verification fails")
        void removeCard_failedOwnershipCheck_noDelete() {
            // ARRANGE
            PaymentCard anotherUsersCard = new PaymentCard();
            anotherUsersCard.setId(10);
            User otherUser = new User();
            otherUser.setId(99);
            anotherUsersCard.setUser(otherUser);

            when(paymentCardRepository.findById(10)).thenReturn(anotherUsersCard);

            // ACT & ASSERT
            assertThrows(IllegalArgumentException.class, () ->
                    paymentCardService.removeCard(10, 1)  // User 1 tries to remove user 99's card
            );
            verify(paymentCardRepository, never()).delete(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getById() - inherited from BaseService
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("should retrieve card by ID")
        void getById_validId_returnsCard() {
            // ARRANGE
            when(paymentCardRepository.findById(10)).thenReturn(testCard);

            // ACT
            Optional<PaymentCard> result = paymentCardService.getById(10);

            // ASSERT
            assertTrue(result.isPresent());
            assertEquals(testCard, result.get());
            verify(paymentCardRepository, times(1)).findById(10);
        }

        @Test
        @DisplayName("should return empty Optional when card not found")
        void getById_nonexistentId_returnsEmpty() {
            // ARRANGE
            when(paymentCardRepository.findById(999)).thenReturn(null);

            // ACT
            Optional<PaymentCard> result = paymentCardService.getById(999);

            // ASSERT
            assertTrue(result.isEmpty());
            verify(paymentCardRepository, times(1)).findById(999);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  save() - inherited from BaseService
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should persist card entity and return it")
        void save_validCard_persistsAndReturns() {
            // ARRANGE
            when(paymentCardRepository.save(testCard)).thenReturn(testCard);

            // ACT
            PaymentCard result = paymentCardService.save(testCard);

            // ASSERT
            assertNotNull(result);
            assertEquals(testCard, result);
            verify(paymentCardRepository, times(1)).save(testCard);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  delete() - inherited from BaseService
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should delete card entity")
        void delete_validCard_deletesCard() {
            // ARRANGE & ACT
            paymentCardService.delete(testCard);

            // ASSERT
            verify(paymentCardRepository, times(1)).delete(testCard);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getAll() - inherited from BaseService
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("should return all cards when they exist")
        void getAll_cardsExist_returnsAll() {
            // ARRANGE
            List<PaymentCard> cards = List.of(testCard);
            when(paymentCardRepository.findAll()).thenReturn(cards);

            // ACT
            List<PaymentCard> result = paymentCardService.getAll();

            // ASSERT
            assertEquals(1, result.size());
            assertEquals(testCard, result.get(0));
            verify(paymentCardRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("should return empty list when no cards exist")
        void getAll_noCards_returnsEmpty() {
            // ARRANGE
            when(paymentCardRepository.findAll()).thenReturn(new ArrayList<>());

            // ACT
            List<PaymentCard> result = paymentCardService.getAll();

            // ASSERT
            assertTrue(result.isEmpty());
            verify(paymentCardRepository, times(1)).findAll();
        }
    }
}
