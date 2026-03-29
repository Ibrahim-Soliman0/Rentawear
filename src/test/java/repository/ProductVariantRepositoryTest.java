package repository;

import entity.Category;
import entity.Product;
import entity.ProductVariant;
import entity.enums.Gender;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import repository.impl.ProductVariantRepositoryImpl;
import util.EntityManagerContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ProductVariantRepositoryTest")
public class ProductVariantRepositoryTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private ProductVariantRepositoryImpl variantRepository;

    // Test data
    private Product testProduct;
    private Product anotherProduct;
    private Category testCategory;

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

        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setGender(Gender.MALE);
        em.persist(testCategory);

        // Create test products
        testProduct = new Product();
        testProduct.setName("Test Product 1");
        testProduct.setCategory(testCategory);
        testProduct.setBasePrice(new BigDecimal("49.99"));
        testProduct.setImageUrl("https://example.com/product1.jpg");
        em.persist(testProduct);

        anotherProduct = new Product();
        anotherProduct.setName("Test Product 2");
        anotherProduct.setCategory(testCategory);
        anotherProduct.setBasePrice(new BigDecimal("99.99"));
        anotherProduct.setImageUrl("https://example.com/product2.jpg");
        em.persist(anotherProduct);

        em.flush();

        variantRepository = new ProductVariantRepositoryImpl();
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

    private ProductVariant createVariant(Product product, String color, String size, int quantity) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(color);
        variant.setSize(size);
        variant.setQuantity(quantity);
        return variant;
    }

    private ProductVariant persistVariant(Product product, String color, String size, int quantity) {
        ProductVariant variant = createVariant(product, color, size, quantity);
        em.persist(variant);
        em.flush();
        return variant;
    }

    // ==================== NESTED TEST CLASSES ====================

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        @DisplayName("should retrieve existing variant by ID")
        void shouldRetrieveExistingVariant() {
            ProductVariant variant = persistVariant(testProduct, "Red", "M", 50);
            em.detach(variant);

            ProductVariant retrieved = variantRepository.findById(variant.getId());

            assertNotNull(retrieved);
            assertEquals(variant.getId(), retrieved.getId());
            assertEquals("Red", retrieved.getColor());
            assertEquals("M", retrieved.getSize());
            assertEquals(50, retrieved.getQuantity());
        }

        @Test
        @DisplayName("should return null for non-existent variant ID")
        void shouldReturnNullForNonExistentId() {
            ProductVariant retrieved = variantRepository.findById(9999);

            assertNull(retrieved);
        }
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        @DisplayName("should persist new variant")
        void shouldPersistNewVariant() {
            ProductVariant variant = createVariant(testProduct, "Blue", "L", 30);

            ProductVariant saved = variantRepository.save(variant);

            assertNotNull(saved.getId());
            assertEquals("Blue", saved.getColor());

            em.detach(saved);
            ProductVariant retrieved = variantRepository.findById(saved.getId());
            assertNotNull(retrieved);
            assertEquals("Blue", retrieved.getColor());
        }

        @Test
        @DisplayName("should update existing variant quantity and size")
        void shouldUpdateExistingVariant() {
            ProductVariant variant = persistVariant(testProduct, "Green", "S", 25);
            Integer variantId = variant.getId();

            variant.setQuantity(15);
            variant.setSize("M");
            variantRepository.save(variant);

            em.detach(variant);
            ProductVariant retrieved = variantRepository.findById(variantId);
            assertEquals("M", retrieved.getSize());
            assertEquals(15, retrieved.getQuantity());
        }
    }

    @Nested
    @DisplayName("FindAll")
    class FindAll {

        @Test
        @DisplayName("should return all variants")
        void shouldReturnAllVariants() {
            persistVariant(testProduct, "Red", "M", 50);
            persistVariant(testProduct, "Blue", "L", 30);
            persistVariant(anotherProduct, "Black", "M", 20);

            List<ProductVariant> variants = variantRepository.findAll();

            assertEquals(3, variants.size());
        }

        @Test
        @DisplayName("should return empty list when no variants exist")
        void shouldReturnEmptyListWhenNoVariants() {
            List<ProductVariant> variants = variantRepository.findAll();

            assertTrue(variants.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        @DisplayName("should delete variant")
        void shouldDeleteVariant() {
            ProductVariant variant = persistVariant(testProduct, "Red", "M", 50);
            Integer variantId = variant.getId();

            variantRepository.delete(variant);
            em.flush();

            ProductVariant retrieved = variantRepository.findById(variantId);
            assertNull(retrieved);
        }

        @Test
        @DisplayName("should handle deletion of already deleted variant")
        void shouldHandleAlreadyDeletedVariant() {
            ProductVariant variant = persistVariant(testProduct, "Red", "M", 50);
            variantRepository.delete(variant);
            em.flush();

            assertDoesNotThrow(() -> {
                em.remove(em.merge(variant));
            });
        }
    }

    @Nested
    @DisplayName("FindByProductId")
    class FindByProductId {

        @Test
        @DisplayName("should find all variants for a product ordered by ID ascending")
        void shouldFindAllVariantsForProduct() {
            persistVariant(testProduct, "Red", "M", 50);
            persistVariant(testProduct, "Red", "L", 40);
            persistVariant(testProduct, "Blue", "M", 30);
            persistVariant(anotherProduct, "Black", "M", 20);

            List<ProductVariant> variants = variantRepository.findByProductId(testProduct.getId());

            assertEquals(3, variants.size());
            assertTrue(variants.stream().allMatch(v -> v.getProduct().getId().equals(testProduct.getId())));
        }

        @Test
        @DisplayName("should return empty list for product with no variants")
        void shouldReturnEmptyListForProductWithNoVariants() {
            List<ProductVariant> variants = variantRepository.findByProductId(testProduct.getId());

            assertTrue(variants.isEmpty());
        }

        @Test
        @DisplayName("should maintain order by ID ascending")
        void shouldMaintainOrderByIdAscending() {
            ProductVariant v1 = persistVariant(testProduct, "Red", "M", 50);
            ProductVariant v2 = persistVariant(testProduct, "Blue", "L", 40);
            ProductVariant v3 = persistVariant(testProduct, "Green", "S", 30);

            List<ProductVariant> variants = variantRepository.findByProductId(testProduct.getId());

            assertEquals(3, variants.size());
            assertEquals(v1.getId(), variants.get(0).getId());
            assertEquals(v2.getId(), variants.get(1).getId());
            assertEquals(v3.getId(), variants.get(2).getId());
        }

        @Test
        @DisplayName("should handle multiple color-size combinations")
        void shouldHandleMultipleColorSizeCombinations() {
            persistVariant(testProduct, "Red", "S", 50);
            persistVariant(testProduct, "Red", "M", 40);
            persistVariant(testProduct, "Red", "L", 30);
            persistVariant(testProduct, "Blue", "M", 25);
            persistVariant(testProduct, "Blue", "L", 20);

            List<ProductVariant> variants = variantRepository.findByProductId(testProduct.getId());

            assertEquals(5, variants.size());
            assertEquals(3, variants.stream().filter(v -> "Red".equals(v.getColor())).count());
            assertEquals(2, variants.stream().filter(v -> "Blue".equals(v.getColor())).count());
        }
    }

    @Nested
    @DisplayName("FindDistinctColorsByProductId")
    class FindDistinctColorsByProductId {

        @Test
        @DisplayName("should return distinct colors for a product")
        void shouldReturnDistinctColors() {
            persistVariant(testProduct, "Red", "M", 50);
            persistVariant(testProduct, "Red", "L", 40);
            persistVariant(testProduct, "Blue", "M", 30);
            persistVariant(testProduct, "Green", "S", 20);

            List<String> colors = variantRepository.findDistinctColorsByProductId(testProduct.getId());

            assertEquals(3, colors.size());
            assertTrue(colors.contains("Red"));
            assertTrue(colors.contains("Blue"));
            assertTrue(colors.contains("Green"));
        }

        @Test
        @DisplayName("should return empty list for product with no variants")
        void shouldReturnEmptyListForProductWithNoVariants() {
            List<String> colors = variantRepository.findDistinctColorsByProductId(testProduct.getId());

            assertTrue(colors.isEmpty());
        }

        @Test
        @DisplayName("should return colors for specified product only")
        void shouldReturnColorsForSpecifiedProductOnly() {
            persistVariant(testProduct, "Red", "M", 50);
            persistVariant(testProduct, "Blue", "M", 40);
            persistVariant(anotherProduct, "Black", "M", 30);
            persistVariant(anotherProduct, "White", "M", 20);

            List<String> colors = variantRepository.findDistinctColorsByProductId(testProduct.getId());

            assertEquals(2, colors.size());
            assertTrue(colors.contains("Red"));
            assertTrue(colors.contains("Blue"));
            assertFalse(colors.contains("Black"));
        }
    }

    @Nested
    @DisplayName("FindSizesByProductIdAndColor")
    class FindSizesByProductIdAndColor {

        @Test
        @DisplayName("should return all sizes for product-color combination")
        void shouldReturnSizesForProductAndColor() {
            persistVariant(testProduct, "Red", "S", 50);
            persistVariant(testProduct, "Red", "M", 40);
            persistVariant(testProduct, "Red", "L", 30);
            persistVariant(testProduct, "Blue", "M", 20);

            List<String> sizes = variantRepository.findSizesByProductIdAndColor(testProduct.getId(), "Red");

            assertEquals(3, sizes.size());
            assertTrue(sizes.contains("S"));
            assertTrue(sizes.contains("M"));
            assertTrue(sizes.contains("L"));
        }

        @Test
        @DisplayName("should return empty list for non-existent product-color combination")
        void shouldReturnEmptyListForNonExistentCombination() {
            persistVariant(testProduct, "Red", "M", 50);

            List<String> sizes = variantRepository.findSizesByProductIdAndColor(testProduct.getId(), "Yellow");

            assertTrue(sizes.isEmpty());
        }

        @Test
        @DisplayName("should filter by color precisely")
        void shouldFilterByColorPrecisely() {
            persistVariant(testProduct, "Red", "M", 50);
            persistVariant(testProduct, "RedOrange", "M", 40);

            List<String> sizes = variantRepository.findSizesByProductIdAndColor(testProduct.getId(), "Red");

            assertEquals(1, sizes.size());
            assertFalse(sizes.contains("RedOrange"));
        }

        @Test
        @DisplayName("should handle NULL sizes")
        void shouldHandleNullSizes() {
            persistVariant(testProduct, "Red", null, 50);
            persistVariant(testProduct, "Red", "M", 40);

            List<String> sizes = variantRepository.findSizesByProductIdAndColor(testProduct.getId(), "Red");

            assertEquals(2, sizes.size());
            assertTrue(sizes.contains(null) || sizes.contains("M"));
        }
    }

    @Nested
    @DisplayName("FindAvailableSizesByProductIdAndColor")
    class FindAvailableSizesByProductIdAndColor {

        @Test
        @DisplayName("should return only sizes with quantity > 0")
        void shouldReturnOnlyAvailableSizes() {
            persistVariant(testProduct, "Red", "S", 50);
            persistVariant(testProduct, "Red", "M", 1);   // Low stock but available
            persistVariant(testProduct, "Red", "L", 0);   // Out of stock
            persistVariant(testProduct, "Red", "XL", 25);

            List<String> sizes = variantRepository.findAvailableSizesByProductIdAndColor(testProduct.getId(), "Red");

            assertEquals(3, sizes.size());
            assertTrue(sizes.contains("S"));
            assertTrue(sizes.contains("M"));
            assertTrue(sizes.contains("XL"));
            assertFalse(sizes.contains("L"));
        }

        @Test
        @DisplayName("should return empty list when all sizes are out of stock")
        void shouldReturnEmptyListWhenAllOutOfStock() {
            persistVariant(testProduct, "Red", "S", 0);
            persistVariant(testProduct, "Red", "M", 0);
            persistVariant(testProduct, "Red", "L", 0);

            List<String> sizes = variantRepository.findAvailableSizesByProductIdAndColor(testProduct.getId(), "Red");

            assertTrue(sizes.isEmpty());
        }

        @Test
        @DisplayName("should filter by quantity and color")
        void shouldFilterByQuantityAndColor() {
            persistVariant(testProduct, "Red", "M", 50);
            persistVariant(testProduct, "Red", "L", 0);
            persistVariant(testProduct, "Blue", "M", 40);
            persistVariant(testProduct, "Blue", "L", 0);

            List<String> redSizes = variantRepository.findAvailableSizesByProductIdAndColor(testProduct.getId(), "Red");
            List<String> blueSizes = variantRepository.findAvailableSizesByProductIdAndColor(testProduct.getId(), "Blue");

            assertEquals(1, redSizes.size());
            assertEquals("M", redSizes.get(0));
            assertEquals(1, blueSizes.size());
            assertEquals("M", blueSizes.get(0));
        }
    }

    @Nested
    @DisplayName("FindByProductIds")
    class FindByProductIds {

        @Test
        @DisplayName("should find variants for multiple products in one query")
        void shouldFindVariantsForMultipleProducts() {
            persistVariant(testProduct, "Red", "M", 50);
            persistVariant(testProduct, "Blue", "L", 40);
            persistVariant(anotherProduct, "Black", "M", 30);
            persistVariant(anotherProduct, "White", "S", 20);

            List<Integer> productIds = List.of(testProduct.getId(), anotherProduct.getId());
            List<ProductVariant> variants = variantRepository.findByProductIds(productIds);

            assertEquals(4, variants.size());
        }

        @Test
        @DisplayName("should return empty list for empty product IDs")
        void shouldReturnEmptyListForEmptyProductIds() {
            persistVariant(testProduct, "Red", "M", 50);

            List<ProductVariant> variants = variantRepository.findByProductIds(List.of());

            assertTrue(variants.isEmpty());
        }

        @Test
        @DisplayName("should skip products with no variants")
        void shouldSkipProductsWithNoVariants() {
            persistVariant(testProduct, "Red", "M", 50);

            List<Integer> productIds = List.of(testProduct.getId(), anotherProduct.getId());
            List<ProductVariant> variants = variantRepository.findByProductIds(productIds);

            assertEquals(1, variants.size());
            assertEquals(testProduct.getId(), variants.get(0).getProduct().getId());
        }

        @Test
        @DisplayName("should return results ordered by product.id then variant.id")
        void shouldReturnOrderedResultsByProductThenVariant() {
            ProductVariant v1 = persistVariant(testProduct, "Red", "M", 50);
            ProductVariant v2 = persistVariant(testProduct, "Blue", "L", 40);
            ProductVariant v3 = persistVariant(anotherProduct, "Black", "M", 30);

            List<Integer> productIds = List.of(testProduct.getId(), anotherProduct.getId());
            List<ProductVariant> variants = variantRepository.findByProductIds(productIds);

            // Should be grouped by product, then ordered by variant ID
            Integer lastProductId = -1;
            Integer lastVariantId = -1;
            for (ProductVariant v : variants) {
                assertTrue(v.getProduct().getId() >= lastProductId);
                if (v.getProduct().getId().equals(lastProductId)) {
                    assertTrue(v.getId() >= lastVariantId);
                }
                lastProductId = v.getProduct().getId();
                lastVariantId = v.getId();
            }
        }
    }

    @Nested
    @DisplayName("DeleteByProductIdAndColor")
    class DeleteByProductIdAndColor {

        @Test
        @DisplayName("should delete all variants for product-color combination")
        void shouldDeleteVariantsByProductAndColor() {
            persistVariant(testProduct, "Red", "S", 50);
            persistVariant(testProduct, "Red", "M", 40);
            persistVariant(testProduct, "Red", "L", 30);
            persistVariant(testProduct, "Blue", "M", 20);

            variantRepository.deleteByProductIdAndColor(testProduct.getId(), "Red");
            em.flush();

            List<ProductVariant> redVariants = variantRepository.findByProductId(testProduct.getId());

            assertEquals(1, redVariants.size());
            assertEquals("Blue", redVariants.get(0).getColor());
        }

        @Test
        @DisplayName("should not delete variants of other products")
        void shouldNotDeleteVariantsOfOtherProducts() {
            persistVariant(testProduct, "Red", "M", 50);
            persistVariant(anotherProduct, "Red", "M", 40);

            variantRepository.deleteByProductIdAndColor(testProduct.getId(), "Red");
            em.flush();

            List<ProductVariant> otherProductVariants = variantRepository.findByProductId(anotherProduct.getId());

            assertEquals(1, otherProductVariants.size());
            assertEquals("Red", otherProductVariants.get(0).getColor());
        }

        @Test
        @DisplayName("should handle deletion of non-existent product-color combination safely")
        void shouldHandleNonExistentCombination() {
            assertDoesNotThrow(() -> {
                variantRepository.deleteByProductIdAndColor(testProduct.getId(), "NonExistent");
                em.flush();
            });
        }
    }

    @Nested
    @DisplayName("DeleteByProductId")
    class DeleteByProductId {

        @Test
        @DisplayName("should delete all variants for a product")
        void shouldDeleteAllVariantsForProduct() {
            persistVariant(testProduct, "Red", "M", 50);
            persistVariant(testProduct, "Blue", "L", 40);
            persistVariant(testProduct, "Green", "S", 30);
            persistVariant(anotherProduct, "Black", "M", 20);

            variantRepository.deleteByProductId(testProduct.getId());
            em.flush();

            List<ProductVariant> testProductVariants = variantRepository.findByProductId(testProduct.getId());
            List<ProductVariant> otherProductVariants = variantRepository.findByProductId(anotherProduct.getId());

            assertEquals(0, testProductVariants.size());
            assertEquals(1, otherProductVariants.size());
        }

        @Test
        @DisplayName("should handle deletion of product with no variants")
        void shouldHandleProductWithNoVariants() {
            assertDoesNotThrow(() -> {
                variantRepository.deleteByProductId(testProduct.getId());
                em.flush();
            });
        }

        @Test
        @DisplayName("should preserve variants of other products")
        void shouldPreserveVariantsOfOtherProducts() {
            persistVariant(testProduct, "Red", "M", 50);
            ProductVariant otherVariant = persistVariant(anotherProduct, "Black", "M", 40);

            variantRepository.deleteByProductId(testProduct.getId());
            em.flush();

            ProductVariant preserved = variantRepository.findById(otherVariant.getId());
            assertNotNull(preserved);
        }
    }

    @Nested
    @DisplayName("Integration")
    class Integration {

        @Test
        @DisplayName("should manage complete product variant lifecycle")
        void shouldManageCompleteLifecycle() {
            // Create
            ProductVariant red = persistVariant(testProduct, "Red", "M", 50);
            ProductVariant blue = persistVariant(testProduct, "Blue", "L", 40);

            // Read
            List<ProductVariant> allVariants = variantRepository.findByProductId(testProduct.getId());
            assertEquals(2, allVariants.size());

            // Update
            red.setQuantity(25);
            variantRepository.save(red);
            em.flush();

            ProductVariant updated = variantRepository.findById(red.getId());
            assertEquals(25, updated.getQuantity());

            // Delete by color
            variantRepository.deleteByProductIdAndColor(testProduct.getId(), "Red");
            em.flush();

            List<ProductVariant> remaining = variantRepository.findByProductId(testProduct.getId());
            assertEquals(1, remaining.size());
            assertEquals("Blue", remaining.get(0).getColor());
        }

        @Test
        @DisplayName("should maintain product association through variant operations")
        void shouldMaintainProductAssociation() {
            ProductVariant variant = persistVariant(testProduct, "Red", "M", 50);
            Integer variantId = variant.getId();

            em.detach(variant);
            ProductVariant retrieved = variantRepository.findById(variantId);

            assertNotNull(retrieved.getProduct());
            assertEquals(testProduct.getId(), retrieved.getProduct().getId());
        }

        @Test
        @DisplayName("should handle complex stock scenarios")
        void shouldHandleComplexStockScenarios() {
            // Populate with different stock levels
            persistVariant(testProduct, "Red", "S", 100);   // High stock
            persistVariant(testProduct, "Red", "M", 1);     // Low stock
            persistVariant(testProduct, "Red", "L", 0);     // Out of stock
            persistVariant(testProduct, "Blue", "M", 50);
            persistVariant(testProduct, "Blue", "L", 0);

            List<String> redSizes = variantRepository.findSizesByProductIdAndColor(testProduct.getId(), "Red");
            List<String> redAvailable = variantRepository.findAvailableSizesByProductIdAndColor(testProduct.getId(), "Red");
            List<String> colors = variantRepository.findDistinctColorsByProductId(testProduct.getId());

            assertEquals(3, redSizes.size());
            assertEquals(2, redAvailable.size());
            assertEquals(2, colors.size());
        }

        @Test
        @DisplayName("should support batch operations for catalog display")
        void shouldSupportBatchOperationsForCatalogDisplay() {
            persistVariant(testProduct, "Red", "M", 50);
            persistVariant(testProduct, "Blue", "L", 40);
            persistVariant(anotherProduct, "Black", "M", 30);
            persistVariant(anotherProduct, "White", "S", 20);

            List<Integer> productIds = List.of(testProduct.getId(), anotherProduct.getId());
            List<ProductVariant> variants = variantRepository.findByProductIds(productIds);
            List<String> colors1 = variantRepository.findDistinctColorsByProductId(testProduct.getId());
            List<String> colors2 = variantRepository.findDistinctColorsByProductId(anotherProduct.getId());

            assertEquals(4, variants.size());
            assertEquals(2, colors1.size());
            assertEquals(2, colors2.size());
        }

        @Test
        @DisplayName("should handle partial deletions correctly")
        void shouldHandlePartialDeletions() {
            persistVariant(testProduct, "Red", "S", 50);
            persistVariant(testProduct, "Red", "M", 40);
            persistVariant(testProduct, "Blue", "M", 30);

            // Delete Red color only
            variantRepository.deleteByProductIdAndColor(testProduct.getId(), "Red");
            em.flush();

            List<ProductVariant> remaining = variantRepository.findByProductId(testProduct.getId());
            assertEquals(1, remaining.size());
            assertEquals("Blue", remaining.get(0).getColor());
            assertEquals("M", remaining.get(0).getSize());
        }
    }
}
