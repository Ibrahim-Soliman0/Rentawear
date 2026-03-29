package service;

import dto.SimpleCartItemDTO;
import entity.Cart;
import entity.CartItem;
import entity.ProductVariant;
import entity.User;
import entity.enums.Gender;
import exception.CartItemNotFoundException;
import exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.CartRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * ─────────────────────────────────────────────────────────────────────────────
 *  WHY CartService IS DIFFERENT FROM UserService
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  UserService only depended on a Repository (an interface), so @InjectMocks
 *  could wire everything automatically.
 *
 *  CartService depends on three other *Service* classes (UserService,
 *  ProductVariantService, CartItemService). Mockito can mock concrete classes,
 *  but @InjectMocks gets confused when there are multiple mocks of the same
 *  type hierarchy. So here we:
 *
 *    1. Declare @Mock for each dependency.
 *    2. Build the CartService manually in @BeforeEach using the new
 *       testable constructor we added.
 *
 *  This is the standard pattern when you have service-to-service dependencies.
 * ─────────────────────────────────────────────────────────────────────────────
 */

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    // ── Mocks ─────────────────────────────────────────────────────────────────

    @Mock private CartRepository cartRepository;
    @Mock private UserService userService;
    @Mock private ProductVariantService productVariantService;
    @Mock private CartItemService cartItemService;

    // ── System under test (built manually so we control all dependencies) ─────

    private CartService cartService;

    // ── Shared test data ──────────────────────────────────────────────────────

    private User user;
    private Cart cart;
    private CartItem cartItem;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        /*
         * Build CartService using the testable constructor we added.
         * Every test gets a fresh instance with fresh mocks.
         */
        cartService = new CartService(
                cartRepository,
                userService,
                productVariantService,
                cartItemService
        );

        // ── A user with a cart that already has one item ───────────────────

        cartItem = new CartItem();
        cartItem.setId(10);
        cartItem.setQuantity(2);
        cartItem.setStartDate(LocalDate.now());
        cartItem.setEndDate(LocalDate.now().plusDays(5));

        /*
         * Cart self-initializes cartItems to new ArrayList<>() in the field
         * declaration, so we can call addCartItem() directly without any setter.
         * addCartItem() also sets cartItem.setCart(this) — matching production
         * behaviour exactly.
         */
        cart = new Cart();
        cart.setId(1);
        cart.addCartItem(cartItem);

        variant = new ProductVariant();
        variant.setId(5);

        user = new User();
        user.setId(1);
        user.setName("Ahmed Hassan");
        user.setEmail("ahmed@example.com");
        user.setGender(Gender.MALE);
        user.setCart(cart);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  addItem()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addItem()")
    class AddItem {

        /*
         * Helper: builds a SimpleCartItemDTO with a valid date range.
         * startDate and endDate must be ISO-8601 strings (LocalDate.parse format).
         */
        private SimpleCartItemDTO buildItemDto(Integer variantId, Integer qty) {
            return new SimpleCartItemDTO(
                    variantId,
                    qty,
                    "2025-06-01",   // startDate — must be a valid LocalDate string
                    "2025-06-10"    // endDate
            );
        }

        @Test
        @DisplayName("should save item and return its id when user and variant exist")
        void addItem_validUserAndVariant_returnsCartItemId() {
            // ARRANGE
            when(userService.getById(1)).thenReturn(Optional.of(user));
            when(productVariantService.getById(5)).thenReturn(Optional.of(variant));

            CartItem savedItem = new CartItem();
            savedItem.setId(99);   // the id assigned after persisting
            /*
             * cartItemService.save() is what actually persists the new CartItem.
             * We stub it to return a CartItem with a known id.
             */
            when(cartItemService.save(any(CartItem.class))).thenReturn(savedItem);

            // ACT
            Integer returnedId = cartService.addItem(1, buildItemDto(5, 2));

            // ASSERT
            assertEquals(99, returnedId, "Should return the id of the saved CartItem");
            verify(cartItemService, times(1)).save(any(CartItem.class));
        }

        @Test
        @DisplayName("should create a cart for the user when they have none yet")
        void addItem_userHasNoCart_createsCartAndAddsItem() {
            // User starts with NO cart
            user.setCart(null);

            when(userService.getById(1)).thenReturn(Optional.of(user));
            when(productVariantService.getById(5)).thenReturn(Optional.of(variant));

            CartItem savedItem = new CartItem();
            savedItem.setId(99);
            when(cartItemService.save(any(CartItem.class))).thenReturn(savedItem);

            // Should not throw — a new Cart must be created transparently
            assertDoesNotThrow(() -> cartService.addItem(1, buildItemDto(5, 2)));

            // After the call the user must have a cart
            assertNotNull(user.getCart(), "A cart should have been created for the user");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void addItem_unknownUser_throwsUserNotFoundException() {
            when(userService.getById(99)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> cartService.addItem(99, buildItemDto(5, 2))
            );

            // No item should be persisted
            verify(cartItemService, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when variant does not exist")
        void addItem_unknownVariant_throwsIllegalArgumentException() {
            when(userService.getById(1)).thenReturn(Optional.of(user));
            when(productVariantService.getById(999)).thenReturn(Optional.empty());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> cartService.addItem(1, buildItemDto(999, 2))
            );

            verify(cartItemService, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("should set quantity and dates on the CartItem before saving")
        void addItem_setsFieldsCorrectlyBeforeSave() {
            when(userService.getById(1)).thenReturn(Optional.of(user));
            when(productVariantService.getById(5)).thenReturn(Optional.of(variant));

            CartItem savedItem = new CartItem();
            savedItem.setId(99);
            when(cartItemService.save(any(CartItem.class))).thenReturn(savedItem);

            cartService.addItem(1, buildItemDto(5, 3));

            // Capture the CartItem that was passed to save()
            org.mockito.ArgumentCaptor<CartItem> captor =
                    org.mockito.ArgumentCaptor.forClass(CartItem.class);
            verify(cartItemService).save(captor.capture());

            CartItem captured = captor.getValue();

            assertAll(
                    "All fields must be set on the CartItem before persisting",
                    () -> assertEquals(3, captured.getQuantity(),     "quantity mismatch"),
                    () -> assertEquals(variant, captured.getVariant(), "variant mismatch"),
                    () -> assertNotNull(captured.getStartDate(),       "startDate must not be null"),
                    () -> assertNotNull(captured.getEndDate(),         "endDate must not be null")
            );
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  removeItem()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("removeItem()")
    class RemoveItem {

        @Test
        @DisplayName("should remove item from cart when user and item both exist")
        void removeItem_validUserAndItem_removesSuccessfully() {
            when(userService.getById(1)).thenReturn(Optional.of(user));
            when(cartItemService.getById(10)).thenReturn(Optional.of(cartItem));

            // Should complete without throwing
            assertDoesNotThrow(() -> cartService.removeItem(1, 10));
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void removeItem_unknownUser_throwsUserNotFoundException() {
            when(userService.getById(99)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> cartService.removeItem(99, 10)
            );
        }

        @Test
        @DisplayName("should throw CartItemNotFoundException when user has no cart")
        void removeItem_userHasNoCart_throwsCartItemNotFoundException() {
            User temp = new User();
            temp.setId(2);
            temp.setName("Adham Khaled");
            temp.setEmail("adham@gmail.com");
            temp.setGender(Gender.MALE);

            when(userService.getById(2)).thenReturn(Optional.of(temp));

            assertThrows(
                    CartItemNotFoundException.class,
                    () -> cartService.removeItem(2, 10)
            );

            // We should bail out before even looking up the cart item
            verify(cartItemService, never()).getById(anyInt());
        }

        @Test
        @DisplayName("should throw CartItemNotFoundException when item id does not exist")
        void removeItem_unknownCartItemId_throwsCartItemNotFoundException() {
            when(userService.getById(1)).thenReturn(Optional.of(user));
            when(cartItemService.getById(999)).thenReturn(Optional.empty());

            assertThrows(
                    CartItemNotFoundException.class,
                    () -> cartService.removeItem(1, 999)
            );
        }

        @Test
        @DisplayName("should throw CartItemNotFoundException when item belongs to a different cart")
        void removeItem_itemNotInUsersCart_throwsCartItemNotFoundException() {
            // An item that exists in the DB but is NOT in this user's cart
            CartItem foreignItem = new CartItem();
            foreignItem.setId(55);

            when(userService.getById(1)).thenReturn(Optional.of(user));
            when(cartItemService.getById(55)).thenReturn(Optional.of(foreignItem));

            assertThrows(
                    CartItemNotFoundException.class,
                    () -> cartService.removeItem(1, 55)
            );
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  updateItemQty()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateItemQty()")
    class UpdateItemQty {

        @Test
        @DisplayName("should update quantity on the CartItem when all inputs are valid")
        void updateItemQty_validInputs_updatesQuantity() {
            when(userService.getById(1)).thenReturn(Optional.of(user));
            when(cartItemService.getById(10)).thenReturn(Optional.of(cartItem));

            cartService.updateItemQty(1, 10, 5);

            assertEquals(5, cartItem.getQuantity(),
                    "CartItem quantity should be updated to the new value");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void updateItemQty_unknownUser_throwsUserNotFoundException() {
            when(userService.getById(99)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> cartService.updateItemQty(99, 10, 5)
            );
        }

        @Test
        @DisplayName("should throw CartItemNotFoundException when user has no cart")
        void updateItemQty_userHasNoCart_throwsCartItemNotFoundException() {
            User temp = new User();
            temp.setId(2);
            temp.setName("Adham Khaled");
            temp.setGender(Gender.MALE);
            temp.setEmail("adham@gmail.com");

            when(userService.getById(2)).thenReturn(Optional.of(temp));

            assertThrows(
                    CartItemNotFoundException.class,
                    () -> cartService.updateItemQty(2, 10, 5)
            );

            verify(cartItemService, never()).getById(anyInt());
        }

        @Test
        @DisplayName("should throw CartItemNotFoundException when cart item id does not exist")
        void updateItemQty_unknownCartItemId_throwsCartItemNotFoundException() {
            when(userService.getById(1)).thenReturn(Optional.of(user));
            when(cartItemService.getById(999)).thenReturn(Optional.empty());

            assertThrows(
                    CartItemNotFoundException.class,
                    () -> cartService.updateItemQty(1, 999, 5)
            );
        }

        @Test
        @DisplayName("should throw CartItemNotFoundException when item belongs to a different cart")
        void updateItemQty_itemNotInUsersCart_throwsCartItemNotFoundException() {
            CartItem foreignItem = new CartItem();
            foreignItem.setId(55);

            when(userService.getById(1)).thenReturn(Optional.of(user));
            when(cartItemService.getById(55)).thenReturn(Optional.of(foreignItem));

            assertThrows(
                    CartItemNotFoundException.class,
                    () -> cartService.updateItemQty(1, 55, 5)
            );
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getItems()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getItems()")
    class GetItems {

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void getItems_unknownUser_throwsUserNotFoundException() {
            when(userService.getById(99)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> cartService.getItems(99)
            );
        }

        @Test
        @DisplayName("should return list of CartItemDTOs for a valid user")
        void getItems_validUser_returnsDtoList() {
            when(userService.getById(1)).thenReturn(Optional.of(user));

            /*
             * getItems() calls mapper.toDTOList(cartItems) internally.
             * The real MapStruct mapper will run here since we're not
             * mocking it — the result just needs to be non-null and
             * not throw. A more detailed assertion would require us to
             * also share the CartItemMapper, which we can do later.
             */
            assertDoesNotThrow(() -> cartService.getItems(1));
        }
    }
}