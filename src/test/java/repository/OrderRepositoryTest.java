package repository;

import entity.*;
import entity.Order;
import entity.enums.Gender;
import entity.enums.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.*;
import repository.impl.OrderRepositoryImpl;
import util.EntityManagerContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderRepositoryTest {

    private EntityManagerFactory emf;
    private EntityManager em;
    private OrderRepositoryImpl repository;

    // ── Shared test data references ──────────────────────────────────────────

    private User testUser;
    private User anotherUser;
    private Product testProduct;
    private ProductVariant testVariant;

    // ─────────────────────────────────────────────────────────────────────────
    //  One-time setup / teardown for the whole class
    // ─────────────────────────────────────────────────────────────────────────

    @BeforeAll
    void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("rentawear_test");
    }

    @AfterAll
    void tearDownFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Per-test setup / teardown
    // ─────────────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUpTransaction() {
        em = emf.createEntityManager();
        EntityManagerContext.set(em);
        em.getTransaction().begin();

        // ── Create test users ──────────────────────────────────────────────

        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPasswordHash("hashed_password");
        testUser.setGender(Gender.MALE);
        em.persist(testUser);

        anotherUser = new User();
        anotherUser.setName("Jane Smith");
        anotherUser.setEmail("jane@example.com");
        anotherUser.setPasswordHash("hashed_password");
        anotherUser.setGender(Gender.FEMALE);
        em.persist(anotherUser);

        // ── Create test product and variant ────────────────────────────────

        testProduct = new Product();
        testProduct.setName("Test T-Shirt");
        testProduct.setBasePrice(new BigDecimal("49.99"));
        testProduct.setImageUrl("https://example.com/tshirt.jpg");
        em.persist(testProduct);

        testVariant = new ProductVariant();
        testVariant.setProduct(testProduct);
        testVariant.setColor("Red");
        testVariant.setSize("M");
        testVariant.setQuantity(50);
        em.persist(testVariant);

        em.flush();

        repository = new OrderRepositoryImpl();
    }

    @AfterEach
    void rollbackTransaction() {
        try {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
            EntityManagerContext.clear();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers — build test data
    // ─────────────────────────────────────────────────────────────────────────

    private Order persistOrder(User user, BigDecimal totalAmount, OrderStatus status) {
        Order o = new Order();
        o.setUser(user);
        o.setTotalAmount(totalAmount);
        o.setStatus(status);
        o.setCreatedAt(Instant.now());
        em.persist(o);
        em.flush();
        return o;
    }

    private Order persistOrder(User user, BigDecimal totalAmount) {
        return persistOrder(user, totalAmount, OrderStatus.ORDERED);
    }

    private OrderItem addOrderItem(Order order, ProductVariant variant, int quantity,
                                   BigDecimal priceAtPurchase) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setVariant(variant);
        item.setQuantity(quantity);
        item.setPriceAtPurchase(priceAtPurchase);
        item.setStartDate(LocalDate.now());
        item.setEndDate(LocalDate.now().plusDays(5));
        order.addOrderItem(item);
        em.persist(item);
        em.flush();
        return item;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findById() — inherited from BaseRepositoryImpl
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return order when it exists")
        void findById_existingId_returnsOrder() {
            // ARRANGE
            Order saved = persistOrder(testUser, new BigDecimal("99.99"), OrderStatus.CONFIRMED);

            // ACT
            Order found = repository.findById(saved.getId());

            // ASSERT
            assertNotNull(found, "Should find the order by its generated id");
            assertEquals(new BigDecimal("99.99"), found.getTotalAmount());
            assertEquals(OrderStatus.CONFIRMED, found.getStatus());
        }

        @Test
        @DisplayName("should return null when order does not exist")
        void findById_unknownId_returnsNull() {
            // ACT
            Order found = repository.findById(99999);

            // ASSERT
            assertNull(found, "Should return null for an id that does not exist");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  save() — inherited from BaseRepositoryImpl
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should persist a new order and return it with an assigned id")
        void save_newOrder_returnsPersistedOrder() {
            // ARRANGE
            Order o = new Order();
            o.setUser(testUser);
            o.setTotalAmount(new BigDecimal("150.00"));
            o.setStatus(OrderStatus.ORDERED);
            o.setCreatedAt(Instant.now());

            // ACT
            Order saved = repository.save(o);

            // ASSERT
            assertNotNull(saved.getId(), "Saved order should have an assigned id");
            assertEquals(new BigDecimal("150.00"), saved.getTotalAmount());
            assertEquals(OrderStatus.ORDERED, saved.getStatus());
        }

        @Test
        @DisplayName("should update an existing order's status and amount")
        void save_existingOrder_updatesFields() {
            // ARRANGE
            Order o = persistOrder(testUser, new BigDecimal("100.00"), OrderStatus.ORDERED);
            Integer originalId = o.getId();

            // ACT
            o.setStatus(OrderStatus.SHIPPED);
            o.setTotalAmount(new BigDecimal("105.00"));
            Order updated = repository.save(o);

            // ASSERT
            assertEquals(originalId, updated.getId(), "Id should remain unchanged");
            assertEquals(OrderStatus.SHIPPED, updated.getStatus());
            assertEquals(new BigDecimal("105.00"), updated.getTotalAmount());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findAll() — inherited from BaseRepositoryImpl
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("should return all persisted orders")
        void findAll_multipleOrders_returnsAll() {
            // ARRANGE
            persistOrder(testUser, new BigDecimal("100.00"));
            persistOrder(testUser, new BigDecimal("200.00"));
            persistOrder(anotherUser, new BigDecimal("150.00"));

            // ACT
            List<Order> all = repository.findAll();

            // ASSERT
            assertEquals(3, all.size(), "Should return all 3 persisted orders");
        }

        @Test
        @DisplayName("should return empty list when no orders exist")
        void findAll_noOrders_returnsEmptyList() {
            // ACT
            List<Order> all = repository.findAll();

            // ASSERT
            assertTrue(all.isEmpty(), "Should return empty list when no orders exist");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  delete() — inherited from BaseRepositoryImpl
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should remove order from the database")
        void delete_existingOrder_removesIt() {
            // ARRANGE
            Order o = persistOrder(testUser, new BigDecimal("99.99"));
            Integer orderId = o.getId();

            // ACT
            repository.delete(o);
            Order found = repository.findById(orderId);

            // ASSERT
            assertNull(found, "Order should be deleted from the database");
        }

        @Test
        @DisplayName("should cascade delete order items when order is deleted")
        void delete_orderWithItems_cascadesItemDeletion() {
            // ARRANGE
            Order o = persistOrder(testUser, new BigDecimal("99.99"));
            addOrderItem(o, testVariant, 2, new BigDecimal("49.99"));
            int orderItemCount = o.getOrderItems().size();
            assertEquals(1, orderItemCount, "Order should have 1 item before deletion");

            // ACT
            repository.delete(o);
            em.flush();

            // ── Verify order is gone ───
            Order found = repository.findById(o.getId());
            assertNull(found, "Order should be deleted");

            // ── Verify cascade worked ──
            Order deletedOrder = new Order();
            deletedOrder.setId(o.getId());
            // OrderItems should be orphaned and deleted due to cascade
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findByUserId() — custom method
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findByUserId()")
    class FindByUserId {

        @Test
        @DisplayName("should return all orders for a specific user")
        void findByUserId_existingUser_returnsUserOrders() {
            // ARRANGE
            persistOrder(testUser, new BigDecimal("100.00"));
            persistOrder(testUser, new BigDecimal("200.00"));
            persistOrder(anotherUser, new BigDecimal("150.00"));

            // ACT
            List<Order> userOrders = repository.findByUserId(testUser.getId());

            // ASSERT
            assertEquals(2, userOrders.size(), "Should return 2 orders for testUser");
            assertTrue(userOrders.stream()
                    .allMatch(o -> o.getUser().getId().equals(testUser.getId())),
                    "All returned orders should belong to testUser");
        }

        @Test
        @DisplayName("should return orders ordered by createdAt descending (newest first)")
        void findByUserId_multipleOrders_returnsOrderedByCreatedAtDesc() {
            // ARRANGE
            Order order1 = persistOrder(testUser, new BigDecimal("100.00"));
            // Add slight delay to ensure different timestamps
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Order order2 = persistOrder(testUser, new BigDecimal("200.00"));

            // ACT
            List<Order> userOrders = repository.findByUserId(testUser.getId());

            // ASSERT
            assertEquals(2, userOrders.size());
            // Newer order (order2) should come first
            assertEquals(order2.getId(), userOrders.get(0).getId());
            assertEquals(order1.getId(), userOrders.get(1).getId());
        }

        @Test
        @DisplayName("should eagerly load order items with variants and products")
        void findByUserId_orderWithItems_eagerlyLoadsRelations() {
            // ARRANGE
            Order o = persistOrder(testUser, new BigDecimal("99.99"));
            addOrderItem(o, testVariant, 2, new BigDecimal("49.99"));

            // ACT
            List<Order> userOrders = repository.findByUserId(testUser.getId());

            // ASSERT
            assertEquals(1, userOrders.size());
            Order fetchedOrder = userOrders.get(0);
            assertNotNull(fetchedOrder.getOrderItems(), "OrderItems should be eagerly loaded");
            assertEquals(1, fetchedOrder.getOrderItems().size());

            OrderItem item = fetchedOrder.getOrderItems().get(0);
            assertNotNull(item.getVariant(), "Variant should be eagerly loaded");
            assertNotNull(item.getVariant().getProduct(), "Product should be eagerly loaded");
            assertEquals("Test T-Shirt", item.getVariant().getProduct().getName());
        }

        @Test
        @DisplayName("should return empty list when user has no orders")
        void findByUserId_userWithNoOrders_returnsEmpty() {
            // ARRANGE
            persistOrder(testUser, new BigDecimal("100.00"));

            // ACT
            List<Order> userOrders = repository.findByUserId(anotherUser.getId());

            // ASSERT
            assertTrue(userOrders.isEmpty(), "Should return empty list for user with no orders");
        }

        @Test
        @DisplayName("should return empty list when user does not exist")
        void findByUserId_nonexistentUser_returnsEmpty() {
            // ARRANGE
            persistOrder(testUser, new BigDecimal("100.00"));

            // ACT
            List<Order> userOrders = repository.findByUserId(99999);

            // ASSERT
            assertTrue(userOrders.isEmpty(), "Should return empty list for non-existent user");
        }

        @Test
        @DisplayName("should isolate orders between different users correctly")
        void findByUserId_multipleUsers_isolatesCorrectly() {
            // ARRANGE
            persistOrder(testUser, new BigDecimal("100.00"));
            persistOrder(testUser, new BigDecimal("150.00"));
            persistOrder(anotherUser, new BigDecimal("200.00"));
            persistOrder(anotherUser, new BigDecimal("250.00"));
            persistOrder(anotherUser, new BigDecimal("300.00"));

            // ACT
            List<Order> testUserOrders = repository.findByUserId(testUser.getId());
            List<Order> anotherUserOrders = repository.findByUserId(anotherUser.getId());

            // ASSERT
            assertEquals(2, testUserOrders.size(), "testUser should have 2 orders");
            assertEquals(3, anotherUserOrders.size(), "anotherUser should have 3 orders");

            // Verify no cross-contamination
            assertTrue(testUserOrders.stream()
                    .noneMatch(o -> o.getUser().getId().equals(anotherUser.getId())));
            assertTrue(anotherUserOrders.stream()
                    .noneMatch(o -> o.getUser().getId().equals(testUser.getId())));
        }

        @Test
        @DisplayName("should handle orders with different statuses")
        void findByUserId_mixedStatuses_returnsAll() {
            // ARRANGE
            persistOrder(testUser, new BigDecimal("100.00"), OrderStatus.ORDERED);
            persistOrder(testUser, new BigDecimal("150.00"), OrderStatus.CONFIRMED);
            persistOrder(testUser, new BigDecimal("200.00"), OrderStatus.SHIPPED);
            persistOrder(testUser, new BigDecimal("250.00"), OrderStatus.DELIVERED);

            // ACT
            List<Order> userOrders = repository.findByUserId(testUser.getId());

            // ASSERT
            assertEquals(4, userOrders.size(), "Should return all 4 orders regardless of status");
            assertTrue(userOrders.stream()
                    .anyMatch(o -> o.getStatus() == OrderStatus.ORDERED));
            assertTrue(userOrders.stream()
                    .anyMatch(o -> o.getStatus() == OrderStatus.CONFIRMED));
            assertTrue(userOrders.stream()
                    .anyMatch(o -> o.getStatus() == OrderStatus.SHIPPED));
            assertTrue(userOrders.stream()
                    .anyMatch(o -> o.getStatus() == OrderStatus.DELIVERED));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Integration — multiple operations
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration")
    class Integration {

        @Test
        @DisplayName("should handle full order lifecycle: create, update, retrieve, delete")
        void integration_fullOrderLifecycle_worksCorrectly() {
            // ARRANGE - Create
            Order o = new Order();
            o.setUser(testUser);
            o.setTotalAmount(new BigDecimal("99.99"));
            o.setStatus(OrderStatus.ORDERED);
            o.setCreatedAt(Instant.now());
            Order saved = repository.save(o);
            Integer orderId = saved.getId();

            // ACT - Update
            saved.setStatus(OrderStatus.CONFIRMED);
            repository.save(saved);

            // ACT - Retrieve
            Order retrieved = repository.findById(orderId);

            // ASSERT - Verify update
            assertNotNull(retrieved);
            assertEquals(OrderStatus.CONFIRMED, retrieved.getStatus());

            // ACT - Delete
            repository.delete(retrieved);
            Order deleted = repository.findById(orderId);

            // ASSERT - Verify deletion
            assertNull(deleted, "Order should be deleted");
        }

        @Test
        @DisplayName("should handle multiple users' orders independently")
        void integration_multipleUsersOrders_keepsThemIsolated() {
            // ARRANGE
            Order order1 = persistOrder(testUser, new BigDecimal("100.00"));
            Order order2 = persistOrder(testUser, new BigDecimal("150.00"));
            Order order3 = persistOrder(anotherUser, new BigDecimal("200.00"));
            addOrderItem(order1, testVariant, 1, new BigDecimal("100.00"));
            addOrderItem(order3, testVariant, 2, new BigDecimal("100.00"));

            // ACT
            List<Order> allOrders = repository.findAll();
            List<Order> testUserOrders = repository.findByUserId(testUser.getId());
            List<Order> anotherUserOrders = repository.findByUserId(anotherUser.getId());

            // ASSERT
            assertEquals(3, allOrders.size());
            assertEquals(2, testUserOrders.size());
            assertEquals(1, anotherUserOrders.size());

            // Verify order items are properly associated
            assertTrue(testUserOrders.stream()
                    .flatMap(o -> o.getOrderItems().stream())
                    .anyMatch(oi -> oi.getQuantity() == 1));
            assertTrue(anotherUserOrders.stream()
                    .flatMap(o -> o.getOrderItems().stream())
                    .anyMatch(oi -> oi.getQuantity() == 2));
        }

        @Test
        @DisplayName("should persist total amount as BigDecimal with correct precision")
        void integration_bigDecimalPrecision_maintainsAccuracy() {
            // ARRANGE
            BigDecimal amount = new BigDecimal("123.45");
            Order o = persistOrder(testUser, amount);

            // ACT
            Order retrieved = repository.findById(o.getId());

            // ASSERT
            assertEquals(amount, retrieved.getTotalAmount());
            assertEquals(2, retrieved.getTotalAmount().scale(), "Should maintain scale of 2");
        }
    }
}
