package service;

import dto.PriceRangeDTO;
import dto.ProductFilterDTO;
import entity.Category;
import entity.Product;
import entity.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ProductRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    // ── Mocks (fake objects we control) ──────────────────────────────────────

    @Mock
    private ProductRepository productRepository;     // fake DB — no real database used

    // ── The real object under test ────────────────────────────────────────────

    @InjectMocks
    private ProductService productService;           // real ProductService, with mock injected

    // ── Shared test data ──────────────────────────────────────────────────────

    private Product tshirtProduct;
    private Product jeansProduct;
    private Category kidsCategory;
    private Category menCategory;

    @BeforeEach
    void setUp() {
        /*
         * Build two reusable Product objects that most tests can work with.
         * We reset these before every test so one test can't
         * accidentally affect another.
         */

        // Category setup
        kidsCategory = new Category();
        kidsCategory.setId(1);
        kidsCategory.setName("Kids");
        kidsCategory.setGender(Gender.FEMALE);

        menCategory = new Category();
        menCategory.setId(2);
        menCategory.setName("Men");
        menCategory.setGender(Gender.MALE);

        // Product 1: T-Shirt (new arrival, lower price)
        tshirtProduct = new Product();
        tshirtProduct.setId(1);
        tshirtProduct.setName("Basic T-Shirt");
        tshirtProduct.setDescription("Comfortable cotton tshirt");
        tshirtProduct.setBasePrice(BigDecimal.valueOf(149.99));
        tshirtProduct.setCategory(menCategory);
        tshirtProduct.setImageUrl("tshirt.jpg");
        tshirtProduct.setCreatedAt(Instant.now());  // counts as "new"

        // Product 2: Jeans (older, higher price)
        jeansProduct = new Product();
        jeansProduct.setId(2);
        jeansProduct.setName("Classic Jeans");
        jeansProduct.setDescription("Durable denim jeans");
        jeansProduct.setBasePrice(BigDecimal.valueOf(499.99));
        jeansProduct.setCategory(menCategory);
        jeansProduct.setImageUrl("jeans.jpg");
        jeansProduct.setCreatedAt(Instant.now().minusSeconds(31 * 24 * 60 * 60));  // over 30 days old
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getById()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("should return product when id exists")
        void getById_validId_returnsProduct() {
            // ARRANGE
            when(productRepository.findById(1)).thenReturn(tshirtProduct);

            // ACT
            Product result = productService.getById(1);

            // ASSERT
            assertNotNull(result, "Should return non-null product");
            assertEquals("Basic T-Shirt", result.getName(), "Product name should match");
            verify(productRepository, times(1)).findById(1);
        }

        @Test
        @DisplayName("should return null when id does not exist")
        void getById_invalidId_returnsNull() {
            // ARRANGE
            when(productRepository.findById(999)).thenReturn(null);

            // ACT
            Product result = productService.getById(999);

            // ASSERT
            assertNull(result, "Should return null for non-existent product");
            verify(productRepository, times(1)).findById(999);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findNew()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findNew()")
    class FindNew {

        private ProductFilterDTO buildFilterDto(String gender, List<Integer> categoryIds,
                                                 int pageSize) {
            return new ProductFilterDTO(
                    null,                  // searchQuery — null means browse mode
                    gender,                // gender filter
                    categoryIds,           // category filter
                    null,                  // minPrice — not used for new products
                    null,                  // maxPrice — not used for new products
                    true,                  // newOnly — triggers findNew path
                    null,                  // interestIds — not used here
                    0,                     // page
                    pageSize               // pageSize
            );
        }

        @Test
        @DisplayName("should find new products with gender and category filters")
        void findNew_withFilters_returnsProducts() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("MALE", List.of(2), 10);
            when(productRepository.findNew(10, 30, "MALE", List.of(2)))
                    .thenReturn(List.of(tshirtProduct));

            // ACT
            List<Product> result = productService.findNew(filter);

            // ASSERT
            assertNotNull(result, "Should return non-null list");
            assertEquals(1, result.size(), "Should return 1 product");
            assertTrue(result.contains(tshirtProduct), "Should contain the tshirt product");
            verify(productRepository, times(1)).findNew(10, 30, "MALE", List.of(2));
        }

        @Test
        @DisplayName("should return empty list when no new products match filters")
        void findNew_noMatches_returnsEmptyList() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("FEMALE", List.of(3), 10);
            when(productRepository.findNew(10, 30, "FEMALE", List.of(3)))
                    .thenReturn(List.of());

            // ACT
            List<Product> result = productService.findNew(filter);

            // ASSERT
            assertNotNull(result, "Should return empty list, not null");
            assertTrue(result.isEmpty(), "Should return empty list");
        }

        @Test
        @DisplayName("should call repository with correct NEW_DAYS constant (30)")
        void findNew_usesCorrectNewDaysConstant() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("MALE", null, 20);
            when(productRepository.findNew(20, 30, "MALE", null))
                    .thenReturn(List.of(tshirtProduct));

            // ACT
            productService.findNew(filter);

            // ASSERT — verify NEW_DAYS=30 was passed (this is the constant in the service)
            verify(productRepository, times(1)).findNew(20, 30, "MALE", null);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findFiltered()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findFiltered()")
    class FindFiltered {

        private ProductFilterDTO buildFilterDto(String gender, List<Integer> categoryIds,
                                                 Double minPrice, Double maxPrice,
                                                 int pageSize, int page) {
            return new ProductFilterDTO(
                    null,                  // searchQuery
                    gender,
                    categoryIds,
                    minPrice,
                    maxPrice,
                    false,                 // newOnly
                    null,                  // interestIds
                    page,
                    pageSize
            );
        }

        @Test
        @DisplayName("should find products with price range filter")
        void findFiltered_withPriceRange_returnsProducts() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("MALE", List.of(2),
                    100.0, 500.0, 10, 0);
            when(productRepository.findFiltered("MALE", List.of(2), 100.0, 500.0, 10, 0))
                    .thenReturn(List.of(tshirtProduct));

            // ACT
            List<Product> result = productService.findFiltered(filter);

            // ASSERT
            assertEquals(1, result.size());
            verify(productRepository, times(1))
                    .findFiltered("MALE", List.of(2), 100.0, 500.0, 10, 0);
        }

        @Test
        @DisplayName("should handle pagination offset calculation correctly")
        void findFiltered_withPagination_calculatesOffsetCorrectly() {
            // ARRANGE — page 2, pageSize 10 should give offset 20
            ProductFilterDTO filter = buildFilterDto(null, null, null, null, 10, 2);
            when(productRepository.findFiltered(null, null, null, null, 10, 20))
                    .thenReturn(List.of(jeansProduct));

            // ACT
            productService.findFiltered(filter);

            // ASSERT — offset should be 2 * 10 = 20
            verify(productRepository, times(1))
                    .findFiltered(null, null, null, null, 10, 20);
        }

        @Test
        @DisplayName("should pass null filters when not specified")
        void findFiltered_withNullFilters_passesNullToRepository() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto(null, null, null, null, 5, 0);
            when(productRepository.findFiltered(null, null, null, null, 5, 0))
                    .thenReturn(List.of());

            // ACT
            productService.findFiltered(filter);

            // ASSERT
            verify(productRepository, times(1))
                    .findFiltered(null, null, null, null, 5, 0);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findByInterests()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findByInterests()")
    class FindByInterests {

        private ProductFilterDTO buildFilterDto(List<Integer> interestIds,
                                                 Double minPrice, Double maxPrice,
                                                 int pageSize) {
            return new ProductFilterDTO(
                    null,                  // searchQuery
                    null,                  // gender — always null for interests (cross-gender)
                    null,                  // categoryIds — will be overridden with interestIds
                    minPrice,
                    maxPrice,
                    false,                 // newOnly
                    interestIds,           // interests
                    0,                     // page
                    pageSize
            );
        }

        @Test
        @DisplayName("should find products by interests with cross-gender filtering")
        void findByInterests_withInterests_returnsProducts() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto(List.of(1, 2), 100.0, 500.0, 10);
            when(productRepository.findFiltered(null, List.of(1, 2), 100.0, 500.0, 10, 0))
                    .thenReturn(List.of(tshirtProduct, jeansProduct));

            // ACT
            List<Product> result = productService.findByInterests(filter);

            // ASSERT — gender should always be null for interests
            assertEquals(2, result.size());
            verify(productRepository, times(1))
                    .findFiltered(null, List.of(1, 2), 100.0, 500.0, 10, 0);
        }

        @Test
        @DisplayName("should pass null gender to repository (cross-gender interests)")
        void findByInterests_alwaysNullGender_passesNullToRepository() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto(List.of(5), null, null, 20);
            when(productRepository.findFiltered(null, List.of(5), null, null, 20, 0))
                    .thenReturn(List.of());

            // ACT
            productService.findByInterests(filter);

            // ASSERT — first parameter (gender) must be null
            verify(productRepository, times(1))
                    .findFiltered(null, List.of(5), null, null, 20, 0);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  searchFiltered()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("searchFiltered()")
    class SearchFiltered {

        private ProductFilterDTO buildFilterDto(String query, String gender,
                                                 List<Integer> categoryIds,
                                                 Double minPrice, Double maxPrice,
                                                 int pageSize) {
            return new ProductFilterDTO(
                    query,                 // searchQuery — must not be null
                    gender,
                    categoryIds,
                    minPrice,
                    maxPrice,
                    false,                 // newOnly
                    null,                  // interestIds
                    0,                     // page
                    pageSize
            );
        }

        @Test
        @DisplayName("should search products with query and filters")
        void searchFiltered_withQuery_returnsSearchResults() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("shirt", "MALE", List.of(2),
                    100.0, 500.0, 10);
            when(productRepository.searchFiltered("shirt", "MALE", List.of(2),
                    100.0, 500.0, 10, 0))
                    .thenReturn(List.of(tshirtProduct));

            // ACT
            List<Product> result = productService.searchFiltered(filter);

            // ASSERT
            assertEquals(1, result.size());
            verify(productRepository, times(1))
                    .searchFiltered("shirt", "MALE", List.of(2), 100.0, 500.0, 10, 0);
        }

        @Test
        @DisplayName("should return empty list when search matches nothing")
        void searchFiltered_noMatches_returnsEmptyList() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("unicorn", null, null,
                    null, null, 10);
            when(productRepository.searchFiltered("unicorn", null, null,
                    null, null, 10, 0))
                    .thenReturn(List.of());

            // ACT
            List<Product> result = productService.searchFiltered(filter);

            // ASSERT
            assertTrue(result.isEmpty());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  countByInterests()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countByInterests()")
    class CountByInterests {

        private ProductFilterDTO buildFilterDto(List<Integer> interestIds,
                                                 Double minPrice, Double maxPrice) {
            return new ProductFilterDTO(
                    null,                  // searchQuery
                    null,                  // gender — always null for interests
                    null,                  // categoryIds
                    minPrice,
                    maxPrice,
                    false,                 // newOnly
                    interestIds,           // interests
                    0,                     // page
                    10                     // pageSize
            );
        }

        @Test
        @DisplayName("should count products by interests")
        void countByInterests_withInterests_returnsCount() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto(List.of(1, 2, 3), 100.0, 500.0);
            when(productRepository.countFiltered(null, List.of(1, 2, 3), 100.0, 500.0))
                    .thenReturn(42L);

            // ACT
            long result = productService.countByInterests(filter);

            // ASSERT
            assertEquals(42L, result);
            verify(productRepository, times(1))
                    .countFiltered(null, List.of(1, 2, 3), 100.0, 500.0);
        }

        @Test
        @DisplayName("should return zero when no products match interests")
        void countByInterests_noMatches_returnsZero() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto(List.of(999), null, null);
            when(productRepository.countFiltered(null, List.of(999), null, null))
                    .thenReturn(0L);

            // ACT
            long result = productService.countByInterests(filter);

            // ASSERT
            assertEquals(0L, result);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  countNew()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countNew()")
    class CountNew {

        private ProductFilterDTO buildFilterDto(String gender, List<Integer> categoryIds) {
            return new ProductFilterDTO(
                    null,                  // searchQuery
                    gender,
                    categoryIds,
                    null,                  // minPrice
                    null,                  // maxPrice
                    true,                  // newOnly
                    null,                  // interestIds
                    0,                     // page
                    10                     // pageSize
            );
        }

        @Test
        @DisplayName("should count new products with filters")
        void countNew_withFilters_returnsCount() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("MALE", List.of(2));
            when(productRepository.countNew(30, "MALE", List.of(2)))
                    .thenReturn(15L);

            // ACT
            long result = productService.countNew(filter);

            // ASSERT
            assertEquals(15L, result);
            verify(productRepository, times(1))
                    .countNew(30, "MALE", List.of(2));
        }

        @Test
        @DisplayName("should use NEW_DAYS constant (30 days)")
        void countNew_usesCorrectNewDaysConstant() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto(null, null);
            when(productRepository.countNew(30, null, null))
                    .thenReturn(100L);

            // ACT
            productService.countNew(filter);

            // ASSERT — verify 30 is passed as NEW_DAYS
            verify(productRepository, times(1))
                    .countNew(30, null, null);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  countFiltered()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countFiltered()")
    class CountFiltered {

        private ProductFilterDTO buildFilterDto(String gender, List<Integer> categoryIds,
                                                 Double minPrice, Double maxPrice) {
            return new ProductFilterDTO(
                    null,                  // searchQuery
                    gender,
                    categoryIds,
                    minPrice,
                    maxPrice,
                    false,                 // newOnly
                    null,                  // interestIds
                    0,                     // page
                    10                     // pageSize
            );
        }

        @Test
        @DisplayName("should count filtered products with all filter types")
        void countFiltered_withAllFilters_returnsCount() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("MALE", List.of(2), 100.0, 500.0);
            when(productRepository.countFiltered("MALE", List.of(2), 100.0, 500.0))
                    .thenReturn(25L);

            // ACT
            long result = productService.countFiltered(filter);

            // ASSERT
            assertEquals(25L, result);
            verify(productRepository, times(1))
                    .countFiltered("MALE", List.of(2), 100.0, 500.0);
        }

        @Test
        @DisplayName("should handle null filters in count")
        void countFiltered_withNullFilters_passesNullToRepository() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto(null, null, null, null);
            when(productRepository.countFiltered(null, null, null, null))
                    .thenReturn(5000L);

            // ACT
            long result = productService.countFiltered(filter);

            // ASSERT
            assertEquals(5000L, result);
            verify(productRepository, times(1))
                    .countFiltered(null, null, null, null);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  countSearchFiltered()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countSearchFiltered()")
    class CountSearchFiltered {

        private ProductFilterDTO buildFilterDto(String query, String gender,
                                                 List<Integer> categoryIds,
                                                 Double minPrice, Double maxPrice) {
            return new ProductFilterDTO(
                    query,                 // searchQuery
                    gender,
                    categoryIds,
                    minPrice,
                    maxPrice,
                    false,                 // newOnly
                    null,                  // interestIds
                    0,                     // page
                    10                     // pageSize
            );
        }

        @Test
        @DisplayName("should count search results with query and filters")
        void countSearchFiltered_withQuery_returnsCount() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("shirt", "MALE", List.of(2),
                    100.0, 300.0);
            when(productRepository.countSearchFiltered("shirt", "MALE", List.of(2),
                    100.0, 300.0))
                    .thenReturn(8L);

            // ACT
            long result = productService.countSearchFiltered(filter);

            // ASSERT
            assertEquals(8L, result);
            verify(productRepository, times(1))
                    .countSearchFiltered("shirt", "MALE", List.of(2), 100.0, 300.0);
        }

        @Test
        @DisplayName("should return zero for searches with no results")
        void countSearchFiltered_noMatches_returnsZero() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("nonexistent", null, null, null, null);
            when(productRepository.countSearchFiltered("nonexistent", null, null, null, null))
                    .thenReturn(0L);

            // ACT
            long result = productService.countSearchFiltered(filter);

            // ASSERT
            assertEquals(0L, result);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getMinMaxPriceForInterests()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getMinMaxPriceForInterests()")
    class GetMinMaxPriceForInterests {

        private ProductFilterDTO buildFilterDto(List<Integer> interestIds) {
            return new ProductFilterDTO(
                    null,                  // searchQuery
                    null,                  // gender — always null for interests
                    null,                  // categoryIds
                    null,                  // minPrice
                    null,                  // maxPrice
                    false,                 // newOnly
                    interestIds,           // interests
                    0,                     // page
                    10                     // pageSize
            );
        }

        @Test
        @DisplayName("should get min/max price for products by interests")
        void getMinMaxPriceForInterests_withInterests_returnsPriceRange() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto(List.of(1, 2, 3));
            PriceRangeDTO priceRange = new PriceRangeDTO(99.99, 899.99);
            when(productRepository.getMinMaxPrice(null, List.of(1, 2, 3)))
                    .thenReturn(priceRange);

            // ACT
            PriceRangeDTO result = productService.getMinMaxPriceForInterests(filter);

            // ASSERT — gender should always be null for interests
            assertNotNull(result);
            assertEquals(99.99, result.min());
            assertEquals(899.99, result.max());
            verify(productRepository, times(1))
                    .getMinMaxPrice(null, List.of(1, 2, 3));
        }

        @Test
        @DisplayName("should pass null gender to repository for interests")
        void getMinMaxPriceForInterests_alwaysNullGender() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto(List.of(5));
            when(productRepository.getMinMaxPrice(null, List.of(5)))
                    .thenReturn(new PriceRangeDTO(50.0, 500.0));

            // ACT
            productService.getMinMaxPriceForInterests(filter);

            // ASSERT
            verify(productRepository, times(1))
                    .getMinMaxPrice(null, List.of(5));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getMinMaxPrice()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getMinMaxPrice()")
    class GetMinMaxPrice {

        private ProductFilterDTO buildFilterDto(String gender, List<Integer> categoryIds) {
            return new ProductFilterDTO(
                    null,                  // searchQuery
                    gender,
                    categoryIds,
                    null,                  // minPrice
                    null,                  // maxPrice
                    false,                 // newOnly
                    null,                  // interestIds
                    0,                     // page
                    10                     // pageSize
            );
        }

        @Test
        @DisplayName("should get min/max price for filtered products")
        void getMinMaxPrice_withFilters_returnsPriceRange() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto("MALE", List.of(2));
            PriceRangeDTO priceRange = new PriceRangeDTO(149.99, 899.99);
            when(productRepository.getMinMaxPrice("MALE", List.of(2)))
                    .thenReturn(priceRange);

            // ACT
            PriceRangeDTO result = productService.getMinMaxPrice(filter);

            // ASSERT
            assertNotNull(result);
            assertEquals(149.99, result.min());
            assertEquals(899.99, result.max());
            verify(productRepository, times(1))
                    .getMinMaxPrice("MALE", List.of(2));
        }

        @Test
        @DisplayName("should handle null filters in price range query")
        void getMinMaxPrice_withNullFilters_passesNullToRepository() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDto(null, null);
            when(productRepository.getMinMaxPrice(null, null))
                    .thenReturn(new PriceRangeDTO(0.0, 2000.0));

            // ACT
            productService.getMinMaxPrice(filter);

            // ASSERT
            verify(productRepository, times(1))
                    .getMinMaxPrice(null, null);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  delete()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should delete product when it exists")
        void delete_existingProduct_deletesSuccessfully() {
            // ARRANGE
            when(productRepository.findById(1)).thenReturn(tshirtProduct);

            // ACT
            productService.delete(1);

            // ASSERT
            verify(productRepository, times(1)).findById(1);
            verify(productRepository, times(1)).delete(tshirtProduct);
        }

        @Test
        @DisplayName("should not call delete when product does not exist")
        void delete_nonexistentProduct_noDeleteCalled() {
            // ARRANGE
            when(productRepository.findById(999)).thenReturn(null);

            // ACT
            productService.delete(999);

            // ASSERT — delete should never be called if product is null
            verify(productRepository, times(1)).findById(999);
            verify(productRepository, never()).delete(any(Product.class));
        }

        @Test
        @DisplayName("should handle deletion gracefully even if product lookup fails")
        void delete_nullProduct_noException() {
            // ARRANGE
            when(productRepository.findById(0)).thenReturn(null);

            // ACT & ASSERT — should not throw
            assertDoesNotThrow(() -> productService.delete(0));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  save()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should save product and return saved instance")
        void save_validProduct_savesCalled() {
            // ARRANGE
            Product newProduct = new Product();
            newProduct.setName("New Shirt");
            newProduct.setBasePrice(BigDecimal.valueOf(199.99));

            when(productRepository.save(newProduct)).thenReturn(newProduct);

            // ACT
            Product result = productService.save(newProduct);

            // ASSERT
            assertNotNull(result);
            assertEquals("New Shirt", result.getName());
            verify(productRepository, times(1)).save(newProduct);
        }

        @Test
        @DisplayName("should return the same product instance after save")
        void save_productInstance_returnsSameInstance() {
            // ARRANGE
            Product product = new Product();
            product.setId(100);
            product.setName("Existing Product");

            when(productRepository.save(product)).thenReturn(product);

            // ACT
            Product result = productService.save(product);

            // ASSERT
            assertEquals(100, result.getId());
            verify(productRepository, times(1)).save(product);
        }

        @Test
        @DisplayName("should handle product with all optional fields")
        void save_productWithAllFields_savesSuccessfully() {
            // ARRANGE
            Product fullProduct = new Product();
            fullProduct.setId(50);
            fullProduct.setName("Full Product");
            fullProduct.setDescription("Complete details");
            fullProduct.setBasePrice(BigDecimal.valueOf(299.99));
            fullProduct.setCategory(menCategory);
            fullProduct.setImageUrl("full.jpg");
            fullProduct.setCreatedAt(Instant.now());

            when(productRepository.save(fullProduct)).thenReturn(fullProduct);

            // ACT
            Product result = productService.save(fullProduct);

            // ASSERT
            assertNotNull(result);
            assertEquals("Full Product", result.getName());
            assertEquals(menCategory, result.getCategory());
            verify(productRepository, times(1)).save(fullProduct);
        }
    }
}
