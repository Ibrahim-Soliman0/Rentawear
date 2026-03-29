package repository;

import entity.Category;
import entity.enums.Gender;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import repository.impl.CategoryRepositoryImpl;
import util.EntityManagerContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * ─────────────────────────────────────────────────────────────────────────────
 *  CategoryRepository Test Suite
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  Tests for CategoryRepositoryImpl covering:
 *    - Inherited methods from BaseRepositoryImpl (findById, save, findAll, delete)
 *    - Custom method: findByGender(Gender gender)
 *
 *  Uses JPA EntityManager for persistence and transactional rollback after
 *  each test to maintain database isolation.
 * ─────────────────────────────────────────────────────────────────────────────
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CategoryRepositoryTest {

    private EntityManagerFactory emf;
    private EntityManager em;
    private CategoryRepositoryImpl repository;

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

        repository = new CategoryRepositoryImpl();
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

    private Category persistCategory(String name, Gender gender, String description) {
        Category c = new Category();
        c.setName(name);
        c.setGender(gender);
        c.setDescription(description);
        em.persist(c);
        em.flush();
        return c;
    }

    private Category persistCategory(String name, Gender gender) {
        return persistCategory(name, gender, "Test description for " + name);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findById() — inherited from BaseRepositoryImpl
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return category when it exists")
        void findById_existingId_returnsCategory() {
            // ARRANGE
            Category saved = persistCategory("Men's Fashion", Gender.MALE);

            // ACT
            Category found = repository.findById(saved.getId());

            // ASSERT
            assertNotNull(found, "Should find the category by its generated id");
            assertEquals("Men's Fashion", found.getName());
            assertEquals(Gender.MALE, found.getGender());
        }

        @Test
        @DisplayName("should return null when category does not exist")
        void findById_unknownId_returnsNull() {
            // ACT
            Category found = repository.findById(99999);

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
        @DisplayName("should persist a new category and return it with an assigned id")
        void save_newCategory_returnsPersistedCategory() {
            // ARRANGE
            Category c = new Category();
            c.setName("Women's Accessories");
            c.setGender(Gender.FEMALE);
            c.setDescription("Belts, hats, and other accessories");

            // ACT
            Category saved = repository.save(c);

            // ASSERT
            assertNotNull(saved.getId(), "Saved category should have an assigned id");
            assertEquals("Women's Accessories", saved.getName());
            assertEquals(Gender.FEMALE, saved.getGender());
        }

        @Test
        @DisplayName("should update an existing category's properties")
        void save_existingCategory_updatesFields() {
            // ARRANGE
            Category c = persistCategory("Original Name", Gender.MALE);
            Integer originalId = c.getId();

            // ACT
            c.setName("Updated Name");
            c.setDescription("Updated description");
            Category updated = repository.save(c);

            // ASSERT
            assertEquals(originalId, updated.getId(), "Id should remain unchanged");
            assertEquals("Updated Name", updated.getName());
            assertEquals("Updated description", updated.getDescription());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findAll() — inherited from BaseRepositoryImpl
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("should return all persisted categories")
        void findAll_multipleCategories_returnsAll() {
            // ARRANGE
            persistCategory("Men's Clothing", Gender.MALE);
            persistCategory("Women's Clothing", Gender.FEMALE);
            persistCategory("Unisex Items", Gender.MALE);  // Or appropriate gender

            // ACT
            List<Category> all = repository.findAll();

            // ASSERT
            assertEquals(3, all.size(), "Should return all 3 persisted categories");
        }

        @Test
        @DisplayName("should return empty list when no categories exist")
        void findAll_noCategories_returnsEmptyList() {
            // ACT
            List<Category> all = repository.findAll();

            // ASSERT
            assertTrue(all.isEmpty(), "Should return empty list when no categories exist");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  delete() — inherited from BaseRepositoryImpl
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should remove category from the database")
        void delete_existingCategory_removesIt() {
            // ARRANGE
            Category c = persistCategory("To Be Deleted", Gender.MALE);
            Integer categoryId = c.getId();

            // ACT
            repository.delete(c);
            Category found = repository.findById(categoryId);

            // ASSERT
            assertNull(found, "Category should be deleted from the database");
        }

        @Test
        @DisplayName("should handle deletion of already-deleted category gracefully")
        void delete_nonexistentCategory_handlesGracefully() {
            // ARRANGE
            Category c = new Category();
            c.setId(99999);
            c.setName("Non-existent");
            c.setGender(Gender.MALE);

            // ACT & ASSERT - should not throw exception
            assertDoesNotThrow(() -> repository.delete(c));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findByGender() — custom method
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findByGender()")
    class FindByGender {

        @Test
        @DisplayName("should return only categories with MALE gender")
        void findByGender_maleGender_returnsMaleCategories() {
            // ARRANGE
            persistCategory("Men's Tops", Gender.MALE);
            persistCategory("Men's Bottoms", Gender.MALE);
            persistCategory("Women's Dresses", Gender.FEMALE);

            // ACT
            List<Category> maleCategories = repository.findByGender(Gender.MALE);

            // ASSERT
            assertEquals(2, maleCategories.size(), "Should return 2 male categories");
            assertTrue(maleCategories.stream()
                    .allMatch(c -> c.getGender() == Gender.MALE),
                    "All returned categories should have MALE gender");
        }

        @Test
        @DisplayName("should return only categories with FEMALE gender")
        void findByGender_femaleGender_returnsFemaleCategories() {
            // ARRANGE
            persistCategory("Men's Tops", Gender.MALE);
            persistCategory("Women's Dresses", Gender.FEMALE);
            persistCategory("Women's Shoes", Gender.FEMALE);

            // ACT
            List<Category> femaleCategories = repository.findByGender(Gender.FEMALE);

            // ASSERT
            assertEquals(2, femaleCategories.size(), "Should return 2 female categories");
            assertTrue(femaleCategories.stream()
                    .allMatch(c -> c.getGender() == Gender.FEMALE),
                    "All returned categories should have FEMALE gender");
        }

        @Test
        @DisplayName("should return categories ordered by name ascending")
        void findByGender_multipleCategoriesSameGender_returnsOrderedByName() {
            // ARRANGE
            persistCategory("Zebra Clothing", Gender.MALE);
            persistCategory("Apple Clothing", Gender.MALE);
            persistCategory("Mango Clothing", Gender.MALE);

            // ACT
            List<Category> maleCategories = repository.findByGender(Gender.MALE);

            // ASSERT
            assertEquals(3, maleCategories.size());
            assertEquals("Apple Clothing", maleCategories.get(0).getName());
            assertEquals("Mango Clothing", maleCategories.get(1).getName());
            assertEquals("Zebra Clothing", maleCategories.get(2).getName());
        }

        @Test
        @DisplayName("should return empty list when no categories exist for gender")
        void findByGender_noCategoriesForGender_returnsEmpty() {
            // ARRANGE
            persistCategory("Men's Tops", Gender.MALE);

            // ACT
            List<Category> femaleCategories = repository.findByGender(Gender.FEMALE);

            // ASSERT
            assertTrue(femaleCategories.isEmpty(),
                    "Should return empty list when no categories match gender");
        }

        @Test
        @DisplayName("should return empty list when no categories exist at all")
        void findByGender_noCategories_returnsEmpty() {
            // ACT
            List<Category> maleCategories = repository.findByGender(Gender.MALE);

            // ASSERT
            assertTrue(maleCategories.isEmpty(), "Should return empty list when database is empty");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Integration — multiple operations
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration")
    class Integration {

        @Test
        @DisplayName("should handle save and retrieve cycle correctly")
        void integration_saveAndRetrieve_worksCorrectly() {
            // ARRANGE
            Category c = new Category();
            c.setName("Test Category");
            c.setGender(Gender.MALE);
            c.setDescription("Integration test category");

            // ACT
            Category saved = repository.save(c);
            Category retrieved = repository.findById(saved.getId());

            // ASSERT
            assertNotNull(retrieved);
            assertEquals("Test Category", retrieved.getName());
            assertEquals(Gender.MALE, retrieved.getGender());
            assertEquals("Integration test category", retrieved.getDescription());
        }

        @Test
        @DisplayName("should handle multiple saves and deletes")
        void integration_multipleOperations_worksCorrectly() {
            // ARRANGE
            Category cat1 = persistCategory("Category 1", Gender.MALE);
            Category cat2 = persistCategory("Category 2", Gender.FEMALE);
            Category cat3 = persistCategory("Category 3", Gender.MALE);

            // ACT - delete one
            repository.delete(cat2);
            List<Category> all = repository.findAll();
            List<Category> maleOnly = repository.findByGender(Gender.MALE);

            // ASSERT
            assertEquals(2, all.size(), "Should have 2 categories after deletion");
            assertEquals(2, maleOnly.size(), "Should have 2 male categories");
            assertNull(repository.findById(cat2.getId()), "Deleted category should not exist");
        }
    }
}
