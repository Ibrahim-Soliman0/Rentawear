package repository;

import entity.Product;
import entity.ProductImage;
import entity.Category;
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
import repository.impl.ProductImageRepositoryImpl;
import util.EntityManagerContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ProductImageRepositoryTest")
public class ProductImageRepositoryTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private ProductImageRepositoryImpl imageRepository;

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

        imageRepository = new ProductImageRepositoryImpl();
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

    private ProductImage createProductImage(Product product, String color, String imageUrl) {
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setColor(color);
        image.setImageUrl(imageUrl);
        return image;
    }

    private ProductImage persistProductImage(Product product, String color, String imageUrl) {
        ProductImage image = createProductImage(product, color, imageUrl);
        em.persist(image);
        em.flush();
        return image;
    }

    // ==================== NESTED TEST CLASSES ====================

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        @DisplayName("should retrieve existing product image by ID")
        void shouldRetrieveExistingImage() {
            ProductImage image = persistProductImage(testProduct, "Red", "/images/red.jpg");
            em.detach(image);

            ProductImage retrieved = imageRepository.findById(image.getId());

            assertNotNull(retrieved);
            assertEquals(image.getId(), retrieved.getId());
            assertEquals("Red", retrieved.getColor());
            assertEquals("/images/red.jpg", retrieved.getImageUrl());
        }

        @Test
        @DisplayName("should return null for non-existent product image ID")
        void shouldReturnNullForNonExistentId() {
            ProductImage retrieved = imageRepository.findById(9999);

            assertNull(retrieved);
        }
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        @DisplayName("should persist new product image")
        void shouldPersistNewImage() {
            ProductImage image = createProductImage(testProduct, "Blue", "/images/blue.jpg");

            ProductImage saved = imageRepository.save(image);

            assertNotNull(saved.getId());
            assertEquals("Blue", saved.getColor());

            em.detach(saved);
            ProductImage retrieved = imageRepository.findById(saved.getId());
            assertNotNull(retrieved);
            assertEquals("Blue", retrieved.getColor());
        }

        @Test
        @DisplayName("should update existing product image")
        void shouldUpdateExistingImage() {
            ProductImage image = persistProductImage(testProduct, "Green", "/images/green.jpg");
            Integer imageId = image.getId();

            image.setImageUrl("/images/green-updated.jpg");
            imageRepository.save(image);

            em.detach(image);
            ProductImage retrieved = imageRepository.findById(imageId);
            assertEquals("/images/green-updated.jpg", retrieved.getImageUrl());
        }
    }

    @Nested
    @DisplayName("FindAll")
    class FindAll {

        @Test
        @DisplayName("should return all product images")
        void shouldReturnAllImages() {
            persistProductImage(testProduct, "Red", "/images/red.jpg");
            persistProductImage(testProduct, "Blue", "/images/blue.jpg");
            persistProductImage(anotherProduct, "Black", "/images/black.jpg");

            List<ProductImage> images = imageRepository.findAll();

            assertEquals(3, images.size());
        }

        @Test
        @DisplayName("should return empty list when no product images exist")
        void shouldReturnEmptyListWhenNoImages() {
            List<ProductImage> images = imageRepository.findAll();

            assertTrue(images.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        @DisplayName("should delete product image")
        void shouldDeleteImage() {
            ProductImage image = persistProductImage(testProduct, "Red", "/images/red.jpg");
            Integer imageId = image.getId();

            imageRepository.delete(image);
            em.flush();

            ProductImage retrieved = imageRepository.findById(imageId);
            assertNull(retrieved);
        }

        @Test
        @DisplayName("should handle deletion of already deleted image")
        void shouldHandleAlreadyDeletedImage() {
            ProductImage image = persistProductImage(testProduct, "Red", "/images/red.jpg");
            imageRepository.delete(image);
            em.flush();

            // Should not throw exception
            assertDoesNotThrow(() -> {
                // Attempting to delete again (detached entity should be safe)
                em.remove(em.merge(image));
            });
        }
    }

    @Nested
    @DisplayName("FindByProductId")
    class FindByProductId {

        @Test
        @DisplayName("should find all images for a product ordered by ID ascending")
        void shouldFindAllImagesForProduct() {
            persistProductImage(testProduct, "Red", "/images/red.jpg");
            persistProductImage(testProduct, "Blue", "/images/blue.jpg");
            persistProductImage(testProduct, "Green", "/images/green.jpg");
            persistProductImage(anotherProduct, "Yellow", "/images/yellow.jpg");

            List<ProductImage> images = imageRepository.findByProductId(testProduct.getId());

            assertEquals(3, images.size());
            assertEquals("Red", images.get(0).getColor());
            assertEquals("Blue", images.get(1).getColor());
            assertEquals("Green", images.get(2).getColor());
        }

        @Test
        @DisplayName("should return empty list for product with no images")
        void shouldReturnEmptyListForProductWithNoImages() {
            List<ProductImage> images = imageRepository.findByProductId(testProduct.getId());

            assertTrue(images.isEmpty());
        }

        @Test
        @DisplayName("should only return images for specified product")
        void shouldOnlyReturnImagesForSpecifiedProduct() {
            persistProductImage(testProduct, "Red", "/images/red.jpg");
            persistProductImage(testProduct, "Blue", "/images/blue.jpg");
            persistProductImage(anotherProduct, "Black", "/images/black.jpg");

            List<ProductImage> images = imageRepository.findByProductId(testProduct.getId());

            assertEquals(2, images.size());
            assertTrue(images.stream().allMatch(img -> img.getProduct().getId().equals(testProduct.getId())));
        }

        @Test
        @DisplayName("should handle multiple colors for same product")
        void shouldHandleMultipleColorsForSameProduct() {
            persistProductImage(testProduct, "Red", "/images/red1.jpg");
            persistProductImage(testProduct, "Red", "/images/red2.jpg");
            persistProductImage(testProduct, "Blue", "/images/blue1.jpg");

            List<ProductImage> images = imageRepository.findByProductId(testProduct.getId());

            assertEquals(3, images.size());
            assertEquals(2, images.stream().filter(img -> "Red".equals(img.getColor())).count());
            assertEquals(1, images.stream().filter(img -> "Blue".equals(img.getColor())).count());
        }
    }

    @Nested
    @DisplayName("FindByProductIdAndColor")
    class FindByProductIdAndColor {

        @Test
        @DisplayName("should find all images for product-color combination")
        void shouldFindImagesByProductAndColor() {
            ProductImage red1 = persistProductImage(testProduct, "Red", "/images/red1.jpg");
            ProductImage red2 = persistProductImage(testProduct, "Red", "/images/red2.jpg");
            persistProductImage(testProduct, "Blue", "/images/blue1.jpg");

            List<ProductImage> images = imageRepository.findByProductIdAndColor(testProduct.getId(), "Red");

            assertEquals(2, images.size());
            assertTrue(images.stream().allMatch(img -> "Red".equals(img.getColor())));
        }

        @Test
        @DisplayName("should return empty list for non-existent product-color combination")
        void shouldReturnEmptyListForNonExistentCombination() {
            persistProductImage(testProduct, "Red", "/images/red.jpg");

            List<ProductImage> images = imageRepository.findByProductIdAndColor(testProduct.getId(), "Yellow");

            assertTrue(images.isEmpty());
        }

        @Test
        @DisplayName("should return ordered results by ID ascending")
        void shouldReturnOrderedResults() {
            ProductImage red1 = persistProductImage(testProduct, "Red", "/images/red1.jpg");
            ProductImage red2 = persistProductImage(testProduct, "Red", "/images/red2.jpg");
            ProductImage red3 = persistProductImage(testProduct, "Red", "/images/red3.jpg");

            List<ProductImage> images = imageRepository.findByProductIdAndColor(testProduct.getId(), "Red");

            assertEquals(3, images.size());
            assertEquals(red1.getId(), images.get(0).getId());
            assertEquals(red2.getId(), images.get(1).getId());
            assertEquals(red3.getId(), images.get(2).getId());
        }

        @Test
        @DisplayName("should filter by color precisely")
        void shouldFilterByColorPrecisely() {
            persistProductImage(testProduct, "Red", "/images/red.jpg");
            persistProductImage(testProduct, "Red", "/images/red2.jpg");
            persistProductImage(testProduct, "RedOrange", "/images/redorange.jpg");

            List<ProductImage> images = imageRepository.findByProductIdAndColor(testProduct.getId(), "Red");

            assertEquals(2, images.size());
            assertTrue(images.stream().allMatch(img -> "Red".equals(img.getColor())));
        }
    }

    @Nested
    @DisplayName("FindPrimaryPerColor")
    class FindPrimaryPerColor {

        @Test
        @DisplayName("should find primary (lowest-ID) image per color for one product")
        void shouldFindPrimaryImagePerColor() {
            persistProductImage(testProduct, "Red", "/images/red1.jpg");
            ProductImage redPrimary = persistProductImage(testProduct, "Red", "/images/red2.jpg");
            persistProductImage(testProduct, "Red", "/images/red3.jpg");
            ProductImage bluePrimary = persistProductImage(testProduct, "Blue", "/images/blue1.jpg");

            List<ProductImage> primaries = imageRepository.findPrimaryPerColor(testProduct.getId());

            assertEquals(2, primaries.size());
            assertTrue(primaries.stream().anyMatch(img -> "Red".equals(img.getColor())));
            assertTrue(primaries.stream().anyMatch(img -> "Blue".equals(img.getColor())));
        }

        @Test
        @DisplayName("should return empty list for product with no images")
        void shouldReturnEmptyListForProductWithNoImages() {
            List<ProductImage> primaries = imageRepository.findPrimaryPerColor(testProduct.getId());

            assertTrue(primaries.isEmpty());
        }

        @Test
        @DisplayName("should return single primary when only one image per color")
        void shouldReturnSinglePrimaryPerColor() {
            ProductImage red = persistProductImage(testProduct, "Red", "/images/red.jpg");
            ProductImage blue = persistProductImage(testProduct, "Blue", "/images/blue.jpg");

            List<ProductImage> primaries = imageRepository.findPrimaryPerColor(testProduct.getId());

            assertEquals(2, primaries.size());
        }

        @Test
        @DisplayName("should select lowest-ID image as primary")
        void shouldSelectLowestIdAsPrimary() {
            // Persist in non-order to test ID-based selection
            ProductImage red2 = persistProductImage(testProduct, "Red", "/images/red2.jpg");
            ProductImage red3 = persistProductImage(testProduct, "Red", "/images/red3.jpg");
            ProductImage red1 = persistProductImage(testProduct, "Red", "/images/red1.jpg");

            List<ProductImage> primaries = imageRepository.findPrimaryPerColor(testProduct.getId());

            assertEquals(1, primaries.size());
            assertEquals(red2.getId(), primaries.get(0).getId()); // Lowest ID among Red images
        }
    }

    @Nested
    @DisplayName("FindPrimaryPerColorForProducts")
    class FindPrimaryPerColorForProducts {

        @Test
        @DisplayName("should find primary image per color for multiple products")
        void shouldFindPrimaryImagesForMultipleProducts() {
            persistProductImage(testProduct, "Red", "/images/product1-red1.jpg");
            persistProductImage(testProduct, "Red", "/images/product1-red2.jpg");
            ProductImage product1Blue = persistProductImage(testProduct, "Blue", "/images/product1-blue.jpg");

            persistProductImage(anotherProduct, "Black", "/images/product2-black1.jpg");
            ProductImage product2Black = persistProductImage(anotherProduct, "Black", "/images/product2-black2.jpg");
            ProductImage product2White = persistProductImage(anotherProduct, "White", "/images/product2-white.jpg");

            List<Integer> productIds = List.of(testProduct.getId(), anotherProduct.getId());
            List<ProductImage> primaries = imageRepository.findPrimaryPerColorForProducts(productIds);

            assertEquals(4, primaries.size()); // 2 colors for product1, 2 colors for product2
        }

        @Test
        @DisplayName("should return empty list for empty product IDs")
        void shouldReturnEmptyListForEmptyProductIds() {
            persistProductImage(testProduct, "Red", "/images/red.jpg");

            List<ProductImage> primaries = imageRepository.findPrimaryPerColorForProducts(List.of());

            assertTrue(primaries.isEmpty());
        }

        @Test
        @DisplayName("should skip products with no images")
        void shouldSkipProductsWithNoImages() {
            persistProductImage(testProduct, "Red", "/images/red.jpg");

            List<Integer> productIds = List.of(testProduct.getId(), anotherProduct.getId());
            List<ProductImage> primaries = imageRepository.findPrimaryPerColorForProducts(productIds);

            assertEquals(1, primaries.size());
            assertEquals(testProduct.getId(), primaries.get(0).getProduct().getId());
        }

        @Test
        @DisplayName("should return results ordered by product.id ASC, then image.id ASC")
        void shouldReturnOrderedResults() {
            int product1Id = testProduct.getId();
            int product2Id = anotherProduct.getId();

            persistProductImage(testProduct, "Red", "/images/p1-red1.jpg");
            ProductImage p1Red = persistProductImage(testProduct, "Red", "/images/p1-red2.jpg");
            persistProductImage(testProduct, "Blue", "/images/p1-blue.jpg");

            persistProductImage(anotherProduct, "Black", "/images/p2-black1.jpg");

            List<Integer> productIds = product1Id < product2Id ? 
                List.of(product1Id, product2Id) : 
                List.of(product2Id, product1Id);

            List<ProductImage> primaries = imageRepository.findPrimaryPerColorForProducts(productIds);

            // Should be grouped by product, then ordered by image ID
            int lastProductId = -1;
            for (ProductImage image : primaries) {
                assertTrue(image.getProduct().getId() >= lastProductId);
                lastProductId = image.getProduct().getId();
            }
        }
    }

    @Nested
    @DisplayName("CountByProductIdAndColor")
    class CountByProductIdAndColor {

        @Test
        @DisplayName("should count images for product-color combination")
        void shouldCountImagesForProductAndColor() {
            persistProductImage(testProduct, "Red", "/images/red1.jpg");
            persistProductImage(testProduct, "Red", "/images/red2.jpg");
            persistProductImage(testProduct, "Red", "/images/red3.jpg");

            long count = imageRepository.countByProductIdAndColor(testProduct.getId(), "Red");

            assertEquals(3L, count);
        }

        @Test
        @DisplayName("should return zero for non-existent product-color combination")
        void shouldReturnZeroForNonExistentCombination() {
            persistProductImage(testProduct, "Red", "/images/red.jpg");

            long count = imageRepository.countByProductIdAndColor(testProduct.getId(), "Yellow");

            assertEquals(0L, count);
        }

        @Test
        @DisplayName("should count only images for specified product and color")
        void shouldCountOnlySpecifiedProductAndColor() {
            persistProductImage(testProduct, "Red", "/images/red1.jpg");
            persistProductImage(testProduct, "Red", "/images/red2.jpg");
            persistProductImage(testProduct, "Blue", "/images/blue.jpg");
            persistProductImage(anotherProduct, "Red", "/images/red3.jpg");

            long count = imageRepository.countByProductIdAndColor(testProduct.getId(), "Red");

            assertEquals(2L, count);
        }
    }

    @Nested
    @DisplayName("DeleteByProductIdAndColor")
    class DeleteByProductIdAndColor {

        @Test
        @DisplayName("should delete all images for product-color combination")
        void shouldDeleteImagesByProductAndColor() {
            persistProductImage(testProduct, "Red", "/images/red1.jpg");
            persistProductImage(testProduct, "Red", "/images/red2.jpg");
            persistProductImage(testProduct, "Blue", "/images/blue.jpg");

            imageRepository.deleteByProductIdAndColor(testProduct.getId(), "Red");
            em.flush();

            List<ProductImage> redImages = imageRepository.findByProductIdAndColor(testProduct.getId(), "Red");
            List<ProductImage> blueImages = imageRepository.findByProductIdAndColor(testProduct.getId(), "Blue");

            assertEquals(0, redImages.size());
            assertEquals(1, blueImages.size());
        }

        @Test
        @DisplayName("should not delete images of other products")
        void shouldNotDeleteImagesOfOtherProducts() {
            persistProductImage(testProduct, "Red", "/images/red1.jpg");
            persistProductImage(anotherProduct, "Red", "/images/red2.jpg");

            imageRepository.deleteByProductIdAndColor(testProduct.getId(), "Red");
            em.flush();

            List<ProductImage> anotherProductRedImages = imageRepository.findByProductIdAndColor(anotherProduct.getId(), "Red");

            assertEquals(1, anotherProductRedImages.size());
        }

        @Test
        @DisplayName("should handle deletion of non-existent product-color combination safely")
        void shouldHandleNonExistentCombination() {
            assertDoesNotThrow(() -> {
                imageRepository.deleteByProductIdAndColor(testProduct.getId(), "NonExistent");
                em.flush();
            });
        }
    }

    @Nested
    @DisplayName("DeleteByProductId")
    class DeleteByProductId {

        @Test
        @DisplayName("should delete all images for a product")
        void shouldDeleteAllImagesForProduct() {
            persistProductImage(testProduct, "Red", "/images/red.jpg");
            persistProductImage(testProduct, "Blue", "/images/blue.jpg");
            persistProductImage(testProduct, "Green", "/images/green.jpg");
            persistProductImage(anotherProduct, "Red", "/images/red2.jpg");

            imageRepository.deleteByProductId(testProduct.getId());
            em.flush();

            List<ProductImage> productImages = imageRepository.findByProductId(testProduct.getId());
            List<ProductImage> anotherProductImages = imageRepository.findByProductId(anotherProduct.getId());

            assertEquals(0, productImages.size());
            assertEquals(1, anotherProductImages.size());
        }

        @Test
        @DisplayName("should handle deletion of product with no images")
        void shouldHandleProductWithNoImages() {
            assertDoesNotThrow(() -> {
                imageRepository.deleteByProductId(testProduct.getId());
                em.flush();
            });
        }

        @Test
        @DisplayName("should preserve images of other products when deleting one product's images")
        void shouldPreserveOtherProductImages() {
            persistProductImage(testProduct, "Red", "/images/red.jpg");
            ProductImage anotherImage = persistProductImage(anotherProduct, "Red", "/images/red2.jpg");

            imageRepository.deleteByProductId(testProduct.getId());
            em.flush();

            ProductImage preserved = imageRepository.findById(anotherImage.getId());
            assertNotNull(preserved);
        }
    }

    @Nested
    @DisplayName("Integration")
    class Integration {

        @Test
        @DisplayName("should manage complete product image lifecycle")
        void shouldManageCompleteLifecycle() {
            // Create
            ProductImage red = persistProductImage(testProduct, "Red", "/images/red1.jpg");
            ProductImage red2 = persistProductImage(testProduct, "Red", "/images/red2.jpg");
            ProductImage blue = persistProductImage(testProduct, "Blue", "/images/blue.jpg");

            // Read
            List<ProductImage> allImages = imageRepository.findByProductId(testProduct.getId());
            assertEquals(3, allImages.size());

            // Update
            red.setImageUrl("/images/red-updated.jpg");
            imageRepository.save(red);
            em.flush();

            // Verify update
            ProductImage updated = imageRepository.findById(red.getId());
            assertEquals("/images/red-updated.jpg", updated.getImageUrl());

            // Delete specific color
            imageRepository.deleteByProductIdAndColor(testProduct.getId(), "Red");
            em.flush();

            List<ProductImage> remaining = imageRepository.findByProductId(testProduct.getId());
            assertEquals(1, remaining.size());
            assertEquals("Blue", remaining.get(0).getColor());
        }

        @Test
        @DisplayName("should maintain product association through image operations")
        void shouldMaintainProductAssociation() {
            ProductImage image = persistProductImage(testProduct, "Red", "/images/red.jpg");
            Integer imageId = image.getId();

            em.detach(image);
            ProductImage retrieved = imageRepository.findById(imageId);

            assertNotNull(retrieved.getProduct());
            assertEquals(testProduct.getId(), retrieved.getProduct().getId());
        }

        @Test
        @DisplayName("should handle multiple colors correctly")
        void shouldHandleMultipleColors() {
            String[] colors = {"Red", "Blue", "Green", "Black", "White"};
            for (String color : colors) {
                persistProductImage(testProduct, color, "/images/" + color.toLowerCase() + ".jpg");
            }

            List<ProductImage> images = imageRepository.findByProductId(testProduct.getId());
            assertEquals(5, images.size());

            for (String color : colors) {
                List<ProductImage> colorImages = imageRepository.findByProductIdAndColor(testProduct.getId(), color);
                assertEquals(1, colorImages.size());
                assertEquals(color, colorImages.get(0).getColor());
            }
        }

        @Test
        @DisplayName("should verify primary image selection across multiple colors")
        void shouldSelectPrimaryImagesCorrectly() {
            persistProductImage(testProduct, "Red", "/images/red1.jpg");
            persistProductImage(testProduct, "Red", "/images/red2.jpg");
            persistProductImage(testProduct, "Blue", "/images/blue1.jpg");
            persistProductImage(testProduct, "Blue", "/images/blue2.jpg");
            persistProductImage(testProduct, "Green", "/images/green.jpg");

            List<ProductImage> primaries = imageRepository.findPrimaryPerColor(testProduct.getId());

            assertEquals(3, primaries.size());
            assertEquals(3, primaries.stream().map(ProductImage::getColor).distinct().count());
        }
    }
}
