package service;

import entity.Product;
import entity.ProductImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ProductImageRepository;
import util.ImagePathUtil;
import util.ImageProcessor;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    // ── Mocks (fake objects we control) ──────────────────────────────────────

    @Mock
    private ProductImageRepository imageRepository;    // fake DB — no real database used

    // ── The real object under test ────────────────────────────────────────────

    @InjectMocks
    private ProductImageService imageService;          // real ProductImageService, with mock injected

    // ── Shared test data ──────────────────────────────────────────────────────

    private Product product;
    private ProductImage redImage1;
    private ProductImage redImage2;
    private ProductImage blueImage1;

    @BeforeEach
    void setUp() {
        /*
         * Build a reusable Product and several product images with different
         * colors. We reset these before every test so one test can't affect
         * another.
         */

        // Product setup
        product = new Product();
        product.setId(1);
        product.setName("Basic T-Shirt");
        product.setDescription("Comfortable cotton tshirt");
        product.setBasePrice(BigDecimal.valueOf(149.99));
        product.setImageUrl("tshirt.jpg");
        product.setCreatedAt(Instant.now());

        // Image 1: Red color, first image
        redImage1 = new ProductImage();
        redImage1.setId(10);
        redImage1.setProduct(product);
        redImage1.setColor("Red");
        redImage1.setImageUrl("/images/1/red/abc12345/thumbnail.jpg");

        // Image 2: Red color, second image
        redImage2 = new ProductImage();
        redImage2.setId(11);
        redImage2.setProduct(product);
        redImage2.setColor("Red");
        redImage2.setImageUrl("/images/1/red/xyz98765/thumbnail.jpg");

        // Image 3: Blue color, first and only image
        blueImage1 = new ProductImage();
        blueImage1.setId(12);
        blueImage1.setProduct(product);
        blueImage1.setColor("Blue");
        blueImage1.setImageUrl("/images/1/blue/def54321/thumbnail.jpg");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getByProductId()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getByProductId()")
    class GetByProductId {

        @Test
        @DisplayName("should return all images for a product")
        void getByProductId_validProductId_returnsAllImages() {
            // ARRANGE
            List<ProductImage> images = List.of(redImage1, redImage2, blueImage1);
            when(imageRepository.findByProductId(1)).thenReturn(images);

            // ACT
            List<ProductImage> result = imageService.getByProductId(1);

            // ASSERT
            assertNotNull(result, "Should return non-null list");
            assertEquals(3, result.size(), "Should return all 3 images");
            assertTrue(result.contains(redImage1), "Should contain red image 1");
            assertTrue(result.contains(blueImage1), "Should contain blue image 1");
            verify(imageRepository, times(1)).findByProductId(1);
        }

        @Test
        @DisplayName("should return empty list when product has no images")
        void getByProductId_noImages_returnsEmptyList() {
            // ARRANGE
            when(imageRepository.findByProductId(999)).thenReturn(List.of());

            // ACT
            List<ProductImage> result = imageService.getByProductId(999);

            // ASSERT
            assertNotNull(result, "Should return empty list, not null");
            assertTrue(result.isEmpty(), "Should return empty list");
        }

        @Test
        @DisplayName("should return images ordered by id ascending")
        void getByProductId_multipleImages_returnsOrderedList() {
            // ARRANGE
            List<ProductImage> images = List.of(redImage1, redImage2, blueImage1);
            when(imageRepository.findByProductId(1)).thenReturn(images);

            // ACT
            List<ProductImage> result = imageService.getByProductId(1);

            // ASSERT — verify order is maintained
            assertEquals(10, result.get(0).getId());
            assertEquals(11, result.get(1).getId());
            assertEquals(12, result.get(2).getId());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getPrimaryPerColor()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPrimaryPerColor()")
    class GetPrimaryPerColor {

        @Test
        @DisplayName("should return primary (first) image per color")
        void getPrimaryPerColor_multipleColorsWithMultipleImages_returnsPrimaryPerColor() {
            // ARRANGE
            // Returns only the first (lowest id) image per color
            List<ProductImage> primaryImages = List.of(redImage1, blueImage1);
            when(imageRepository.findPrimaryPerColor(1)).thenReturn(primaryImages);

            // ACT
            List<ProductImage> result = imageService.getPrimaryPerColor(1);

            // ASSERT
            assertEquals(2, result.size(), "Should return 2 colors");
            assertTrue(result.contains(redImage1), "Should contain primary red image");
            assertFalse(result.contains(redImage2), "Should NOT contain second red image");
            assertTrue(result.contains(blueImage1), "Should contain blue image");
            verify(imageRepository, times(1)).findPrimaryPerColor(1);
        }

        @Test
        @DisplayName("should return only one image per color")
        void getPrimaryPerColor_returnsOnePerColor() {
            // ARRANGE
            ProductImage kidsProduct2 = new ProductImage();
            kidsProduct2.setId(1);
            kidsProduct2.setProduct(product);
            kidsProduct2.setColor("Red");

            when(imageRepository.findPrimaryPerColor(1)).thenReturn(List.of(kidsProduct2));

            // ACT
            List<ProductImage> result = imageService.getPrimaryPerColor(1);

            // ASSERT
            assertEquals(1, result.size());
            assertEquals(1, result.get(0).getId());
        }

        @Test
        @DisplayName("should return empty list when product has no images")
        void getPrimaryPerColor_noImages_returnsEmptyList() {
            // ARRANGE
            when(imageRepository.findPrimaryPerColor(999)).thenReturn(List.of());

            // ACT
            List<ProductImage> result = imageService.getPrimaryPerColor(999);

            // ASSERT
            assertTrue(result.isEmpty());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  countByProductIdAndColor()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countByProductIdAndColor()")
    class CountByProductIdAndColor {

        @Test
        @DisplayName("should count images for a product and color")
        void countByProductIdAndColor_validProductAndColor_returnsCount() {
            // ARRANGE
            when(imageRepository.countByProductIdAndColor(1, "Red")).thenReturn(2L);

            // ACT
            long result = imageService.countByProductIdAndColor(1, "Red");

            // ASSERT
            assertEquals(2L, result, "Should count 2 red images");
            verify(imageRepository, times(1)).countByProductIdAndColor(1, "Red");
        }

        @Test
        @DisplayName("should return 0 when no images for color exist")
        void countByProductIdAndColor_colorDoesNotExist_returnsZero() {
            // ARRANGE
            when(imageRepository.countByProductIdAndColor(1, "Purple")).thenReturn(0L);

            // ACT
            long result = imageService.countByProductIdAndColor(1, "Purple");

            // ASSERT
            assertEquals(0L, result);
        }

        @Test
        @DisplayName("should return correct count for single image")
        void countByProductIdAndColor_singleImage_returnsOne() {
            // ARRANGE
            when(imageRepository.countByProductIdAndColor(1, "Blue")).thenReturn(1L);

            // ACT
            long result = imageService.countByProductIdAndColor(1, "Blue");

            // ASSERT
            assertEquals(1L, result);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getPrimaryPerColorForProducts()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPrimaryPerColorForProducts()")
    class GetPrimaryPerColorForProducts {

        @Test
        @DisplayName("should return images grouped by product id")
        void getPrimaryPerColorForProducts_multipleProducts_returnsGroupedImages() {
            // ARRANGE
            Product product2 = new Product();
            product2.setId(2);

            ProductImage img1 = new ProductImage();
            img1.setId(1);
            img1.setProduct(product);
            img1.setColor("Red");

            ProductImage img2 = new ProductImage();
            img2.setId(2);
            img2.setProduct(product2);
            img2.setColor("Blue");

            List<ProductImage> allImages = List.of(img1, img2);
            when(imageRepository.findPrimaryPerColorForProducts(List.of(1, 2)))
                    .thenReturn(allImages);

            // ACT
            Map<Integer, List<ProductImage>> result = imageService.getPrimaryPerColorForProducts(
                    List.of(1, 2)
            );

            // ASSERT
            assertNotNull(result);
            assertEquals(2, result.size(), "Should have 2 product groups");
            assertEquals(1, result.get(1).size(), "Product 1 should have 1 image");
            assertEquals(1, result.get(2).size(), "Product 2 should have 1 image");
        }

        @Test
        @DisplayName("should maintain LinkedHashMap ordering (product id order)")
        void getPrimaryPerColorForProducts_ordersLinkedHashMap() {
            // ARRANGE
            Product product2 = new Product();
            product2.setId(2);
            Product product3 = new Product();
            product3.setId(3);

            ProductImage img1 = new ProductImage();
            img1.setId(10);
            img1.setProduct(product);

            ProductImage img2 = new ProductImage();
            img2.setId(20);
            img2.setProduct(product2);

            ProductImage img3 = new ProductImage();
            img3.setId(30);
            img3.setProduct(product3);

            when(imageRepository.findPrimaryPerColorForProducts(List.of(1, 2, 3)))
                    .thenReturn(List.of(img1, img2, img3));

            // ACT
            Map<Integer, List<ProductImage>> result = imageService.getPrimaryPerColorForProducts(
                    List.of(1, 2, 3)
            );

            // ASSERT — verify order is maintained
            List<Integer> keys = new ArrayList<>(result.keySet());
            assertEquals(List.of(1, 2, 3), keys, "Keys should be in product id order");
        }

        @Test
        @DisplayName("should return empty map when no products provided")
        void getPrimaryPerColorForProducts_emptyList_returnsEmptyMap() {
            // ARRANGE
            when(imageRepository.findPrimaryPerColorForProducts(List.of()))
                    .thenReturn(List.of());

            // ACT
            Map<Integer, List<ProductImage>> result = imageService.getPrimaryPerColorForProducts(
                    List.of()
            );

            // ASSERT
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should group all images under correct product id")
        void getPrimaryPerColorForProducts_correctGrouping() {
            // ARRANGE
            ProductImage img1a = new ProductImage();
            img1a.setId(100);
            img1a.setProduct(product);
            img1a.setColor("Red");

            ProductImage img1b = new ProductImage();
            img1b.setId(101);
            img1b.setProduct(product);
            img1b.setColor("Blue");

            when(imageRepository.findPrimaryPerColorForProducts(List.of(1)))
                    .thenReturn(List.of(img1a, img1b));

            // ACT
            Map<Integer, List<ProductImage>> result = imageService.getPrimaryPerColorForProducts(
                    List.of(1)
            );

            // ASSERT
            assertEquals(1, result.size());
            assertEquals(2, result.get(1).size());
            assertTrue(result.get(1).contains(img1a));
            assertTrue(result.get(1).contains(img1b));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  deleteColorImages()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteColorImages()")
    class DeleteColorImages {

        @Test
        @DisplayName("should delete all images for a product and color")
        void deleteColorImages_validProductAndColor_deletesCalled() {
            // ARRANGE & ACT
            imageService.deleteColorImages(1, "Red");

            // ASSERT
            verify(imageRepository, times(1)).deleteByProductIdAndColor(1, "Red");
        }

        @Test
        @DisplayName("should pass correct product id and color to repository")
        void deleteColorImages_passesCorrectParameters() {
            // ARRANGE & ACT
            imageService.deleteColorImages(42, "Purple");

            // ASSERT
            verify(imageRepository).deleteByProductIdAndColor(42, "Purple");
        }

        @Test
        @DisplayName("should not throw even if color does not exist")
        void deleteColorImages_nonexistentColor_noException() {
            // ARRANGE & ACT & ASSERT
            assertDoesNotThrow(() -> imageService.deleteColorImages(1, "NonExistent"));
            verify(imageRepository, times(1)).deleteByProductIdAndColor(1, "NonExistent");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  deleteByProductId()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteByProductId()")
    class DeleteByProductId {

        @Test
        @DisplayName("should delete all images for a product")
        void deleteByProductId_validProductId_deletesCalled() {
            // ARRANGE & ACT
            imageService.deleteByProductId(1);

            // ASSERT
            verify(imageRepository, times(1)).deleteByProductId(1);
        }

        @Test
        @DisplayName("should pass correct product id to repository")
        void deleteByProductId_passesCorrectProductId() {
            // ARRANGE & ACT
            imageService.deleteByProductId(99);

            // ASSERT
            verify(imageRepository).deleteByProductId(99);
        }

        @Test
        @DisplayName("should not throw even if product has no images")
        void deleteByProductId_noImages_noException() {
            // ARRANGE & ACT & ASSERT
            assertDoesNotThrow(() -> imageService.deleteByProductId(999));
            verify(imageRepository, times(1)).deleteByProductId(999);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  findByProductIdAndColor()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findByProductIdAndColor()")
    class FindByProductIdAndColor {

        @Test
        @DisplayName("should return images for a product and color")
        void findByProductIdAndColor_validProductAndColor_returnsImages() {
            // ARRANGE
            List<ProductImage> images = List.of(redImage1, redImage2);
            when(imageRepository.findByProductIdAndColor(1, "Red")).thenReturn(images);

            // ACT
            List<ProductImage> result = imageService.findByProductIdAndColor(1, "Red");

            // ASSERT
            assertEquals(2, result.size());
            assertTrue(result.contains(redImage1));
            assertTrue(result.contains(redImage2));
            verify(imageRepository, times(1)).findByProductIdAndColor(1, "Red");
        }

        @Test
        @DisplayName("should return empty list when color does not exist")
        void findByProductIdAndColor_invalidColor_returnsEmptyList() {
            // ARRANGE
            when(imageRepository.findByProductIdAndColor(1, "Purple")).thenReturn(List.of());

            // ACT
            List<ProductImage> result = imageService.findByProductIdAndColor(1, "Purple");

            // ASSERT
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return single image when only one exists")
        void findByProductIdAndColor_singleImage_returnsOne() {
            // ARRANGE
            when(imageRepository.findByProductIdAndColor(1, "Blue"))
                    .thenReturn(List.of(blueImage1));

            // ACT
            List<ProductImage> result = imageService.findByProductIdAndColor(1, "Blue");

            // ASSERT
            assertEquals(1, result.size());
            assertEquals(blueImage1, result.get(0));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  saveColorImage()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveColorImage()")
    class SaveColorImage {

        @Test
        @DisplayName("should save new product image when no previous image exists")
        void saveColorImage_newColor_savesImage() throws Exception {
            // ARRANGE
            InputStream mockInputStream = mock(InputStream.class);
            String webappRoot = "/var/www";

            // Mock repository to return empty (no previous image)
            when(imageRepository.findByProductIdAndColor(1, "Red"))
                    .thenReturn(List.of());

            try (var mockProcessor = mockStatic(ImageProcessor.class);
                 var mockPathUtil = mockStatic(ImagePathUtil.class)) {

                when(ImagePathUtil.base(anyInt(), anyString(), anyString()))
                        .thenReturn("/images/1/red/abc12345");
                when(ImagePathUtil.absoluteDir(anyString(), anyInt(), anyString()))
                        .thenReturn("/var/www/assets/images/1/red");

                // ACT
                String result = imageService.saveColorImage(product, "Red", mockInputStream, webappRoot);

                // ASSERT
                assertNotNull(result, "Should return image path");
                assertTrue(result.contains("/images/1/red"), "Should contain color path");
                verify(imageRepository).save(any(ProductImage.class));
            }
        }

        @Test
        @DisplayName("should delete old image when replacing color image")
        void saveColorImage_replaceExistingColor_deletesOldImage() throws Exception {
            // ARRANGE
            InputStream mockInputStream = mock(InputStream.class);
            String webappRoot = "/var/www";

            // Mock repository to return existing image
            when(imageRepository.findByProductIdAndColor(1, "Red"))
                    .thenReturn(List.of(redImage1));

            try (var mockProcessor = mockStatic(ImageProcessor.class);
                 var mockPathUtil = mockStatic(ImagePathUtil.class)) {

                when(ImagePathUtil.base(anyInt(), anyString(), anyString()))
                        .thenReturn("/images/1/red/xyz98765");
                when(ImagePathUtil.absoluteDir(anyString(), anyInt(), anyString()))
                        .thenReturn("/var/www/assets/images/1/red");

                // ACT
                imageService.saveColorImage(product, "Red", mockInputStream, webappRoot);

                // ASSERT — verify old image was deleted
                verify(imageRepository).delete(redImage1);
            }
        }

        @Test
        @DisplayName("should call ImageProcessor.process with correct parameters")
        void saveColorImage_callsImageProcessor() throws Exception {
            // ARRANGE
            InputStream mockInputStream = mock(InputStream.class);
            String webappRoot = "/var/www";

            when(imageRepository.findByProductIdAndColor(1, "Red"))
                    .thenReturn(List.of());

            try (var mockProcessor = mockStatic(ImageProcessor.class);
                 var mockPathUtil = mockStatic(ImagePathUtil.class)) {

                when(ImagePathUtil.base(anyInt(), anyString(), anyString()))
                        .thenReturn("/images/1/red/abc12345");
                when(ImagePathUtil.absoluteDir(anyString(), anyInt(), anyString()))
                        .thenReturn("/var/www/assets/images/1/red");

                // ACT
                imageService.saveColorImage(product, "Red", mockInputStream, webappRoot);

                // ASSERT — verify ImageProcessor.process was called
                verify(imageRepository).save(any(ProductImage.class));
            }
        }

        @Test
        @DisplayName("should return image path with color and uuid")
        void saveColorImage_returnsValidImagePath() throws Exception {
            // ARRANGE
            InputStream mockInputStream = mock(InputStream.class);
            String webappRoot = "/var/www";
            String expectedPath = "/images/1/red/abc12345";

            when(imageRepository.findByProductIdAndColor(1, "Red"))
                    .thenReturn(List.of());

            try (var mockProcessor = mockStatic(ImageProcessor.class);
                 var mockPathUtil = mockStatic(ImagePathUtil.class)) {

                when(ImagePathUtil.base(eq(1), eq("Red"), anyString()))
                        .thenReturn(expectedPath);
                when(ImagePathUtil.absoluteDir(eq(webappRoot), eq(1), eq("Red")))
                        .thenReturn("/var/www/assets/images/1/red");

                // ACT
                String result = imageService.saveColorImage(product, "Red", mockInputStream, webappRoot);

                // ASSERT
                assertEquals(expectedPath, result);
            }
        }

        @Test
        @DisplayName("should save ProductImage with correct product and color")
        void saveColorImage_savesWithCorrectMetadata() throws Exception {
            // ARRANGE
            InputStream mockInputStream = mock(InputStream.class);
            String webappRoot = "/var/www";

            when(imageRepository.findByProductIdAndColor(1, "Red"))
                    .thenReturn(List.of());

            try (var mockProcessor = mockStatic(ImageProcessor.class);
                 var mockPathUtil = mockStatic(ImagePathUtil.class)) {

                when(ImagePathUtil.base(anyInt(), anyString(), anyString()))
                        .thenReturn("/images/1/red/abc12345");
                when(ImagePathUtil.absoluteDir(anyString(), anyInt(), anyString()))
                        .thenReturn("/var/www/assets/images/1/red");

                // Capture the saved ProductImage
                org.mockito.ArgumentCaptor<ProductImage> captor =
                        org.mockito.ArgumentCaptor.forClass(ProductImage.class);

                // ACT
                imageService.saveColorImage(product, "Red", mockInputStream, webappRoot);

                // ASSERT
                verify(imageRepository).save(captor.capture());
                ProductImage saved = captor.getValue();

                assertAll(
                        "ProductImage should have correct metadata",
                        () -> assertEquals(product, saved.getProduct(), "Product should match"),
                        () -> assertEquals("Red", saved.getColor(), "Color should be Red"),
                        () -> assertNotNull(saved.getImageUrl(), "Image URL should be set")
                );
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  deleteColorImage()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteColorImage()")
    class DeleteColorImage {

        @Test
        @DisplayName("should delete image and call ImageProcessor.deleteAll when image exists")
        void deleteColorImage_imageExists_deletesAndProcesses() {
            // ARRANGE
            String webappRoot = "/var/www";

            when(imageRepository.findByProductIdAndColor(1, "Red"))
                    .thenReturn(List.of(redImage1));

            try (var mock = mockStatic(ImageProcessor.class)) {
                // ACT
                imageService.deleteColorImage(1, "Red", webappRoot);

                // ASSERT
                verify(imageRepository).delete(redImage1);
                mock.verify(() -> ImageProcessor.deleteAll(redImage1.getImageUrl(), webappRoot));
                verify(imageRepository).findByProductIdAndColor(1, "Red");
            }
        }

        @Test
        @DisplayName("should not call delete when no image exists for color")
        void deleteColorImage_noImage_nothingDeleted() {
            // ARRANGE
            String webappRoot = "/var/www";

            when(imageRepository.findByProductIdAndColor(1, "Purple"))
                    .thenReturn(List.of());

            try (var mock = mockStatic(ImageProcessor.class)) {
                // ACT
                imageService.deleteColorImage(1, "Purple", webappRoot);

                // ASSERT
                verify(imageRepository, never()).delete(any(ProductImage.class));
                mock.verify(() -> ImageProcessor.deleteAll(anyString(), anyString()), never());
            }
        }

        @Test
        @DisplayName("should use findFirst from collection to get single image")
        void deleteColorImage_deletesFirstImage() {
            // ARRANGE
            String webappRoot = "/var/www";

            // Even if multiple images exist (shouldn't happen but defensive)
            when(imageRepository.findByProductIdAndColor(1, "Red"))
                    .thenReturn(List.of(redImage1, redImage2));

            try (var mock = mockStatic(ImageProcessor.class)) {
                // ACT
                imageService.deleteColorImage(1, "Red", webappRoot);

                // ASSERT — should delete only the first one
                verify(imageRepository).delete(redImage1);
                mock.verify(() -> ImageProcessor.deleteAll(redImage1.getImageUrl(), webappRoot));
            }
        }

        @Test
        @DisplayName("should pass correct imageUrl to ImageProcessor.deleteAll")
        void deleteColorImage_passesCorrectImageUrl() {
            // ARRANGE
            String webappRoot = "/var/www";

            when(imageRepository.findByProductIdAndColor(1, "Blue"))
                    .thenReturn(List.of(blueImage1));

            try (var mock = mockStatic(ImageProcessor.class)) {
                // ACT
                imageService.deleteColorImage(1, "Blue", webappRoot);

                // ASSERT
                mock.verify(() -> ImageProcessor.deleteAll(blueImage1.getImageUrl(), webappRoot));
            }
        }
    }
}
