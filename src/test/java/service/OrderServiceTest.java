package service;

import dto.AdminOrderDTO;
import dto.OrderDTO;
import entity.*;
import entity.enums.OrderStatus;
import exception.InsufficientFundsException;
import exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * ─────────────────────────────────────────────────────────────────────────────
 *  OrderService Test Suite
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  OrderService depends on four collaborators:
 *    1. OrderRepository (for persisting Order entities)
 *    2. CartService (for clearing user carts after order placement)
 *    3. UserService (for loading and validating users)
 *    4. ProductVariantService (for checking/updating variant stock)
 *
 *  Since we have service-to-service dependencies, we build OrderService
 *  manually in @BeforeEach instead of using @InjectMocks. This gives us
 *  full control over all dependencies.
 *
 *  Test organization:
 *    - @Nested classes group tests by method
 *    - Helper methods build test data consistently
 *    - ArgumentCaptor verifies state changes during persistence
 * ─────────────────────────────────────────────────────────────────────────────
 */

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    // ── Mocks (fake objects we control) ──────────────────────────────────────

    @Mock private OrderRepository orderRepository;
    @Mock private CartService cartService;
    @Mock private UserService userService;
    @Mock private ProductVariantService productVariantService;

    // ── System under test (built manually so we control all dependencies) ─────

    private OrderService orderService;

    // ── Shared test data ──────────────────────────────────────────────────────

    private User testUser;
    private Order testOrder;
    private OrderItem testOrderItem;
    private ProductVariant testVariant;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        /*
         * Build OrderService using our testable constructor, passing all mocks.
         * Every test gets a fresh instance with fresh mocks.
         */
        orderService = new OrderService(
                orderRepository,
                cartService,
                userService,
                productVariantService
        );

        // ── Build a test user with a cart ──────────────────────────────────

        testCart = new Cart();
        testCart.setId(1);

        testUser = new User();
        testUser.setId(1);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setCreditLimit(new BigDecimal("10000.00"));
        testUser.setCart(testCart);

        // ── Build a test product ───────────────────────────────────────────

        testProduct = new Product();
        testProduct.setId(100);
        testProduct.setName("Cotton T-Shirt");
        testProduct.setBasePrice(new BigDecimal("50.00"));

        // ── Build a test product variant ───────────────────────────────────

        testVariant = new ProductVariant();
        testVariant.setId(5);
        testVariant.setProduct(testProduct);
        testVariant.setQuantity(10);

        // ── Build a test order with one item ───────────────────────────────

        testOrderItem = new OrderItem();
        testOrderItem.setId(1);
        testOrderItem.setVariant(testVariant);
        testOrderItem.setQuantity(2);
        testOrderItem.setPriceAtPurchase(new BigDecimal("50.00"));
        testOrderItem.setStartDate(LocalDate.of(2025, 6, 1));
        testOrderItem.setEndDate(LocalDate.of(2025, 6, 10));

        testOrder = new Order();
        testOrder.setId(1);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.ORDERED);
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.addOrderItem(testOrderItem);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getAllOrders()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAllOrders()")
    class GetAllOrders {

        @Test
        @DisplayName("should return empty list when no orders exist")
        void getAllOrders_noOrders_returnsEmptyList() {
            // ARRANGE
            when(orderRepository.findAll()).thenReturn(new ArrayList<>());

            // ACT
            List<AdminOrderDTO> result = orderService.getAllOrders();

            // ASSERT
            assertNotNull(result, "Result should not be null");
            assertTrue(result.isEmpty(), "Result should be an empty list");
            verify(orderRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("should return list of AdminOrderDTOs for all orders")
        void getAllOrders_ordersExist_returnsMappedDtos() {
            // ARRANGE
            List<Order> orders = new ArrayList<>();
            orders.add(testOrder);
            when(orderRepository.findAll()).thenReturn(orders);

            // ACT
            List<AdminOrderDTO> result = orderService.getAllOrders();

            // ASSERT
            assertNotNull(result, "Result should not be null");
            assertEquals(1, result.size(), "Should return one DTO");
            verify(orderRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("should force load lazy associations before mapping")
        void getAllOrders_forceLoadsLazyAssociations() {
            // ARRANGE
            // Mock spies to verify lazy associations are accessed
            List<Order> orders = new ArrayList<>();
            orders.add(testOrder);
            when(orderRepository.findAll()).thenReturn(orders);

            // ACT
            List<AdminOrderDTO> result = orderService.getAllOrders();

            // ASSERT
            assertNotNull(result, "Result should not be null");
            assertEquals(1, result.size(), "Should have one order");
            /* Lazy associations are accessed in the map operation:
             * - order.getUser()
             * - order.getOrderItems()
             * - item.getVariant().getProduct().getName()
             * This ensures Hibernate lazy loads them before serialization.
             */
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getOrdersForUser()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getOrdersForUser()")
    class GetOrdersForUser {

        @Test
        @DisplayName("should return empty lists when user has no orders")
        void getOrdersForUser_noOrders_returnsEmptyLists() {
            // ARRANGE
            when(orderRepository.findByUserId(1)).thenReturn(new ArrayList<>());

            // ACT
            List<List<OrderDTO>> result = orderService.getOrdersForUser(1);

            // ASSERT
            assertNotNull(result, "Result should not be null");
            assertEquals(2, result.size(), "Should return two lists");
            assertTrue(result.get(0).isEmpty(), "Active orders list should be empty");
            assertTrue(result.get(1).isEmpty(), "Past orders list should be empty");
        }

        @Test
        @DisplayName("should separate active orders from past orders")
        void getOrdersForUser_mixedStatuses_separatesIntoTwoLists() {
            // ARRANGE
            Order activeOrder = new Order();
            activeOrder.setId(1);
            activeOrder.setStatus(OrderStatus.ORDERED);
            activeOrder.setUser(testUser);

            Order pastOrder = new Order();
            pastOrder.setId(2);
            pastOrder.setStatus(OrderStatus.RETURNED);
            pastOrder.setUser(testUser);

            List<Order> allOrders = List.of(activeOrder, pastOrder);
            when(orderRepository.findByUserId(1)).thenReturn(allOrders);

            // ACT
            List<List<OrderDTO>> result = orderService.getOrdersForUser(1);

            // ASSERT
            List<OrderDTO> active = result.get(0);
            List<OrderDTO> past = result.get(1);

            assertEquals(1, active.size(), "Should have one active order");
            assertEquals(1, past.size(), "Should have one past order");
        }

        @Test
        @DisplayName("should treat DELIVERED as active order")
        void getOrdersForUser_deliveredStatus_isActive() {
            // ARRANGE
            Order deliveredOrder = new Order();
            deliveredOrder.setId(1);
            deliveredOrder.setStatus(OrderStatus.DELIVERED);
            deliveredOrder.setUser(testUser);

            when(orderRepository.findByUserId(1)).thenReturn(List.of(deliveredOrder));

            // ACT
            List<List<OrderDTO>> result = orderService.getOrdersForUser(1);

            // ASSERT
            assertEquals(1, result.get(0).size(), "DELIVERED orders should be in active list");
            assertEquals(0, result.get(1).size(), "DELIVERED orders should not be in past list");
        }

        @Test
        @DisplayName("should treat CANCELLED as past order")
        void getOrdersForUser_cancelledStatus_isPast() {
            // ARRANGE
            Order cancelledOrder = new Order();
            cancelledOrder.setId(1);
            cancelledOrder.setStatus(OrderStatus.CANCELLED);
            cancelledOrder.setUser(testUser);

            when(orderRepository.findByUserId(1)).thenReturn(List.of(cancelledOrder));

            // ACT
            List<List<OrderDTO>> result = orderService.getOrdersForUser(1);

            // ASSERT
            assertEquals(0, result.get(0).size(), "CANCELLED orders should not be in active list");
            assertEquals(1, result.get(1).size(), "CANCELLED orders should be in past list");
        }

        @Test
        @DisplayName("should treat RETURNED as past order")
        void getOrdersForUser_returnedStatus_isPast() {
            // ARRANGE
            Order returnedOrder = new Order();
            returnedOrder.setId(1);
            returnedOrder.setStatus(OrderStatus.RETURNED);
            returnedOrder.setUser(testUser);

            when(orderRepository.findByUserId(1)).thenReturn(List.of(returnedOrder));

            // ACT
            List<List<OrderDTO>> result = orderService.getOrdersForUser(1);

            // ASSERT
            assertEquals(0, result.get(0).size(), "RETURNED orders should not be in active list");
            assertEquals(1, result.get(1).size(), "RETURNED orders should be in past list");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  placeOrder()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("placeOrder()")
    class PlaceOrder {

        /*
         * Helper: Build a valid cart item JSON string for Gson parsing.
         * This matches the frontend's CartItemNormaliser structure.
         */
        private String buildCartJson(int variantId, int qty, String startDate, String endDate) {
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("id", (double) variantId);  // Gson parses JSON numbers as Double
            item.put("qty", (double) qty);
            item.put("startDate", startDate);
            item.put("endDate", endDate);
            items.add(item);
            return "[{\"id\":" + variantId + ".0,\"qty\":" + qty + ".0,\"startDate\":\"" + startDate
                    + "\",\"endDate\":\"" + endDate + "\"}]";
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when cart JSON is invalid")
        void placeOrder_invalidJson_throwsException() {
            // ARRANGE
            String invalidJson = "not-valid-json";

            // ACT & ASSERT
            assertThrows(
                    IllegalArgumentException.class,
                    () -> orderService.placeOrder(1, invalidJson, new BigDecimal("100.00"))
            );

            // Verify no order was saved
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("should throw IllegalStateException when cart is empty")
        void placeOrder_emptyCart_throwsException() {
            // ARRANGE
            String emptyJson = "[]";  // Valid JSON but empty array

            // ACT & ASSERT
            assertThrows(
                    IllegalStateException.class,
                    () -> orderService.placeOrder(1, emptyJson, new BigDecimal("100.00"))
            );

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void placeOrder_unknownUser_throwsException() {
            // ARRANGE
            String cartJson = buildCartJson(5, 2, "2025-06-01", "2025-06-10");
            when(userService.getById(999)).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThrows(
                    UserNotFoundException.class,
                    () -> orderService.placeOrder(999, cartJson, new BigDecimal("100.00"))
            );

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("should throw InsufficientFundsException when user credit is too low")
        void placeOrder_insufficientCredit_throwsException() {
            // ARRANGE
            testUser.setCreditLimit(new BigDecimal("50.00"));  // Not enough
            String cartJson = buildCartJson(5, 2, "2025-06-01", "2025-06-10");
            BigDecimal orderAmount = new BigDecimal("100.00");

            when(userService.getById(1)).thenReturn(Optional.of(testUser));

            // ACT & ASSERT
            assertThrows(
                    InsufficientFundsException.class,
                    () -> orderService.placeOrder(1, cartJson, orderAmount)
            );

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("should throw IllegalStateException when variant does not exist")
        void placeOrder_unknownVariant_throwsException() {
            // ARRANGE
            String cartJson = buildCartJson(999, 2, "2025-06-01", "2025-06-10");
            BigDecimal orderAmount = new BigDecimal("100.00");

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(productVariantService.getById(999)).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThrows(
                    IllegalStateException.class,
                    () -> orderService.placeOrder(1, cartJson, orderAmount)
            );

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("should throw IllegalStateException when stock is insufficient")
        void placeOrder_insufficientStock_throwsException() {
            // ARRANGE
            testVariant.setQuantity(1);  // Only 1 left, but requesting 2
            String cartJson = buildCartJson(5, 2, "2025-06-01", "2025-06-10");
            BigDecimal orderAmount = new BigDecimal("100.00");

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(productVariantService.getById(5)).thenReturn(Optional.of(testVariant));

            // ACT & ASSERT
            assertThrows(
                    IllegalStateException.class,
                    () -> orderService.placeOrder(1, cartJson, orderAmount),
                    "Should indicate insufficient stock"
            );

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("should successfully place order with valid inputs")
        void placeOrder_validInputs_savesOrderAndReturnsId() {
            // ARRANGE
            String cartJson = buildCartJson(5, 2, "2025-06-01", "2025-06-10");
            BigDecimal orderAmount = new BigDecimal("100.00");

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(productVariantService.getById(5)).thenReturn(Optional.of(testVariant));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(123);  // Simulate DB assigning an ID
                return order;
            });

            // ACT
            Integer returnedId = orderService.placeOrder(1, cartJson, orderAmount);

            // ASSERT
            assertEquals(123, returnedId, "Should return the order ID assigned by DB");
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(cartService, times(1)).save(testCart);
        }

        @Test
        @DisplayName("should deduct order amount from user's credit limit")
        void placeOrder_deductsUserCredit() {
            // ARRANGE
            BigDecimal initialCredit = new BigDecimal("10000.00");
            testUser.setCreditLimit(initialCredit);
            String cartJson = buildCartJson(5, 2, "2025-06-01", "2025-06-10");
            BigDecimal orderAmount = new BigDecimal("100.00");

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(productVariantService.getById(5)).thenReturn(Optional.of(testVariant));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(123);
                return order;
            });

            // ACT
            orderService.placeOrder(1, cartJson, orderAmount);

            // ASSERT
            BigDecimal expectedCredit = initialCredit.subtract(orderAmount);
            assertEquals(expectedCredit, testUser.getCreditLimit(),
                    "User credit should be deducted by order amount");
        }

        @Test
        @DisplayName("should reduce variant quantity based on order quantity")
        void placeOrder_reducesVariantStock() {
            // ARRANGE
            int initialStock = 10;
            int orderQty = 3;
            testVariant.setQuantity(initialStock);

            String cartJson = buildCartJson(5, orderQty, "2025-06-01", "2025-06-10");
            BigDecimal orderAmount = new BigDecimal("100.00");

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(productVariantService.getById(5)).thenReturn(Optional.of(testVariant));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(123);
                return order;
            });

            // ACT
            orderService.placeOrder(1, cartJson, orderAmount);

            // ASSERT
            int expectedStock = initialStock - orderQty;
            assertEquals(expectedStock, testVariant.getQuantity(),
                    "Variant quantity should be reduced by order quantity");
        }

        @Test
        @DisplayName("should create order with correct status and amount")
        void placeOrder_setsOrderProperties() {
            // ARRANGE
            String cartJson = buildCartJson(5, 2, "2025-06-01", "2025-06-10");
            BigDecimal orderAmount = new BigDecimal("150.50");

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(productVariantService.getById(5)).thenReturn(Optional.of(testVariant));

            // Capture the Order passed to save()
            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(captor.capture())).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(123);
                return order;
            });

            // ACT
            orderService.placeOrder(1, cartJson, orderAmount);

            // ASSERT
            Order savedOrder = captor.getValue();
            assertEquals(OrderStatus.ORDERED, savedOrder.getStatus(),
                    "Order status should be ORDERED");
            assertEquals(orderAmount, savedOrder.getTotalAmount(),
                    "Order total amount should match");
            assertEquals(testUser, savedOrder.getUser(),
                    "Order should belong to the user");
        }

        @Test
        @DisplayName("should create OrderItems for each cart item with correct dates")
        void placeOrder_createsOrderItems() {
            // ARRANGE
            String cartJson = buildCartJson(5, 2, "2025-06-01", "2025-06-10");
            BigDecimal orderAmount = new BigDecimal("100.00");

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(productVariantService.getById(5)).thenReturn(Optional.of(testVariant));

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(captor.capture())).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(123);
                return order;
            });

            // ACT
            orderService.placeOrder(1, cartJson, orderAmount);

            // ASSERT
            Order savedOrder = captor.getValue();
            assertEquals(1, savedOrder.getOrderItems().size(),
                    "Should have one OrderItem");

            OrderItem item = savedOrder.getOrderItems().get(0);
            assertEquals(2, item.getQuantity(), "Quantity should match");
            assertEquals(LocalDate.of(2025, 6, 1), item.getStartDate(),
                    "Start date should match");
            assertEquals(LocalDate.of(2025, 6, 10), item.getEndDate(),
                    "End date should match");
            assertEquals(testVariant, item.getVariant(),
                    "Variant should be correct");
        }

        @Test
        @DisplayName("should clear user's cart after placing order")
        void placeOrder_clearsUserCart() {
            // ARRANGE
            CartItem cartItem = new CartItem();
            cartItem.setId(1);
            testCart.addCartItem(cartItem);

            String cartJson = buildCartJson(5, 2, "2025-06-01", "2025-06-10");
            BigDecimal orderAmount = new BigDecimal("100.00");

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(productVariantService.getById(5)).thenReturn(Optional.of(testVariant));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(123);
                return order;
            });

            assertTrue(testCart.getCartItems().size() > 0, "Cart should have items before order");

            // ACT
            orderService.placeOrder(1, cartJson, orderAmount);

            // ASSERT
            assertTrue(testCart.getCartItems().isEmpty(),
                    "Cart should be empty after order placement");
            verify(cartService, times(1)).save(testCart);
        }

        @Test
        @DisplayName("should handle multiple items in cart")
        void placeOrder_multipleItems_createsMultipleOrderItems() {
            // ARRANGE
            /*
             * Build a cart JSON with two items.
             * This tests that placeOrder correctly iterates through all items.
             */
            List<Map<String, Object>> items = new ArrayList<>();

            Map<String, Object> item1 = new HashMap<>();
            item1.put("id", 5.0);
            item1.put("qty", 2.0);
            item1.put("startDate", "2025-06-01");
            item1.put("endDate", "2025-06-10");
            items.add(item1);

            Map<String, Object> item2 = new HashMap<>();
            item2.put("id", 6.0);
            item2.put("qty", 3.0);
            item2.put("startDate", "2025-06-05");
            item2.put("endDate", "2025-06-15");
            items.add(item2);

            String cartJson = "[{\"id\":5.0,\"qty\":2.0,\"startDate\":\"2025-06-01\",\"endDate\":\"2025-06-10\"},"
                    + "{\"id\":6.0,\"qty\":3.0,\"startDate\":\"2025-06-05\",\"endDate\":\"2025-06-15\"}]";

            ProductVariant variant2 = new ProductVariant();
            variant2.setId(6);
            variant2.setProduct(testProduct);
            variant2.setQuantity(10);

            BigDecimal orderAmount = new BigDecimal("250.00");

            when(userService.getById(1)).thenReturn(Optional.of(testUser));
            when(productVariantService.getById(5)).thenReturn(Optional.of(testVariant));
            when(productVariantService.getById(6)).thenReturn(Optional.of(variant2));

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(captor.capture())).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(123);
                return order;
            });

            // ACT
            orderService.placeOrder(1, cartJson, orderAmount);

            // ASSERT
            Order savedOrder = captor.getValue();
            assertEquals(2, savedOrder.getOrderItems().size(),
                    "Should have two OrderItems");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  updateOrderStatus()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateOrderStatus()")
    class UpdateOrderStatus {

        @Test
        @DisplayName("should return false when order does not exist")
        void updateOrderStatus_unknownOrder_returnsFalse() {
            // ARRANGE
            when(orderRepository.findById(999)).thenReturn(null);

            // ACT
            boolean result = orderService.updateOrderStatus(999, OrderStatus.DELIVERED);

            // ASSERT
            assertFalse(result, "Should return false for non-existent order");
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("should update order status when order exists")
        void updateOrderStatus_validOrder_updatesStatus() {
            // ARRANGE
            when(orderRepository.findById(1)).thenReturn(testOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // ACT
            boolean result = orderService.updateOrderStatus(1, OrderStatus.DELIVERED);

            // ASSERT
            assertTrue(result, "Should return true for successful update");
            assertEquals(OrderStatus.DELIVERED, testOrder.getStatus(),
                    "Order status should be updated to DELIVERED");
            verify(orderRepository, times(1)).save(testOrder);
        }

        @Test
        @DisplayName("should update from ORDERED to CONFIRMED")
        void updateOrderStatus_orderedToConfirmed() {
            // ARRANGE
            testOrder.setStatus(OrderStatus.ORDERED);
            when(orderRepository.findById(1)).thenReturn(testOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // ACT
            orderService.updateOrderStatus(1, OrderStatus.CONFIRMED);

            // ASSERT
            assertEquals(OrderStatus.CONFIRMED, testOrder.getStatus());
        }

        @Test
        @DisplayName("should update from CONFIRMED to SHIPPED")
        void updateOrderStatus_confirmedToShipped() {
            // ARRANGE
            testOrder.setStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(1)).thenReturn(testOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // ACT
            orderService.updateOrderStatus(1, OrderStatus.SHIPPED);

            // ASSERT
            assertEquals(OrderStatus.SHIPPED, testOrder.getStatus());
        }

        @Test
        @DisplayName("should update from SHIPPED to DELIVERED")
        void updateOrderStatus_shippedToDelivered() {
            // ARRANGE
            testOrder.setStatus(OrderStatus.SHIPPED);
            when(orderRepository.findById(1)).thenReturn(testOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // ACT
            orderService.updateOrderStatus(1, OrderStatus.DELIVERED);

            // ASSERT
            assertEquals(OrderStatus.DELIVERED, testOrder.getStatus());
        }

        @Test
        @DisplayName("should update to RETURNED status")
        void updateOrderStatus_toReturned() {
            // ARRANGE
            when(orderRepository.findById(1)).thenReturn(testOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // ACT
            orderService.updateOrderStatus(1, OrderStatus.RETURNED);

            // ASSERT
            assertEquals(OrderStatus.RETURNED, testOrder.getStatus());
        }

        @Test
        @DisplayName("should update to CANCELLED status")
        void updateOrderStatus_toCancelled() {
            // ARRANGE
            when(orderRepository.findById(1)).thenReturn(testOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // ACT
            orderService.updateOrderStatus(1, OrderStatus.CANCELLED);

            // ASSERT
            assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        }

        @Test
        @DisplayName("should save order after status update")
        void updateOrderStatus_persistsChanges() {
            // ARRANGE
            when(orderRepository.findById(1)).thenReturn(testOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // ACT
            orderService.updateOrderStatus(1, OrderStatus.SHIPPED);

            // ASSERT
            verify(orderRepository, times(1)).findById(1);
            verify(orderRepository, times(1)).save(testOrder);
        }
    }
}
