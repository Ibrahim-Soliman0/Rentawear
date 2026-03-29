package service;

import entity.Product;
import entity.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ProductVariantRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductVariantServiceTest {

    // ── Mocks (fake objects we control) ──────────────────────────────────────

    @Mock
    private ProductVariantRepository variantRepository;   // fake DB — no real database used

    // ── The real object under test ────────────────────────────────────────────

    @InjectMocks
    private ProductVariantService variantService;         // real ProductVariantService, with mock injected

    // ── Shared test data ──────────────────────────────────────────────────────

    private Product product;
    private ProductVariant redSmallVariant;
    private ProductVariant redLargeVariant;
    private ProductVariant blueSmallVariant;
    private ProductVariant blueOutOfStockVariant;

    @BeforeEach
    void setUp() {
        /*
         * Build a reusable Product and several variants with different colors,
         * sizes, and stock levels.
         * We reset these before every test so one test can't affect another.
         */

        // Product setup
        product = new Product();
        product.setId(1);
        product.setName("Basic T-Shirt");
        product.setDescription("Comfortable cotton tshirt");
        product.setBasePrice(BigDecimal.valueOf(149.99));
        product.setImageUrl("tshirt.jpg");
        product.setCreatedAt(Instant.now());

        // Variant 1: Red, Small, In stock
        redSmallVariant = new ProductVariant();
        redSmallVariant.setId(10);
        redSmallVariant.setProduct(product);
        redSmallVariant.setColor("Red");
        redSmallVariant.setSize("S");
        redSmallVariant.setQuantity(25);

        // Variant 2: Red, Large, In stock
        redLargeVariant = new ProductVariant();
        redLargeVariant.setId(11);
        redLargeVariant.setProduct(product);
        redLargeVariant.setColor("Red");
        redLargeVariant.setSize("L");
        redLargeVariant.setQuantity(10);

        // Variant 3: Blue, Small, In stock
        blueSmallVariant = new ProductVariant();
        blueSmallVariant.setId(12);
        blueSmallVariant.setProduct(product);
        blueSmallVariant.setColor("Blue");
        blueSmallVariant.setSize("S");
        blueSmallVariant.setQuantity(5);

        // Variant 4: Blue, Small, Out of stock
        blueOutOfStockVariant = new ProductVariant();
        blueOutOfStockVariant.setId(13);
        blueOutOfStockVariant.setProduct(product);
        blueOutOfStockVariant.setColor("Blue");
        blueOutOfStockVariant.setSize("M");
        blueOutOfStockVariant.setQuantity(0);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getByProductId()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getByProductId()")
    class GetByProductId {

        @Test
        @DisplayName("should return all variants for a product")
        void getByProductId_validProductId_returnsAllVariants() {
            // ARRANGE
            List<ProductVariant> variants = List.of(
                    redSmallVariant,
                    redLargeVariant,
                    blueSmallVariant,
                    blueOutOfStockVariant
            );
            when(variantRepository.findByProductId(1)).thenReturn(variants);

            // ACT
            List<ProductVariant> result = variantService.getByProductId(1);

            // ASSERT
            assertNotNull(result, "Should return non-null list");
            assertEquals(4, result.size(), "Should return all 4 variants");
            assertTrue(result.contains(redSmallVariant), "Should contain red small");
            assertTrue(result.contains(blueOutOfStockVariant), "Should include out-of-stock variants");
            verify(variantRepository, times(1)).findByProductId(1);
        }

        @Test
        @DisplayName("should return empty list when product has no variants")
        void getByProductId_noVariants_returnsEmptyList() {
            // ARRANGE
            when(variantRepository.findByProductId(999)).thenReturn(List.of());

            // ACT
            List<ProductVariant> result = variantService.getByProductId(999);

            // ASSERT
            assertNotNull(result, "Should return empty list, not null");
            assertTrue(result.isEmpty(), "Should return empty list");
            verify(variantRepository, times(1)).findByProductId(999);
        }

        @Test
        @DisplayName("should return variants ordered by id ascending")
        void getByProductId_multipleVariants_returnsOrderedList() {
            // ARRANGE
            List<ProductVariant> variants = List.of(
                    redSmallVariant,       // id = 10
                    redLargeVariant,       // id = 11
                    blueSmallVariant,      // id = 12
                    blueOutOfStockVariant  // id = 13
            );
            when(variantRepository.findByProductId(1)).thenReturn(variants);

            // ACT
            List<ProductVariant> result = variantService.getByProductId(1);

            // ASSERT — verify order is maintained
            assertEquals(10, result.get(0).getId());
            assertEquals(11, result.get(1).getId());
            assertEquals(12, result.get(2).getId());
            assertEquals(13, result.get(3).getId());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getDistinctColors()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getDistinctColors()")
    class GetDistinctColors {

        @Test
        @DisplayName("should return distinct colors for a product")
        void getDistinctColors_multipleVariants_returnsDistinctColors() {
            // ARRANGE
            List<String> colors = List.of("Blue", "Red");
            when(variantRepository.findDistinctColorsByProductId(1)).thenReturn(colors);

            // ACT
            List<String> result = variantService.getDistinctColors(1);

            // ASSERT
            assertNotNull(result, "Should return non-null list");
            assertEquals(2, result.size(), "Should return 2 distinct colors");
            assertTrue(result.contains("Red"), "Should contain Red");
            assertTrue(result.contains("Blue"), "Should contain Blue");
            verify(variantRepository, times(1)).findDistinctColorsByProductId(1);
        }

        @Test
        @DisplayName("should return colors ordered alphabetically")
        void getDistinctColors_multipleVariants_returnsOrderedColors() {
            // ARRANGE
            List<String> colors = List.of("Blue", "Green", "Red");
            when(variantRepository.findDistinctColorsByProductId(1)).thenReturn(colors);

            // ACT
            List<String> result = variantService.getDistinctColors(1);

            // ASSERT — verify alphabetical order
            assertEquals("Blue", result.get(0));
            assertEquals("Green", result.get(1));
            assertEquals("Red", result.get(2));
        }

        @Test
        @DisplayName("should return empty list when product has no variants")
        void getDistinctColors_noVariants_returnsEmptyList() {
            // ARRANGE
            when(variantRepository.findDistinctColorsByProductId(999)).thenReturn(List.of());

            // ACT
            List<String> result = variantService.getDistinctColors(999);

            // ASSERT
            assertTrue(result.isEmpty(), "Should return empty list");
        }

        @Test
        @DisplayName("should return single color when product has one color only")
        void getDistinctColors_singleColor_returnsOneElement() {
            // ARRANGE
            when(variantRepository.findDistinctColorsByProductId(5)).thenReturn(List.of("Black"));

            // ACT
            List<String> result = variantService.getDistinctColors(5);

            // ASSERT
            assertEquals(1, result.size());
            assertEquals("Black", result.getFirst());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getSizesByColor()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getSizesByColor()")
    class GetSizesByColor {

        @Test
        @DisplayName("should return all sizes for a product and color")
        void getSizesByColor_validProductAndColor_returnsSizes() {
            // ARRANGE
            List<String> sizes = List.of("S", "L");
            when(variantRepository.findSizesByProductIdAndColor(1, "Red")).thenReturn(sizes);

            // ACT
            List<String> result = variantService.getSizesByColor(1, "Red");

            // ASSERT
            assertNotNull(result, "Should return non-null list");
            assertEquals(2, result.size(), "Should return 2 sizes");
            assertTrue(result.contains("S"), "Should contain Small");
            assertTrue(result.contains("L"), "Should contain Large");
            verify(variantRepository, times(1)).findSizesByProductIdAndColor(1, "Red");
        }

        @Test
        @DisplayName("should return sizes including out-of-stock")
        void getSizesByColor_includesOutOfStock_returnsAllSizes() {
            // ARRANGE — includes M which is out of stock
            List<String> sizes = List.of("S", "M", "L");
            when(variantRepository.findSizesByProductIdAndColor(1, "Blue")).thenReturn(sizes);

            // ACT
            List<String> result = variantService.getSizesByColor(1, "Blue");

            // ASSERT
            assertEquals(3, result.size());
            assertTrue(result.contains("M"), "Should include out-of-stock size");
        }

        @Test
        @DisplayName("should return empty list when color does not exist")
        void getSizesByColor_invalidColor_returnsEmptyList() {
            // ARRANGE
            when(variantRepository.findSizesByProductIdAndColor(1, "Purple")).thenReturn(List.of());

            // ACT
            List<String> result = variantService.getSizesByColor(1, "Purple");

            // ASSERT
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return sizes ordered by id ascending")
        void getSizesByColor_orderedSizes_returnsInOrder() {
            // ARRANGE
            List<String> sizes = List.of("XS", "S", "M", "L", "XL");
            when(variantRepository.findSizesByProductIdAndColor(1, "Red")).thenReturn(sizes);

            // ACT
            List<String> result = variantService.getSizesByColor(1, "Red");

            // ASSERT
            assertAll(
                    "Sizes should be ordered by variant id",
                    () -> assertEquals("XS", result.getFirst()),
                    () -> assertEquals("S", result.get(1)),
                    () -> assertEquals("M", result.get(2)),
                    () -> assertEquals("L", result.get(3)),
                    () -> assertEquals("XL", result.get(4))
            );
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getAvailableSizesByColor()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAvailableSizesByColor()")
    class GetAvailableSizesByColor {

        @Test
        @DisplayName("should return only in-stock sizes for a color")
        void getAvailableSizesByColor_inStockOnly_returnsAvailableSizes() {
            // ARRANGE
            List<String> availableSizes = List.of("S", "M");
            when(variantRepository.findAvailableSizesByProductIdAndColor(1, "Blue"))
                    .thenReturn(availableSizes);

            // ACT
            List<String> result = variantService.getAvailableSizesByColor(1, "Blue");

            // ASSERT — should not include out-of-stock sizes
            assertEquals(2, result.size());
            assertTrue(result.contains("S"));
            assertTrue(result.contains("M"));
            verify(variantRepository, times(1))
                    .findAvailableSizesByProductIdAndColor(1, "Blue");
        }

        @Test
        @DisplayName("should return empty list when all sizes are out of stock")
        void getAvailableSizesByColor_allOutOfStock_returnsEmptyList() {
            // ARRANGE
            when(variantRepository.findAvailableSizesByProductIdAndColor(1, "Purple"))
                    .thenReturn(List.of());

            // ACT
            List<String> result = variantService.getAvailableSizesByColor(1, "Purple");

            // ASSERT
            assertTrue(result.isEmpty(), "Should return empty when no sizes in stock");
        }

        @Test
        @DisplayName("should filter out sizes with quantity = 0")
        void getAvailableSizesByColor_excludesZeroQuantity() {
            // ARRANGE
            List<String> availableSizes = List.of("S", "L");  // M is excluded (qty=0)
            when(variantRepository.findAvailableSizesByProductIdAndColor(1, "Red"))
                    .thenReturn(availableSizes);

            // ACT
            List<String> result = variantService.getAvailableSizesByColor(1, "Red");

            // ASSERT
            assertEquals(2, result.size());
            assertFalse(result.contains("M"), "Should not include out-of-stock M size");
        }

        @Test
        @DisplayName("should use repository's findAvailableSizesByProductIdAndColor")
        void getAvailableSizesByColor_callsCorrectRepository() {
            // ARRANGE
            when(variantRepository.findAvailableSizesByProductIdAndColor(2, "Green"))
                    .thenReturn(List.of("M", "L"));

            // ACT
            variantService.getAvailableSizesByColor(2, "Green");

            // ASSERT
            verify(variantRepository, times(1))
                    .findAvailableSizesByProductIdAndColor(2, "Green");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getByProductIds()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getByProductIds()")
    class GetByProductIds {

        @Test
        @DisplayName("should return variants grouped by product id")
        void getByProductIds_multipleProducts_returnsGroupedVariants() {
            // ARRANGE — variants for product 1 and product 2
            Product product2 = new Product();
            product2.setId(2);

            ProductVariant var1 = new ProductVariant();
            var1.setId(101);
            var1.setProduct(product);
            var1.setColor("Red");
            var1.setSize("S");

            ProductVariant var2 = new ProductVariant();
            var2.setId(102);
            var2.setProduct(product);
            var2.setColor("Blue");
            var2.setSize("L");

            ProductVariant var3 = new ProductVariant();
            var3.setId(201);
            var3.setProduct(product2);
            var3.setColor("Black");
            var3.setSize("M");

            List<ProductVariant> allVariants = List.of(var1, var2, var3);
            when(variantRepository.findByProductIds(List.of(1, 2))).thenReturn(allVariants);

            // ACT
            Map<Integer, List<ProductVariant>> result = variantService.getByProductIds(
                    List.of(1, 2)
            );

            // ASSERT — verify grouping
            assertNotNull(result);
            assertEquals(2, result.size(), "Should have 2 product groups");
            assertEquals(2, result.get(1).size(), "Product 1 should have 2 variants");
            assertEquals(1, result.get(2).size(), "Product 2 should have 1 variant");
        }

        @Test
        @DisplayName("should maintain LinkedHashMap ordering (product id order)")
        void getByProductIds_multipleProducts_maintainOrder() {
            // ARRANGE
            Product product2 = new Product();
            product2.setId(2);
            Product product3 = new Product();
            product3.setId(3);

            ProductVariant var1 = new ProductVariant();
            var1.setId(1);
            var1.setProduct(product);

            ProductVariant var2 = new ProductVariant();
            var2.setId(2);
            var2.setProduct(product2);

            ProductVariant var3 = new ProductVariant();
            var3.setId(3);
            var3.setProduct(product3);

            when(variantRepository.findByProductIds(List.of(1, 2, 3)))
                    .thenReturn(List.of(var1, var2, var3));

            // ACT
            Map<Integer, List<ProductVariant>> result = variantService.getByProductIds(
                    List.of(1, 2, 3)
            );

            // ASSERT — verify order is maintained
            List<Integer> keys = new ArrayList<>(result.keySet());
            assertEquals(List.of(1, 2, 3), keys, "Keys should be in order 1, 2, 3");
        }

        @Test
        @DisplayName("should return empty map when no products provided")
        void getByProductIds_emptyList_returnsEmptyMap() {
            // ARRANGE
            when(variantRepository.findByProductIds(List.of())).thenReturn(List.of());

            // ACT
            Map<Integer, List<ProductVariant>> result = variantService.getByProductIds(
                    List.of()
            );

            // ASSERT
            assertTrue(result.isEmpty(), "Should return empty map");
        }

        @Test
        @DisplayName("should group all variants under correct product id")
        void getByProductIds_correctGrouping_allVariantsGrouped() {
            // ARRANGE
            Product product2 = new Product();
            product2.setId(2);

            ProductVariant var1a = new ProductVariant();
            var1a.setId(100);
            var1a.setProduct(product);

            ProductVariant var1b = new ProductVariant();
            var1b.setId(101);
            var1b.setProduct(product);

            ProductVariant var1c = new ProductVariant();
            var1c.setId(102);
            var1c.setProduct(product);

            when(variantRepository.findByProductIds(List.of(1)))
                    .thenReturn(List.of(var1a, var1b, var1c));

            // ACT
            Map<Integer, List<ProductVariant>> result = variantService.getByProductIds(
                    List.of(1)
            );

            // ASSERT
            assertEquals(1, result.size());
            assertEquals(3, result.get(1).size());
            assertTrue(result.get(1).contains(var1a));
            assertTrue(result.get(1).contains(var1b));
            assertTrue(result.get(1).contains(var1c));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  deleteColor()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteColor()")
    class DeleteColor {

        @Test
        @DisplayName("should delete all variants for a product and color")
        void deleteColor_validProductAndColor_deletesCalled() {
            // ARRANGE — no mock return needed for delete
            // ACT
            variantService.deleteColor(1, "Red");

            // ASSERT
            verify(variantRepository, times(1)).deleteByProductIdAndColor(1, "Red");
        }

        @Test
        @DisplayName("should call repository exactly once")
        void deleteColor_callsRepositoryOnce() {
            // ARRANGE & ACT
            variantService.deleteColor(5, "Blue");

            // ASSERT
            verify(variantRepository, times(1)).deleteByProductIdAndColor(5, "Blue");
            verify(variantRepository, times(1)).deleteByProductIdAndColor(anyInt(), anyString());
        }

        @Test
        @DisplayName("should pass correct product id and color to repository")
        void deleteColor_passesCorrectParameters() {
            // ARRANGE & ACT
            variantService.deleteColor(42, "Purple");

            // ASSERT
            verify(variantRepository).deleteByProductIdAndColor(42, "Purple");
        }

        @Test
        @DisplayName("should not throw even if color does not exist")
        void deleteColor_nonexistentColor_noException() {
            // ARRANGE
            // ACT & ASSERT
            assertDoesNotThrow(() -> variantService.deleteColor(1, "NonExistent"));
            verify(variantRepository, times(1)).deleteByProductIdAndColor(1, "NonExistent");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  deleteByProductId()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteByProductId()")
    class DeleteByProductId {

        @Test
        @DisplayName("should delete all variants for a product")
        void deleteByProductId_validProductId_deletesCalled() {
            // ARRANGE & ACT
            variantService.deleteByProductId(1);

            // ASSERT
            verify(variantRepository, times(1)).deleteByProductId(1);
        }

        @Test
        @DisplayName("should call repository exactly once")
        void deleteByProductId_callsRepositoryOnce() {
            // ARRANGE & ACT
            variantService.deleteByProductId(10);

            // ASSERT
            verify(variantRepository, times(1)).deleteByProductId(10);
        }

        @Test
        @DisplayName("should pass correct product id to repository")
        void deleteByProductId_passesCorrectProductId() {
            // ARRANGE & ACT
            variantService.deleteByProductId(99);

            // ASSERT
            verify(variantRepository).deleteByProductId(99);
        }

        @Test
        @DisplayName("should not throw even if product has no variants")
        void deleteByProductId_noVariants_noException() {
            // ARRANGE & ACT & ASSERT
            assertDoesNotThrow(() -> variantService.deleteByProductId(999));
            verify(variantRepository, times(1)).deleteByProductId(999);
        }

        @Test
        @DisplayName("should delete all variants regardless of color or size")
        void deleteByProductId_deletesAllVariants() {
            // ARRANGE & ACT — note: with mock, we can't verify deletion actually happened,
            // but we verify the repository method was called with the product id
            variantService.deleteByProductId(1);

            // ASSERT — verify method was called to delete all variants for product 1
            verify(variantRepository).deleteByProductId(1);
        }
    }
}
