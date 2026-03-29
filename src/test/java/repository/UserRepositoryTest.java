package repository;

import entity.Category;
import entity.Cart;
import entity.PaymentCard;
import entity.User;
import entity.UserCategory;
import entity.enums.CardType;
import entity.enums.Gender;
import entity.enums.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import repository.impl.UserRepositoryImpl;
import util.EntityManagerContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("UserRepositoryTest")
public class UserRepositoryTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private UserRepositoryImpl userRepository;

    @BeforeAll
    public static void setUpAll() {
        emf = Persistence.createEntityManagerFactory("rentawear_test");
    }

    @AfterAll
    public static void tearDownAll() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @BeforeEach
    public void setUp() {
        em = emf.createEntityManager();
        EntityManagerContext.set(em);
        em.getTransaction().begin();

        userRepository = new UserRepositoryImpl();
    }

    @AfterEach
    public void rollbackTransaction() {
        try {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
            EntityManagerContext.clear();
        }
    }

    // ==================== HELPER METHODS ====================

    private User createUser(String name, String email, String passwordHash, Gender gender) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setGender(gender);
        return user;
    }

    private User persistUser(String name, String email, String passwordHash, Gender gender) {
        User user = createUser(name, email, passwordHash, gender);
        em.persist(user);
        em.flush();
        return user;
    }

    // ==================== NESTED TEST CLASSES ====================

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        @DisplayName("should retrieve existing user by ID")
        void shouldRetrieveExistingUser() {
            User user = persistUser("John Doe", "john@example.com", "hashedPassword123", Gender.MALE);
            em.detach(user);

            User retrieved = userRepository.findById(user.getId());

            assertNotNull(retrieved);
            assertEquals(user.getId(), retrieved.getId());
            assertEquals("John Doe", retrieved.getName());
            assertEquals("john@example.com", retrieved.getEmail());
            assertEquals(Gender.MALE, retrieved.getGender());
        }

        @Test
        @DisplayName("should return null for non-existent user ID")
        void shouldReturnNullForNonExistentId() {
            User retrieved = userRepository.findById(9999);

            assertNull(retrieved);
        }
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        @DisplayName("should persist new user with auto-generated ID")
        void shouldPersistNewUser() {
            User user = createUser("Jane Smith", "jane@example.com", "hashedPassword456", Gender.FEMALE);

            User saved = userRepository.save(user);

            assertNotNull(saved.getId());
            assertEquals("Jane Smith", saved.getName());
            assertNotNull(saved.getCreatedAt());

            em.detach(saved);
            User retrieved = userRepository.findById(saved.getId());
            assertNotNull(retrieved);
            assertEquals("jane@example.com", retrieved.getEmail());
        }

        @Test
        @DisplayName("should update existing user properties")
        void shouldUpdateExistingUser() {
            User user = persistUser("John Doe", "john@example.com", "hashed123", Gender.MALE);
            Integer userId = user.getId();

            user.setName("John Updated");
            user.setAddress("123 Main Street");
            user.setJob("Software Engineer");
            userRepository.save(user);

            em.detach(user);
            User retrieved = userRepository.findById(userId);
            assertEquals("John Updated", retrieved.getName());
            assertEquals("123 Main Street", retrieved.getAddress());
            assertEquals("Software Engineer", retrieved.getJob());
        }

        @Test
        @DisplayName("should set default role to USER on creation")
        void shouldSetDefaultRole() {
            User user = createUser("Test User", "test@example.com", "hashed", Gender.MALE);

            User saved = userRepository.save(user);

            assertEquals(UserRole.USER, saved.getRole());
        }

        @Test
        @DisplayName("should allow custom role assignment")
        void shouldAllowCustomRole() {
            User user = createUser("Admin User", "admin@example.com", "hashed", Gender.FEMALE);
            user.setRole(UserRole.ADMIN);

            User saved = userRepository.save(user);

            assertEquals(UserRole.ADMIN, saved.getRole());
        }
    }

    @Nested
    @DisplayName("FindAll")
    class FindAll {

        @Test
        @DisplayName("should return all persisted users")
        void shouldReturnAllUsers() {
            persistUser("User 1", "user1@example.com", "hash1", Gender.MALE);
            persistUser("User 2", "user2@example.com", "hash2", Gender.FEMALE);
            persistUser("User 3", "user3@example.com", "hash3", Gender.MALE);

            List<User> users = userRepository.findAll();

            assertEquals(3, users.size());
        }

        @Test
        @DisplayName("should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsers() {
            List<User> users = userRepository.findAll();

            assertTrue(users.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        @DisplayName("should delete user from database")
        void shouldDeleteUser() {
            User user = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);
            Integer userId = user.getId();

            userRepository.delete(user);
            em.flush();

            User retrieved = userRepository.findById(userId);
            assertNull(retrieved);
        }

        @Test
        @DisplayName("should cascade delete user interests")
        void shouldCascadeDeleteInterests() {
            User user = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            Category category = new Category();
            category.setName("Test Category");
            category.setGender(Gender.MALE);
            em.persist(category);

            user.addInterest(category);
            em.flush();

            assertEquals(1, user.getInterests().size());

            userRepository.delete(user);
            em.flush();

            User retrieved = userRepository.findById(user.getId());
            assertNull(retrieved);
        }

        @Test
        @DisplayName("should cascade delete payment cards")
        void shouldCascadeDeletePaymentCards() {
            User user = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            PaymentCard card = new PaymentCard();
            card.setUser(user);
            card.setCardNumber("1234567890123456");
            card.setCardholderName("John Doe");
            card.setCardType(CardType.VISA);
            card.setCvv("123");
            card.setExpiryMonth("12");
            card.setExpiryYear("2025");
            user.addPaymentCard(card);
            em.persist(card);
            em.flush();

            assertEquals(1, user.getPaymentCards().size());

            userRepository.delete(user);
            em.flush();

            User retrieved = userRepository.findById(user.getId());
            assertNull(retrieved);
        }
    }

    @Nested
    @DisplayName("FindByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should find user by email")
        void shouldFindUserByEmail() {
            persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            User retrieved = userRepository.findByEmail("john@example.com");

            assertNotNull(retrieved);
            assertEquals("John Doe", retrieved.getName());
            assertEquals("john@example.com", retrieved.getEmail());
        }

        @Test
        @DisplayName("should return null for non-existent email")
        void shouldReturnNullForNonExistentEmail() {
            persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            User retrieved = userRepository.findByEmail("nonexistent@example.com");

            assertNull(retrieved);
        }

        @Test
        @DisplayName("should find correct user when multiple users exist")
        void shouldFindCorrectUserWhenMultipleExist() {
            persistUser("User 1", "user1@example.com", "hash1", Gender.MALE);
            persistUser("User 2", "user2@example.com", "hash2", Gender.FEMALE);
            persistUser("User 3", "user3@example.com", "hash3", Gender.MALE);

            User retrieved = userRepository.findByEmail("user2@example.com");

            assertNotNull(retrieved);
            assertEquals("User 2", retrieved.getName());
            assertEquals("user2@example.com", retrieved.getEmail());
        }

        @Test
        @DisplayName("should be case-sensitive for email lookup")
        void shouldBeCaseSensitiveForEmail() {
            persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            User retrieved = userRepository.findByEmail("JOHN@EXAMPLE.COM");

            assertNull(retrieved);
        }
    }

    @Nested
    @DisplayName("Email Uniqueness")
    class EmailUniqueness {

        @Test
        @DisplayName("should prevent duplicate email insertion via database constraint")
        void shouldPreventDuplicateEmail() {
            // ARRANGE
            persistUser("User 1", "duplicate@example.com", "hash1", Gender.MALE);

            User duplicateUser = createUser(
                    "User 2",
                    "duplicate@example.com",
                    "hash2",
                    Gender.FEMALE
            );

            // ACT & ASSERT
            assertThrows(PersistenceException.class, () -> {
                em.persist(duplicateUser);
                em.flush();
            });
        }
    }

    @Nested
    @DisplayName("Profile Information")
    class ProfileInformation {

        @Test
        @DisplayName("should store and retrieve complete user profile")
        void shouldStoreCompleteProfile() {
            User user = createUser("Jane Smith", "jane@example.com", "hashed", Gender.FEMALE);
            user.setBirthday(LocalDate.of(1990, 5, 15));
            user.setJob("Product Manager");
            user.setAddress("456 Oak Avenue, Springfield");
            user.setCreditLimit(new BigDecimal("5000.00"));

            User saved = userRepository.save(user);
            Integer userId = saved.getId();

            em.detach(saved);
            User retrieved = userRepository.findById(userId);

            assertEquals("Jane Smith", retrieved.getName());
            assertEquals(LocalDate.of(1990, 5, 15), retrieved.getBirthday());
            assertEquals("Product Manager", retrieved.getJob());
            assertEquals("456 Oak Avenue, Springfield", retrieved.getAddress());
            assertEquals(new BigDecimal("5000.00"), retrieved.getCreditLimit());
        }

        @Test
        @DisplayName("should allow NULL optional profile fields")
        void shouldAllowNullOptionalFields() {
            User user = createUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            User saved = userRepository.save(user);

            assertNull(saved.getBirthday());
            assertNull(saved.getJob());
            assertNull(saved.getAddress());
        }

        @Test
        @DisplayName("should default credit limit to 0.00")
        void shouldDefaultCreditLimitToZero() {
            User user = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            assertEquals(new BigDecimal("0.00"), user.getCreditLimit());
        }
    }

    @Nested
    @DisplayName("Lazy Loading")
    class LazyLoading {

        @Test
        @DisplayName("should lazy load user interests collection")
        void shouldLazyLoadInterests() {
            User user = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            Category category = new Category();
            category.setName("Men's Fashion");
            category.setGender(Gender.MALE);
            em.persist(category);

            user.addInterest(category);
            em.flush();
            em.detach(user);

            User retrieved = userRepository.findById(user.getId());
            assertNotNull(retrieved);

            // Access lazy collection
            int interestCount = retrieved.getInterests().size();
            assertEquals(1, interestCount);
        }

        @Test
        @DisplayName("should lazy load payment cards collection")
        void shouldLazyLoadPaymentCards() {
            User user = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            PaymentCard card = new PaymentCard();
            card.setUser(user);
            card.setCardNumber("378282246310005");
            card.setCardholderName("John Doe");
            card.setCardType(CardType.VISA);
            card.setCvv("1234");
            card.setExpiryMonth("08");
            card.setExpiryYear("2026");
            user.addPaymentCard(card);
            em.persist(card);
            em.flush();
            em.detach(user);

            User retrieved = userRepository.findById(user.getId());

            int cardCount = retrieved.getPaymentCards().size();
            assertEquals(1, cardCount);
        }

        @Test
        @DisplayName("should lazy load cart association")
        void shouldLazyLoadCart() {
            User user = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            Cart cart = new Cart();
            user.setCart(cart);
            em.persist(cart);
            em.flush();
            em.detach(user);

            User retrieved = userRepository.findById(user.getId());

            assertNotNull(retrieved.getCart());
        }
    }

    @Nested
    @DisplayName("Relationships")
    class Relationships {

        @Test
        @DisplayName("should manage user-category interests bidirectionally")
        void shouldManageInterestsBidirectionally() {
            User user = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            Category category = new Category();
            category.setName("Men's Tops");
            category.setGender(Gender.MALE);
            em.persist(category);

            user.addInterest(category);
            em.flush();

            assertEquals(1, user.getInterests().size());
            assertTrue(user.getInterests().stream().anyMatch(uc -> uc.getCategory().getId().equals(category.getId())));
        }

        @Test
        @DisplayName("should manage multiple interests for single user")
        void shouldManageMultipleInterests() {
            User user = persistUser("Jane Smith", "jane@example.com", "hashed", Gender.FEMALE);

            Category category1 = new Category();
            category1.setName("Women's Dresses");
            category1.setGender(Gender.FEMALE);
            em.persist(category1);

            Category category2 = new Category();
            category2.setName("Women's Shoes");
            category2.setGender(Gender.FEMALE);
            em.persist(category2);

            user.addInterest(category1);
            user.addInterest(category2);
            em.flush();

            assertEquals(2, user.getInterests().size());
        }

        @Test
        @DisplayName("should manage payment cards bidirectionally")
        void shouldManagePaymentCardsBidirectionally() {
            User user = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            PaymentCard card = new PaymentCard();
            card.setUser(user);
            card.setCardNumber("5555555555554444");
            card.setCardholderName("John Doe");
            card.setCardType(CardType.MASTERCARD);
            card.setCvv("567");
            card.setExpiryMonth("11");
            card.setExpiryYear("2027");
            user.addPaymentCard(card);
            em.persist(card);
            em.flush();

            assertEquals(1, user.getPaymentCards().size());
            assertEquals(user.getId(), card.getUser().getId());
        }

        @Test
        @DisplayName("should manage single user cart")
        void shouldManageSingleUserCart() {
            User user = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);

            Cart cart = new Cart();
            user.setCart(cart);
            em.persist(cart);
            em.flush();

            assertEquals(user.getId(), user.getCart().getUser().getId());
        }
    }

    @Nested
    @DisplayName("Gender Field")
    class GenderField {

        @Test
        @DisplayName("should store and retrieve gender correctly")
        void shouldStoreGenderCorrectly() {
            User maleUser = persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);
            User femaleUser = persistUser("Jane Smith", "jane@example.com", "hashed", Gender.FEMALE);

            em.detach(maleUser);
            em.detach(femaleUser);

            User retrievedMale = userRepository.findById(maleUser.getId());
            User retrievedFemale = userRepository.findById(femaleUser.getId());

            assertEquals(Gender.MALE, retrievedMale.getGender());
            assertEquals(Gender.FEMALE, retrievedFemale.getGender());
        }
    }

    @Nested
    @DisplayName("Integration")
    class Integration {

        @Test
        @DisplayName("should manage complete user lifecycle")
        void shouldManageCompleteLifecycle() {
            // Create
            User user = createUser("John Doe", "john@example.com", "hashed123", Gender.MALE);
            user.setJob("Software Engineer");
            user.setCreditLimit(new BigDecimal("3000.00"));

            User saved = userRepository.save(user);
            Integer userId = saved.getId();
            assertNotNull(saved.getCreatedAt());

            // Read
            User retrieved = userRepository.findById(userId);
            assertEquals("john@example.com", retrieved.getEmail());

            // Update
            retrieved.setAddress("789 Pine Road");
            retrieved.setCreditLimit(new BigDecimal("5000.00"));
            userRepository.save(retrieved);
            em.flush();

            User updated = userRepository.findById(userId);
            assertEquals("789 Pine Road", updated.getAddress());
            assertEquals(new BigDecimal("5000.00"), updated.getCreditLimit());

            // Delete
            userRepository.delete(updated);
            em.flush();

            User deleted = userRepository.findById(userId);
            assertNull(deleted);
        }

        @Test
        @DisplayName("should maintain user data integrity across operations")
        void shouldMaintainDataIntegrity() {
            User user = persistUser("John Doe", "john@example.com", "hashedPassword", Gender.MALE);

            Category category = new Category();
            category.setName("Men's Fashion");
            category.setGender(Gender.MALE);
            em.persist(category);

            user.addInterest(category);

            PaymentCard card = new PaymentCard();
            card.setUser(user);
            card.setCardNumber("5555555555554444");
            card.setCardholderName("John Doe");
            card.setCardType(CardType.MASTERCARD);
            card.setCvv("890");
            card.setExpiryMonth("05");
            card.setExpiryYear("2028");
            user.addPaymentCard(card);
            em.persist(card);

            Cart cart = new Cart();
            user.setCart(cart);
            em.persist(cart);

            em.flush();
            em.detach(user);

            User retrieved = userRepository.findById(user.getId());

            assertEquals("John Doe", retrieved.getName());
            assertEquals(Gender.MALE, retrieved.getGender());
            assertEquals(1, retrieved.getInterests().size());
            assertEquals(1, retrieved.getPaymentCards().size());
            assertNotNull(retrieved.getCart());
        }

        @Test
        @DisplayName("should support finding users by email after persistence")
        void shouldSupportFindByEmailAfterPersistence() {
            persistUser("John Doe", "john@example.com", "hashed", Gender.MALE);
            persistUser("Jane Smith", "jane@example.com", "hashed", Gender.FEMALE);

            User retrieved = userRepository.findByEmail("jane@example.com");

            assertNotNull(retrieved);
            assertEquals("Jane Smith", retrieved.getName());
            assertEquals(Gender.FEMALE, retrieved.getGender());
        }

        @Test
        @DisplayName("should handle user with all optional fields populated")
        void shouldHandleCompleteUserProfile() {
            User user = createUser("Complete User", "complete@example.com", "hashed", Gender.MALE);
            user.setBirthday(LocalDate.of(1985, 3, 20));
            user.setJob("Data Scientist");
            user.setAddress("100 Tech Street, San Francisco");
            user.setCreditLimit(new BigDecimal("10000.00"));
            user.setRole(UserRole.ADMIN);

            User saved = userRepository.save(user);
            Integer userId = saved.getId();

            em.detach(saved);
            User retrieved = userRepository.findById(userId);

            assertEquals("Complete User", retrieved.getName());
            assertEquals(LocalDate.of(1985, 3, 20), retrieved.getBirthday());
            assertEquals("Data Scientist", retrieved.getJob());
            assertEquals("100 Tech Street, San Francisco", retrieved.getAddress());
            assertEquals(new BigDecimal("10000.00"), retrieved.getCreditLimit());
            assertEquals(UserRole.ADMIN, retrieved.getRole());
        }
    }
}
