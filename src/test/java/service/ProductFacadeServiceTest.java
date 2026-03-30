package service;

import dto.*;
import entity.Product;
import entity.ProductImage;
import entity.ProductVariant;
import mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductFacadeServiceTest {

    // ── Mocks (fake objects we control) ──────────────────────────────────────

    @Mock private ProductService productService;
    @Mock private ProductVariantService variantService;
    @Mock private ProductImageService imageService;
    @Mock private ProductMapper productMapper;
    @Mock private CategoryService categoryService;
    @Mock private CartItemService cartItemService;

    // ── The real object under test (built manually for controlled dependencies) ─

    private ProductFacadeService facadeService;

    // ── Shared test data ──────────────────────────────────────────────────────

    private Product product1;
    private Product product2;
    private ProductVariant variant1;
    private ProductVariant variant2;
    private ProductImage image1;
    private ProductCardDTO cardDTO1;
    private ProductDetailDTO detailDTO1;

    @BeforeEach
    void setUp() {
        /*
         * Build ProductFacadeService using the constructor with all 5 dependencies.
         * Every test gets a fresh instance with fresh mocks.
         */
        facadeService = new ProductFacadeService(
                productService,
                variantService,
                imageService,
                productMapper,
                categoryService,
                cartItemService
        );

        // ── Product 1 ───────────────────────────────────────────────────────

        product1 = new Product();
        product1.setId(1);
        product1.setName("Basic T-Shirt");
        product1.setDescription("Comfortable cotton tshirt");
        product1.setBasePrice(BigDecimal.valueOf(149.99));
        product1.setImageUrl("tshirt.jpg");
        product1.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));  // counts as "new"

        // ── Product 2 ───────────────────────────────────────────────────────

        product2 = new Product();
        product2.setId(2);
        product2.setName("Classic Jeans");
        product2.setDescription("Durable denim jeans");
        product2.setBasePrice(BigDecimal.valueOf(499.99));
        product2.setImageUrl("jeans.jpg");
        product2.setCreatedAt(Instant.now().minus(60, ChronoUnit.DAYS));  // not "new"

        // ── Variants ────────────────────────────────────────────────────────

        variant1 = new ProductVariant();
        variant1.setId(10);
        variant1.setProduct(product1);
        variant1.setColor("Red");
        variant1.setSize("M");
        variant1.setQuantity(15);

        variant2 = new ProductVariant();
        variant2.setId(11);
        variant2.setProduct(product1);
        variant2.setColor("Blue");
        variant2.setSize("L");
        variant2.setQuantity(8);

        // ── Images ──────────────────────────────────────────────────────────

        image1 = new ProductImage();
        image1.setId(100);
        image1.setProduct(product1);
        image1.setColor("Red");
        image1.setImageUrl("/images/1/red/abc12345");

        // ── DTOs ────────────────────────────────────────────────────────────

        ProductCoreDTO coreDTOProduct1 = new ProductCoreDTO(1, "Basic T-Shirt", "Cotton Brand", 149.99,
                "/images/1/tshirt.jpg", "MALE", null, null);
        cardDTO1 = new ProductCardDTO(coreDTOProduct1, true, false, List.of(), Map.of());

        ProductCoreDTO coreDTOForDetail = new ProductCoreDTO(1, "Basic T-Shirt", "Cotton Brand", 149.99,
                "/images/1/tshirt.jpg", "MALE", null, null);
        detailDTO1 = new ProductDetailDTO(coreDTOForDetail, "Comfortable cotton tshirt",
                List.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getProducts()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getProducts()")
    class GetProducts {

        @Test
        @DisplayName("should fetch products with variants and images in batch")
        void getProducts_validFilter_returnsBatchedData() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDTO("MALE", null, false, false, 10);
            List<Product> products = List.of(product1, product2);

            when(productService.findFiltered(filter)).thenReturn(products);
            when(productService.countFiltered(filter)).thenReturn(2L);
            when(productService.getMinMaxPrice(filter))
                    .thenReturn(new PriceRangeDTO(149.99, 499.99));

            Map<Integer, List<ProductVariant>> variantsByProductId = Map.of(
                    1, List.of(variant1, variant2),
                    2, List.of()
            );
            when(variantService.getByProductIds(List.of(1, 2)))
                    .thenReturn(variantsByProductId);

            Map<Integer, List<ProductImage>> imagesByProductId = Map.of(
                    1, List.of(image1),
                    2, List.of()
            );
            when(imageService.getPrimaryPerColorForProducts(List.of(1, 2)))
                    .thenReturn(imagesByProductId);

            when(productMapper.toCardDTO(product1, List.of(variant1, variant2), List.of(image1)))
                    .thenReturn(cardDTO1);
            when(productMapper.toCardDTO(product2, List.of(), List.of()))
                    .thenReturn(new ProductCardDTO(
                            new ProductCoreDTO(2, "Classic Jeans", "Denim Brand", 499.99,
                                    "/images/2/jeans.jpg", "MALE", null, null),
                            false, false, List.of(), Map.of()));

            // ACT
            ProductListResult result = facadeService.getProducts(filter);

            // ASSERT
            assertNotNull(result);
            assertEquals(2, result.products().size());
            assertEquals(2L, result.total());
            assertEquals(149.99, result.priceRange().min());

            // Verify batch fetching (no N+1)
            verify(variantService, times(1)).getByProductIds(List.of(1, 2));
            verify(imageService, times(1)).getPrimaryPerColorForProducts(List.of(1, 2));
        }

        @Test
        @DisplayName("should use interest-based price range when filter has interests")
        void getProducts_interestBasedFilter_usesPropperPriceRange() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDTO(null, List.of(1, 2), false, true, 10);
            List<Product> products = List.of(product1);

            when(productService.findByInterests(filter)).thenReturn(products);
            when(productService.countByInterests(filter)).thenReturn(1L);
            when(productService.getMinMaxPriceForInterests(filter))
                    .thenReturn(new PriceRangeDTO(99.99, 199.99));

            when(variantService.getByProductIds(List.of(1))).thenReturn(Map.of(1, List.of(variant1)));
            when(imageService.getPrimaryPerColorForProducts(List.of(1))).thenReturn(Map.of(1, List.of(image1)));
            when(productMapper.toCardDTO(any(), any(), any())).thenReturn(cardDTO1);

            // ACT
            ProductListResult result = facadeService.getProducts(filter);

            // ASSERT — verify interest-based price range was used
            verify(productService, times(1)).getMinMaxPriceForInterests(filter);
            verify(productService, never()).getMinMaxPrice(filter);
        }

        @Test
        @DisplayName("should return empty list when no products match filter")
        void getProducts_noMatches_returnsEmpty() {
            // ARRANGE
            ProductFilterDTO filter = buildFilterDTO("FEMALE", null, false, false, 10);

            when(productService.findFiltered(filter)).thenReturn(List.of());
            when(productService.countFiltered(filter)).thenReturn(0L);
            when(productService.getMinMaxPrice(filter))
                    .thenReturn(new PriceRangeDTO(0.0, 0.0));

            when(variantService.getByProductIds(List.of())).thenReturn(Map.of());
            when(imageService.getPrimaryPerColorForProducts(List.of())).thenReturn(Map.of());

            // ACT
            ProductListResult result = facadeService.getProducts(filter);

            // ASSERT
            assertTrue(result.products().isEmpty());
            assertEquals(0L, result.total());
        }

        private ProductFilterDTO buildFilterDTO(String gender, List<Integer> categoryIds,
                                                 boolean newOnly, boolean interestBased, int pageSize) {
            return new ProductFilterDTO(
                    null,                  // searchQuery
                    gender,
                    categoryIds,
                    null,                  // minPrice
                    null,                  // maxPrice
                    newOnly,
                    interestBased ? List.of(1, 2) : null,
                    0,                     // page
                    pageSize
            );
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  searchProducts()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("searchProducts()")
    class SearchProducts {

        @Test
        @DisplayName("should search products and return search results")
        void searchProducts_validQuery_returnsSearchResults() {
            // ARRANGE
            ProductFilterDTO filter = new ProductFilterDTO(
                    "shirt", "MALE", null, null, null, null, null, 0, 10
            );
            List<Product> searchResults = List.of(product1);

            when(productService.searchFiltered(filter)).thenReturn(searchResults);
            when(productService.countSearchFiltered(filter)).thenReturn(1L);
            when(productService.getMinMaxPrice(filter))
                    .thenReturn(new PriceRangeDTO(149.99, 149.99));

            ProductSearchDTO searchDTO = new ProductSearchDTO(
                    new ProductCoreDTO(1, "Basic T-Shirt", "Cotton Brand", 149.99,
                            "/images/1/tshirt.jpg", "MALE", null, null));
            when(productMapper.toSearchDTO(product1)).thenReturn(searchDTO);

            // ACT
            ProductSearchResult result = facadeService.searchProducts(filter);

            // ASSERT
            assertNotNull(result);
            assertEquals(1, result.products().size());
            assertEquals(1L, result.total());
            verify(productMapper, times(1)).toSearchDTO(product1);
        }

        @Test
        @DisplayName("should return empty results when search matches nothing")
        void searchProducts_noMatches_returnsEmpty() {
            // ARRANGE
            ProductFilterDTO filter = new ProductFilterDTO(
                    "unicorn", null, null, null, null, null, null, 0, 10
            );

            when(productService.searchFiltered(filter)).thenReturn(List.of());
            when(productService.countSearchFiltered(filter)).thenReturn(0L);
            when(productService.getMinMaxPrice(filter))
                    .thenReturn(new PriceRangeDTO(0.0, 0.0));

            // ACT
            ProductSearchResult result = facadeService.searchProducts(filter);

            // ASSERT
            assertTrue(result.products().isEmpty());
            assertEquals(0L, result.total());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getProductDetail()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getProductDetail()")
    class GetProductDetail {

        @Test
        @DisplayName("should fetch product with all variants and images")
        void getProductDetail_validProductId_returnsDetail() {
            // ARRANGE
            when(productService.getById(1)).thenReturn(product1);
            when(variantService.getByProductId(1)).thenReturn(List.of(variant1, variant2));
            when(imageService.getByProductId(1)).thenReturn(List.of(image1));

            when(productMapper.toDetailDTO(product1, List.of(variant1, variant2), List.of(image1)))
                    .thenReturn(detailDTO1);

            // ACT
            ProductDetailDTO result = facadeService.getProductDetail(1);

            // ASSERT
            assertNotNull(result);
            assertEquals(1, result.core().id());
            assertEquals("Basic T-Shirt", result.core().name());
            verify(productService, times(1)).getById(1);
            verify(variantService, times(1)).getByProductId(1);
            verify(imageService, times(1)).getByProductId(1);
        }

        @Test
        @DisplayName("should handle product with no variants or images")
        void getProductDetail_noVariantsOrImages_returnsDetail() {
            // ARRANGE
            when(productService.getById(2)).thenReturn(product2);
            when(variantService.getByProductId(2)).thenReturn(List.of());
            when(imageService.getByProductId(2)).thenReturn(List.of());

            when(productMapper.toDetailDTO(product2, List.of(), List.of()))
                    .thenReturn(new ProductDetailDTO(
                            new ProductCoreDTO(2, "Classic Jeans", "Denim Brand", 499.99,
                                    "/images/2/jeans.jpg", "MALE", null, null),
                            null, List.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of()));

            // ACT
            ProductDetailDTO result = facadeService.getProductDetail(2);

            // ASSERT
            assertNotNull(result);
            assertEquals(2, result.core().id());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  deleteProduct()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProduct {

        @Test
        @DisplayName("should soft-delete product along with variants and images")
        void deleteProduct_validProductId_deletesSuccessfully() {
            // ARRANGE
            product1.getProductVariants().clear();
            product1.addProductVariant(variant1);
            product1.addProductVariant(variant2);
            when(productService.getById(1)).thenReturn(product1);

            // ACT
            boolean result = facadeService.deleteProduct(1);

            // ASSERT
            assertTrue(result);
            verify(cartItemService, times(1))
                    .deleteByVariantIds(List.of(variant1.getId(), variant2.getId()));
            verify(variantService, times(1)).softDeleteByProductId(1);
            verify(imageService, times(1)).deleteByProductId(1);
            verify(productService, times(1)).softDelete(1);
        }

        @Test
        @DisplayName("should delete cart items, variants, images, then product")
        void deleteProduct_deletionOrder_variantsAndImagesFirst() {
            // ARRANGE
            product1.getProductVariants().clear();
            product1.addProductVariant(variant1);
            product1.addProductVariant(variant2);
            when(productService.getById(1)).thenReturn(product1);

            // Create an order verifier
            InOrder inOrder = inOrder(cartItemService, variantService, imageService, productService);

            // ACT
            facadeService.deleteProduct(1);

            // ASSERT — verify deletion order
            inOrder.verify(cartItemService)
                    .deleteByVariantIds(List.of(variant1.getId(), variant2.getId()));
            inOrder.verify(variantService).softDeleteByProductId(1);
            inOrder.verify(imageService).deleteByProductId(1);
            inOrder.verify(productService).softDelete(1);
        }

        @Test
        @DisplayName("should return false when product does not exist")
        void deleteProduct_nonexistentProduct_returnsFalse() {
            // ARRANGE
            when(productService.getById(999)).thenReturn(null);

            // ACT
            boolean result = facadeService.deleteProduct(999);

            // ASSERT
            assertFalse(result);
            verify(cartItemService, never()).deleteByVariantIds(anyList());
            verify(variantService, never()).softDeleteByProductId(anyInt());
            verify(imageService, never()).deleteByProductId(anyInt());
            verify(productService, never()).softDelete(anyInt());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  deleteColor()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteColor()")
    class DeleteColor {

        @Test
        @DisplayName("should delete cart items, variants, and image for a color")
        void deleteColor_validProductAndColor_deletesVariantsAndImages() {
            // ARRANGE & ACT
            when(variantService.findIdsByColor(1, "Red")).thenReturn(List.of(10, 11));

            facadeService.deleteColor(1, "Red", "/var/www");

            // ASSERT
            verify(variantService, times(1)).findIdsByColor(1, "Red");
            verify(cartItemService, times(1)).deleteByVariantIds(List.of(10, 11));
            verify(variantService, times(1)).softDeleteColor(1, "Red");
            verify(imageService, times(1)).deleteColorImage(1, "Red", "/var/www");
        }

        @Test
        @DisplayName("should call services in correct order")
        void deleteColor_callOrder_variantsBeforeImages() {
            // ARRANGE
            when(variantService.findIdsByColor(1, "Blue")).thenReturn(List.of(12));
            InOrder inOrder = inOrder(variantService, cartItemService, imageService);

            // ACT
            facadeService.deleteColor(1, "Blue", "/var/www");

            // ASSERT
            inOrder.verify(variantService).findIdsByColor(1, "Blue");
            inOrder.verify(cartItemService).deleteByVariantIds(List.of(12));
            inOrder.verify(variantService).softDeleteColor(1, "Blue");
            inOrder.verify(imageService).deleteColorImage(1, "Blue", "/var/www");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  saveProduct()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveProduct()")
    class SaveProduct {

        @Test
        @DisplayName("should save new product and return its id")
        void saveProduct_validDTO_returnsSavedProductId() {
            // ARRANGE
            SaveProductDTO dto = new SaveProductDTO(
                    null, "New Product", null, 299.99, "image.jpg", "Description", List.of()
            );

            Product expectedProduct = new Product();
            expectedProduct.setId(100);
            expectedProduct.setName("New Product");

            when(productService.save(any(Product.class))).thenReturn(expectedProduct);

            // ACT
            Integer result = facadeService.saveProduct(dto);

            // ASSERT
            assertEquals(100, result);
            verify(productService, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("should apply DTO fields to Product before saving")
        void saveProduct_appliesDTOFields() {
            // ARRANGE
            SaveProductDTO dto = new SaveProductDTO(
                    null, "Test Product", 5, 199.99, "test.jpg", "Test Description", List.of()
            );

            Product savedProduct = new Product();
            savedProduct.setId(50);

            when(productService.save(any(Product.class))).thenReturn(savedProduct);
            when(categoryService.getById(5)).thenReturn(Optional.empty());

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

            // ACT
            facadeService.saveProduct(dto);

            // ASSERT
            verify(productService).save(captor.capture());
            Product captured = captor.getValue();

            assertAll(
                    "Product should have all DTO fields applied",
                    () -> assertEquals("Test Product", captured.getName()),
                    () -> assertEquals(BigDecimal.valueOf(199.99), captured.getBasePrice()),
                    () -> assertEquals("test.jpg", captured.getImageUrl()),
                    () -> assertEquals("Test Description", captured.getDescription())
            );
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  updateProduct()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateProduct()")
    class UpdateProduct {

        @Test
        @DisplayName("should update product and return admin detail")
        void updateProduct_validIdAndDTO_returnsUpdatedDetail() {
            // ARRANGE
            SaveProductDTO dto = new SaveProductDTO(
                    null, "Updated Product", null, 299.99, "updated.jpg", "Updated Description",
                    List.of()
            );

            when(productService.getById(1)).thenReturn(product1);
            when(productService.save(any(Product.class))).thenReturn(product1);
            when(variantService.getByProductId(1)).thenReturn(List.of());
            lenient().when(imageService.findByProductIdAndColor(eq(1), anyString()))
                    .thenReturn(List.of());
            // ACT
            AdminProductDetailDTO result = facadeService.updateProduct(1, dto);

            // ASSERT
            assertNotNull(result);
            verify(productService, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("should return null when product does not exist")
        void updateProduct_nonexistentProduct_returnsNull() {
            // ARRANGE
            SaveProductDTO dto = new SaveProductDTO(null, "Product", null, 100.0, "image.jpg", "Desc", List.of());

            when(productService.getById(999)).thenReturn(null);

            // ACT
            AdminProductDetailDTO result = facadeService.updateProduct(999, dto);

            // ASSERT
            assertNull(result);
            verify(productService, never()).save(any(Product.class));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getAdminProducts()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAdminProducts()")
    class GetAdminProducts {

        @Test
        @DisplayName("should return admin product list with variant info")
        void getAdminProducts_validFilter_returnsAdminList() {
            // ARRANGE
            ProductFilterDTO filter = new ProductFilterDTO(
                    null, "MALE", null, null, null, null, null, 0, 10
            );
            List<Product> products = List.of(product1);

            when(productService.findFiltered(filter)).thenReturn(products);
            when(productService.countFiltered(filter)).thenReturn(1L);
            when(variantService.getByProductIds(List.of(1)))
                    .thenReturn(Map.of(1, List.of(variant1, variant2)));

            AdminProductRowDTO rowDTO = new AdminProductRowDTO(
                    new ProductCoreDTO(1, "Basic T-Shirt", "Cotton Brand", 149.99,
                            "/images/1/tshirt.jpg", "MALE", null, null),
                    true, 2);
            when(productMapper.toAdminRowDTO(product1, List.of(variant1, variant2)))
                    .thenReturn(rowDTO);

            // ACT
            AdminProductListResult result = facadeService.getAdminProducts(filter);

            // ASSERT
            assertEquals(1, result.products().size());
            assertEquals(1L, result.total());
            assertNotNull(result.products().get(0));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  isNew()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isNew()")
    class IsNew {

        @Test
        @DisplayName("should return true for products less than 30 days old")
        void isNew_recentProduct_returnsTrue() {
            // ARRANGE
            Product newProduct = new Product();
            newProduct.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));

            // ACT
            boolean result = facadeService.isNew(newProduct);

            // ASSERT
            assertTrue(result);
        }

        @Test
        @DisplayName("should return false for products older than 30 days")
        void isNew_oldProduct_returnsFalse() {
            // ARRANGE
            Product oldProduct = new Product();
            oldProduct.setCreatedAt(Instant.now().minus(60, ChronoUnit.DAYS));

            // ACT
            boolean result = facadeService.isNew(oldProduct);

            // ASSERT
            assertFalse(result);
        }

        @Test
        @DisplayName("should return true for products exactly at 30 day boundary")
        void isNew_exactlyThirtyDays_returnsTrue() {
            // ARRANGE
            Product boundaryProduct = new Product();
            boundaryProduct.setCreatedAt(Instant.now().minus(29, ChronoUnit.DAYS).minus(23, ChronoUnit.HOURS));

            // ACT
            boolean result = facadeService.isNew(boundaryProduct);

            // ASSERT
            assertTrue(result);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getProductById()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getProductById()")
    class GetProductById {

        @Test
        @DisplayName("should delegate to productService.getById()")
        void getProductById_validId_returnsProduct() {
            // ARRANGE
            when(productService.getById(1)).thenReturn(product1);

            // ACT
            Product result = facadeService.getProductById(1);

            // ASSERT
            assertEquals(product1, result);
            verify(productService, times(1)).getById(1);
        }

        @Test
        @DisplayName("should return null when product does not exist")
        void getProductById_nonexistentId_returnsNull() {
            // ARRANGE
            when(productService.getById(999)).thenReturn(null);

            // ACT
            Product result = facadeService.getProductById(999);

            // ASSERT
            assertNull(result);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  saveColorImage()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveColorImage()")
    class SaveColorImage {

        @Test
        @DisplayName("should delegate to imageService.saveColorImage()")
        void saveColorImage_validInputs_delegatesToImageService() throws Exception {
            // ARRANGE
            InputStream mockStream = mock(InputStream.class);
            when(imageService.saveColorImage(product1, "Red", mockStream, "/var/www"))
                    .thenReturn("/images/1/red/abc123");

            // ACT
            String result = facadeService.saveColorImage(product1, "Red", mockStream, "/var/www");

            // ASSERT
            assertEquals("/images/1/red/abc123", result);
            verify(imageService, times(1)).saveColorImage(product1, "Red", mockStream, "/var/www");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  deleteColorImage()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteColorImage()")
    class DeleteColorImage {

        @Test
        @DisplayName("should delegate to imageService.deleteColorImage()")
        void deleteColorImage_validInputs_delegatesToImageService() {
            // ARRANGE & ACT
            facadeService.deleteColorImage(1, "Red", "/var/www");

            // ASSERT
            verify(imageService, times(1)).deleteColorImage(1, "Red", "/var/www");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  updateProductImage()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateProductImage()")
    class UpdateProductImage {

        @Test
        @DisplayName("should update product image URL and return old URL")
        void updateProductImage_validProductAndURL_returnsOldURL() {
            // ARRANGE
            Product productWithImage = new Product();
            productWithImage.setId(1);
            productWithImage.setImageUrl("/old/image.jpg");

            when(productService.getById(1)).thenReturn(productWithImage);
            when(productService.save(any(Product.class))).thenReturn(productWithImage);

            // ACT
            String result = facadeService.updateProductImage(1, "/new/image.jpg");

            // ASSERT
            assertEquals("/old/image.jpg", result);
            verify(productService, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("should return null when product does not exist")
        void updateProductImage_nonexistentProduct_returnsNull() {
            // ARRANGE
            when(productService.getById(999)).thenReturn(null);

            // ACT
            String result = facadeService.updateProductImage(999, "/new/image.jpg");

            // ASSERT
            assertNull(result);
            verify(productService, never()).save(any(Product.class));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getAdminDetail()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAdminDetail()")
    class GetAdminDetail {

        @Test
        @DisplayName("should build admin detail with stock by color")
        void getAdminDetail_validProductId_returnsDetailWithStockByColor() {
            // ARRANGE
            when(productService.getById(1)).thenReturn(product1);
            when(variantService.getByProductId(1)).thenReturn(List.of(variant1, variant2));
            when(imageService.getByProductId(1)).thenReturn(List.of(image1));

            ProductCoreDTO coreDTO = new ProductCoreDTO(1, "Basic T-Shirt", "Cotton Brand", 149.99,
                    "tshirt.jpg", "MALE", null, null);
            when(productMapper.toCoreDTO(product1)).thenReturn(coreDTO);

            // ACT
            AdminProductDetailDTO result = facadeService.getAdminDetail(1);

            // ASSERT
            assertNotNull(result);
            assertTrue(result.isNew());
            assertNotNull(result.stockByColor());
            assertTrue(result.stockByColor().containsKey("Red"));
            assertTrue(result.stockByColor().containsKey("Blue"));
        }

        @Test
        @DisplayName("should group variants by color")
        void getAdminDetail_multipleVariantsPerColor_groupsCorrectly() {
            // ARRANGE
            ProductVariant redVariant1 = new ProductVariant();
            redVariant1.setId(10);
            redVariant1.setColor("Red");
            redVariant1.setSize("S");
            redVariant1.setQuantity(5);

            ProductVariant redVariant2 = new ProductVariant();
            redVariant2.setId(11);
            redVariant2.setColor("Red");
            redVariant2.setSize("L");
            redVariant2.setQuantity(8);

            when(productService.getById(1)).thenReturn(product1);
            when(variantService.getByProductId(1)).thenReturn(List.of(redVariant1, redVariant2));
            when(imageService.getByProductId(1)).thenReturn(List.of(image1));

            ProductCoreDTO coreDTO = new ProductCoreDTO(1, "Product", "Test Brand", 100.0,
                    null, "MALE", null, null);
            when(productMapper.toCoreDTO(product1)).thenReturn(coreDTO);

            // ACT
            AdminProductDetailDTO result = facadeService.getAdminDetail(1);

            // ASSERT
            assertEquals(2, result.stockByColor().get("Red").size());
        }
    }
}
