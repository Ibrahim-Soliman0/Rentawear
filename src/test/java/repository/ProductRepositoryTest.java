package repository;

import dto.PriceRangeDTO;
import entity.Category;
import entity.Product;
import entity.ProductVariant;
import entity.enums.Gender;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import repository.impl.ProductRepositoryImpl;
import util.EntityManagerContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductRepositoryTest {

    private EntityManagerFactory emf;

    private EntityManager em;
    private ProductRepositoryImpl repository;

    // ── Shared category references — reused across tests ──────────────────────

    private Category menCategory;
    private Category womenCategory;

    // ═════════════════════════════════════════════════════════════════════════
    //  One-time setup / teardown for the whole class
    // ═════════════════════════════════════════════════════════════════════════

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

    // ═════════════════════════════════════════════════════════════════════════
    //  Per-test setup / teardown
    // ═════════════════════════════════════════════════════════════════════════

    @BeforeEach
    void setUpTransaction() {
        em = emf.createEntityManager();

        EntityManagerContext.set(em);

        em.getTransaction().begin();

        menCategory = new Category();
        menCategory.setName("Men's Tops");
        menCategory.setGender(Gender.MALE);
        em.persist(menCategory);

        womenCategory = new Category();
        womenCategory.setName("Women's Dresses");
        womenCategory.setGender(Gender.FEMALE);
        em.persist(womenCategory);


        em.flush();

        repository = new ProductRepositoryImpl();
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

    // ═════════════════════════════════════════════════════════════════════════
    //  Helpers — build test data
    // ═════════════════════════════════════════════════════════════════════════

    private Product persistProduct(String name, BigDecimal price, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setBasePrice(price);
        p.setCategory(category);
        p.setImageUrl("https://example.com/img.jpg");
        em.persist(p);
        em.flush();
        return p;
    }


    private ProductVariant addVariant(Product product, String color, String size, int quantity) {
        ProductVariant v = new ProductVariant();
        v.setColor(color);
        v.setSize(size);
        v.setQuantity(quantity);
        product.addProductVariant(v);
        em.persist(v);
        em.flush();
        return v;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findById() — inherited from BaseRepositoryImpl
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return product when it exists")
        void findById_existingId_returnsProduct() {
            Product saved = persistProduct("Blue Shirt", new BigDecimal("49.99"), menCategory);

            Product found = repository.findById(saved.getId());

            assertNotNull(found, "Should find the product by its generated id");
            assertEquals("Blue Shirt", found.getName());
        }

        @Test
        @DisplayName("should return null when product does not exist")
        void findById_unknownId_returnsNull() {
            Product found = repository.findById(99999);

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
        @DisplayName("should persist a new product and return it with an assigned id")
        void save_newProduct_returnsPersistedProduct() {
            Product p = new Product();
            p.setName("Red Dress");
            p.setBasePrice(new BigDecimal("89.99"));
            p.setCategory(womenCategory);
            p.setImageUrl("https://example.com/dress.jpg");

            Product saved = repository.save(p);

            assertNotNull(saved.getId(), "Id should be assigned after save");
            assertEquals("Red Dress", saved.getName());
        }

        @Test
        @DisplayName("should update an existing product's name")
        void save_existingProduct_updatesFields() {
            Product saved = persistProduct("Old Name", new BigDecimal("30.00"), menCategory);

            saved.setName("Updated Name");
            Product updated = repository.save(saved);

            assertEquals("Updated Name", updated.getName());

            // Also verify the DB reflects the change
            em.clear();   // evict from first-level cache so findById hits H2
            Product reloaded = repository.findById(saved.getId());
            assertEquals("Updated Name", reloaded.getName());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findAll() — inherited from BaseRepositoryImpl
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("should return all persisted products")
        void findAll_multipleProducts_returnsAll() {
            persistProduct("Shirt A", new BigDecimal("40.00"), menCategory);
            persistProduct("Shirt B", new BigDecimal("50.00"), menCategory);
            persistProduct("Dress C", new BigDecimal("70.00"), womenCategory);

            List<Product> all = repository.findAll();

            assertEquals(3, all.size(), "Should return all 3 persisted products");
        }

        @Test
        @DisplayName("should return empty list when no products exist")
        void findAll_noProducts_returnsEmptyList() {
            List<Product> all = repository.findAll();

            assertTrue(all.isEmpty(), "Should return empty list when table is empty");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  delete() — inherited from BaseRepositoryImpl
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should remove product from the database")
        void delete_existingProduct_removesIt() {
            Product saved = persistProduct("To Delete", new BigDecimal("20.00"), menCategory);
            Integer id = saved.getId();

            repository.delete(saved);
            em.flush();
            em.clear();

            assertNull(repository.findById(id), "Product should be gone after delete");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findFiltered() — gender + price range, no categories
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findFiltered()")
    class FindFiltered {

        @Test
        @DisplayName("should return only men's products when gender filter is MALE")
        void findFiltered_maleGender_returnsMensProductsOnly() {
            persistProduct("Men Shirt",   new BigDecimal("40.00"), menCategory);
            persistProduct("Women Dress", new BigDecimal("60.00"), womenCategory);

            List<Product> result = repository.findFiltered(
                    "MALE", null, null, null, 10, 0);

            assertEquals(1, result.size());
            assertEquals("Men Shirt", result.get(0).getName());
        }

        @Test
        @DisplayName("should return all products when gender filter is null")
        void findFiltered_nullGender_returnsAllProducts() {
            persistProduct("Men Shirt",   new BigDecimal("40.00"), menCategory);
            persistProduct("Women Dress", new BigDecimal("60.00"), womenCategory);

            List<Product> result = repository.findFiltered(
                    null, null, null, null, 10, 0);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should filter by min and max price range")
        void findFiltered_priceRange_returnsProductsInRange() {
            persistProduct("Cheap Shirt",     new BigDecimal("20.00"), menCategory);
            persistProduct("Mid Shirt",       new BigDecimal("50.00"), menCategory);
            persistProduct("Expensive Shirt", new BigDecimal("120.00"), menCategory);

            List<Product> result = repository.findFiltered(
                    null, null, 30.0, 80.0, 10, 0);

            assertEquals(1, result.size());
            assertEquals("Mid Shirt", result.get(0).getName());
        }

        @Test
        @DisplayName("should respect limit parameter")
        void findFiltered_limitApplied_returnsOnlyLimitedResults() {
            persistProduct("Shirt 1", new BigDecimal("40.00"), menCategory);
            persistProduct("Shirt 2", new BigDecimal("41.00"), menCategory);
            persistProduct("Shirt 3", new BigDecimal("42.00"), menCategory);

            List<Product> result = repository.findFiltered(
                    null, null, null, null, 2, 0);

            assertEquals(2, result.size(), "Limit of 2 should return only 2 products");
        }

        @Test
        @DisplayName("should filter by specific category ids")
        void findFiltered_withCategoryIds_returnsMatchingProducts() {
            persistProduct("Men Shirt",   new BigDecimal("40.00"), menCategory);
            persistProduct("Women Dress", new BigDecimal("60.00"), womenCategory);

            List<Product> result = repository.findFiltered(
                    null,
                    List.of(womenCategory.getId()),
                    null, null, 10, 0);

            assertEquals(1, result.size());
            assertEquals("Women Dress", result.get(0).getName());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  countFiltered()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countFiltered()")
    class CountFiltered {

        @Test
        @DisplayName("should return correct count for gender filter")
        void countFiltered_maleGender_countsCorrectly() {
            persistProduct("Men Shirt 1", new BigDecimal("40.00"), menCategory);
            persistProduct("Men Shirt 2", new BigDecimal("50.00"), menCategory);
            persistProduct("Women Dress", new BigDecimal("60.00"), womenCategory);

            long count = repository.countFiltered("MALE", null, null, null);

            assertEquals(2, count);
        }

        @Test
        @DisplayName("should return 0 when no products match the filter")
        void countFiltered_noMatches_returnsZero() {
            persistProduct("Men Shirt", new BigDecimal("40.00"), menCategory);

            // Filter for women — but only men's products exist
            long count = repository.countFiltered("FEMALE", null, null, null);

            assertEquals(0, count);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  searchFiltered()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("searchFiltered()")
    class SearchFiltered {

        @Test
        @DisplayName("should find products whose name contains the search term (case-insensitive)")
        void searchFiltered_matchingName_returnsResults() {
            persistProduct("Blue Denim Jacket", new BigDecimal("80.00"), menCategory);
            persistProduct("Red Silk Blouse",   new BigDecimal("70.00"), womenCategory);
            persistProduct("Blue Linen Shirt",  new BigDecimal("45.00"), menCategory);

            List<Product> result = repository.searchFiltered(
                    "blue", null, null, null, null, 10, 0);

            assertEquals(2, result.size(),
                    "Search for 'blue' should match both Blue products");
        }

        @Test
        @DisplayName("should return empty list when no product name matches")
        void searchFiltered_noMatch_returnsEmpty() {
            persistProduct("Red Shirt", new BigDecimal("40.00"), menCategory);

            List<Product> result = repository.searchFiltered(
                    "invisible", null, null, null, null, 10, 0);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should combine name search with gender filter correctly")
        void searchFiltered_nameAndGender_filtersCorrectly() {
            persistProduct("Blue Men Shirt",  new BigDecimal("40.00"), menCategory);
            persistProduct("Blue Women Top",  new BigDecimal("55.00"), womenCategory);

            List<Product> result = repository.searchFiltered(
                    "blue", "MALE", null, null, null, 10, 0);

            assertEquals(1, result.size());
            assertEquals("Blue Men Shirt", result.get(0).getName());
        }

        @Test
        @DisplayName("should combine search with category filter")
        void searchFiltered_nameAndCategoryIds_filtersCorrectly() {
            persistProduct("Blue Men Shirt",  new BigDecimal("40.00"), menCategory);
            persistProduct("Blue Women Top",  new BigDecimal("55.00"), womenCategory);

            List<Product> result = repository.searchFiltered(
                    "blue", null,
                    List.of(menCategory.getId()),
                    null, null, 10, 0);

            assertEquals(1, result.size());
            assertEquals("Blue Men Shirt", result.get(0).getName());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  countSearchFiltered()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countSearchFiltered()")
    class CountSearchFiltered {

        @Test
        @DisplayName("should count products matching the search term")
        void countSearchFiltered_matchingTerm_returnsCorrectCount() {
            persistProduct("Blue Shirt",  new BigDecimal("40.00"), menCategory);
            persistProduct("Blue Dress",  new BigDecimal("60.00"), womenCategory);
            persistProduct("Red Blouse",  new BigDecimal("50.00"), womenCategory);

            long count = repository.countSearchFiltered(
                    "blue", null, null, null, null);

            assertEquals(2, count);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findByInterests()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findByInterests()")
    class FindByInterests {

        @Test
        @DisplayName("should return products matching the given category ids")
        void findByInterests_matchingCategories_returnsProducts() {
            persistProduct("Men Shirt",   new BigDecimal("40.00"), menCategory);
            persistProduct("Women Dress", new BigDecimal("60.00"), womenCategory);

            List<Product> result = repository.findByInterests(
                    List.of(menCategory.getId()), null, 10, 0);

            assertEquals(1, result.size());
            assertEquals("Men Shirt", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when categoryIds is null")
        void findByInterests_nullCategoryIds_returnsEmpty() {
            persistProduct("Men Shirt", new BigDecimal("40.00"), menCategory);

            List<Product> result = repository.findByInterests(
                    null, null, 10, 0);

            assertTrue(result.isEmpty(),
                    "Should short-circuit and return empty list for null ids");
        }

        @Test
        @DisplayName("should return empty list when categoryIds is empty")
        void findByInterests_emptyCategoryIds_returnsEmpty() {
            persistProduct("Men Shirt", new BigDecimal("40.00"), menCategory);

            List<Product> result = repository.findByInterests(
                    List.of(), null, 10, 0);

            assertTrue(result.isEmpty());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  countByInterests()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countByInterests()")
    class CountByInterests {

        @Test
        @DisplayName("should return 0 immediately when categoryIds is null or empty")
        void countByInterests_nullOrEmptyIds_returnsZero() {
            persistProduct("Men Shirt", new BigDecimal("40.00"), menCategory);

            assertEquals(0, repository.countByInterests(null, null));
            assertEquals(0, repository.countByInterests(List.of(), null));
        }

        @Test
        @DisplayName("should count products in the given categories")
        void countByInterests_matchingCategories_countsCorrectly() {
            persistProduct("Men Shirt 1", new BigDecimal("40.00"), menCategory);
            persistProduct("Men Shirt 2", new BigDecimal("50.00"), menCategory);
            persistProduct("Women Dress", new BigDecimal("60.00"), womenCategory);

            long count = repository.countByInterests(
                    List.of(menCategory.getId()), null);

            assertEquals(2, count);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getMinMaxPrice()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getMinMaxPrice()")
    class GetMinMaxPrice {

        @Test
        @DisplayName("should return correct min and max across all products")
        void getMinMaxPrice_noFilter_returnsGlobalMinMax() {
            persistProduct("Cheap",     new BigDecimal("10.00"), menCategory);
            persistProduct("Mid",       new BigDecimal("50.00"), womenCategory);
            persistProduct("Expensive", new BigDecimal("200.00"), menCategory);

            PriceRangeDTO result = repository.getMinMaxPrice(null, null);

            assertNotNull(result);
            assertEquals(10.0,  result.min(), 0.01);
            assertEquals(200.0, result.max(), 0.01);
        }

        @Test
        @DisplayName("should return null min/max when no products exist")
        void getMinMaxPrice_noProducts_returnsNullRange() {
            PriceRangeDTO result = repository.getMinMaxPrice(null, null);

            assertNotNull(result);
            assertNull(result.min(), "min should be null when table is empty");
            assertNull(result.max(), "max should be null when table is empty");
        }

        @Test
        @DisplayName("should filter min/max by gender")
        void getMinMaxPrice_genderFilter_returnsGenderedRange() {
            persistProduct("Men Cheap",     new BigDecimal("20.00"), menCategory);
            persistProduct("Men Expensive", new BigDecimal("100.00"), menCategory);
            persistProduct("Women Dress",   new BigDecimal("300.00"), womenCategory);

            PriceRangeDTO result = repository.getMinMaxPrice("MALE", null);

            assertEquals(20.0,  result.min(), 0.01);
            assertEquals(100.0, result.max(), 0.01);
        }

        @Test
        @DisplayName("should filter min/max by category ids")
        void getMinMaxPrice_categoryFilter_returnsCategoryRange() {
            persistProduct("Men Shirt",   new BigDecimal("40.00"), menCategory);
            persistProduct("Women Dress", new BigDecimal("150.00"), womenCategory);

            PriceRangeDTO result = repository.getMinMaxPrice(
                    null, List.of(womenCategory.getId()));

            assertEquals(150.0, result.min(), 0.01);
            assertEquals(150.0, result.max(), 0.01);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findNew() — new arrivals within N days
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findNew()")
    class FindNew {

        /*
         * @CreationTimestamp sets createdAt automatically on flush.
         * Products persisted in this test will always have createdAt = now,
         * so querying for products created within the last 1 day will find them.
         *
         * We can't easily test "old" products without manipulating createdAt,
         * which @CreationTimestamp doesn't allow by default.
         * The safe tests here are: products created "today" are returned,
         * and empty category ids short-circuits to the no-category path.
         */

        @Test
        @DisplayName("should return recently created products (created within last 1 day)")
        void findNew_recentProducts_returnsResults() {
            persistProduct("New Shirt", new BigDecimal("40.00"), menCategory);
            persistProduct("New Dress", new BigDecimal("60.00"), womenCategory);

            List<Product> result = repository.findNew(10, 1, null, null);

            assertEquals(2, result.size(),
                    "Both products are brand new, should be within 1 day cutoff");
        }

        @Test
        @DisplayName("should filter new arrivals by gender")
        void findNew_withGenderFilter_returnsFilteredResults() {
            persistProduct("New Men Shirt",  new BigDecimal("40.00"), menCategory);
            persistProduct("New Women Dress",new BigDecimal("60.00"), womenCategory);

            List<Product> result = repository.findNew(10, 1, "FEMALE", null);

            assertEquals(1, result.size());
            assertEquals("New Women Dress", result.get(0).getName());
        }

        @Test
        @DisplayName("should filter new arrivals by category ids")
        void findNew_withCategoryIds_returnsFilteredResults() {
            persistProduct("New Men Shirt",   new BigDecimal("40.00"), menCategory);
            persistProduct("New Women Dress", new BigDecimal("60.00"), womenCategory);

            List<Product> result = repository.findNew(
                    10, 1, null, List.of(menCategory.getId()));

            assertEquals(1, result.size());
            assertEquals("New Men Shirt", result.get(0).getName());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  countNew()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countNew()")
    class CountNew {

        @Test
        @DisplayName("should count recently created products")
        void countNew_recentProducts_returnsCorrectCount() {
            persistProduct("New Shirt", new BigDecimal("40.00"), menCategory);
            persistProduct("New Dress", new BigDecimal("60.00"), womenCategory);

            long count = repository.countNew(1, null, null);

            assertEquals(2, count);
        }

        @Test
        @DisplayName("should count zero when no products exist")
        void countNew_noProducts_returnsZero() {
            long count = repository.countNew(1, null, null);

            assertEquals(0, count);
        }
    }
}