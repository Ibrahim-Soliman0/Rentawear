package service;

import dto.*;
import entity.Product;
import entity.ProductImage;
import entity.ProductVariant;
import mapper.ProductMapper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Orchestrates ProductService, ProductVariantService, and ProductImageService.
// This is the only class that:
//   1. Calls more than one service in a single operation
//   2. Calls the mapper to assemble final DTOs
//   3. Is called directly by servlets
public class ProductFacadeService {

    private final ProductService        productService;
    private final ProductVariantService variantService;
    private final ProductImageService   imageService;
    private final ProductMapper         mapper;
    private final CategoryService       categoryService;

    public ProductFacadeService(ProductService productService,
                                ProductVariantService variantService,
                                ProductImageService imageService,
                                ProductMapper mapper, CategoryService categoryService) {
        this.productService = productService;
        this.variantService = variantService;
        this.imageService   = imageService;
        this.mapper         = mapper;
        this.categoryService = categoryService;
    }

    // ── Catalog listing ───────────────────────────────────────────────────────
    //
    // Batch strategy to avoid N+1:
    //   1. Fetch the page of products (2-step pagination in repo)
    //   2. Collect all product ids on that page
    //   3. Fetch primary images for ALL those ids in ONE query
    //   4. Group images by product id in memory
    //   5. Map each product with its pre-fetched images
    //

    public ProductListResult getProducts(ProductFilterDTO filter) {
        List<Product> products = fetchProducts(filter);
        long          total    = countProducts(filter);
        PriceRangeDTO range    = filter.isInterestBased()
                ? productService.getMinMaxPriceForInterests(filter)
                : productService.getMinMaxPrice(filter);

        List<Integer> ids = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        // Batch-fetch both variants AND images — still only 2 extra queries
        Map<Integer, List<ProductVariant>> variantsByProductId =
                variantService.getByProductIds(ids);

        Map<Integer, List<ProductImage>> imagesByProductId =
                imageService.getPrimaryPerColorForProducts(ids);

        List<ProductCardDTO> cards = products.stream()
                .map(p -> mapper.toCardDTO(
                        p,
                        variantsByProductId.getOrDefault(p.getId(), List.of()),
                        imagesByProductId.getOrDefault(p.getId(), List.of())
                ))
                .collect(Collectors.toList());

        return new ProductListResult(cards, total, range, filter.page(), filter.pageSize());
    }

    // ── Search ────────────────────────────────────────────────────────────────

    public ProductSearchResult searchProducts(ProductFilterDTO filter) {
        List<Product> products = productService.searchFiltered(filter);
        long          total    = productService.countSearchFiltered(filter);
        PriceRangeDTO range    = productService.getMinMaxPrice(filter);

        List<Integer> ids = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        Map<Integer, List<ProductVariant>> variantsByProductId =
                variantService.getByProductIds(ids);

        Map<Integer, List<ProductImage>> imagesByProductId =
                imageService.getPrimaryPerColorForProducts(ids);

        products.forEach(p -> {
            String resolved = resolveVariantOrderedPrimary(
                    variantsByProductId.getOrDefault(p.getId(), List.of()),
                    imagesByProductId.getOrDefault(p.getId(), List.of())
            );
            if (resolved != null) p.setImageUrl(resolved);
        });

        List<ProductSearchDTO> results = products.stream()
                .map(mapper::toSearchDTO)
                .collect(Collectors.toList());

        return new ProductSearchResult(results, total, range, filter.page(), filter.pageSize());
    }

    // Fetches product + all variants + all images — three queries, always.

    public ProductDetailDTO getProductDetail(int productId) {
        Product              product  = productService.getById(productId);
        List<ProductVariant> variants = variantService.getByProductId(productId);
        List<ProductImage>   images   = imageService.getByProductId(productId);
        return mapper.toDetailDTO(product, variants, images);
    }

    // ── Admin product list ────────────────────────────────────────────────────
    public AdminProductListResult getAdminProducts(ProductFilterDTO filter) {
        List<Product> products = fetchProducts(filter);
        long total = countProducts(filter);

        List<Integer> ids = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        Map<Integer, List<ProductVariant>> variantsByProductId =
                variantService.getByProductIds(ids);

        Map<Integer, List<ProductImage>> imagesByProductId =
                imageService.getPrimaryPerColorForProducts(ids);

        products.forEach(p -> {
            String resolved = resolveVariantOrderedPrimary(
                    variantsByProductId.getOrDefault(p.getId(), List.of()),
                    imagesByProductId.getOrDefault(p.getId(), List.of())
            );
            if (resolved != null) p.setImageUrl(resolved);
        });

        List<AdminProductRowDTO> rows = products.stream()
                .map(p -> mapper.toAdminRowDTO(p,
                        variantsByProductId.getOrDefault(p.getId(), List.of())))
                .collect(Collectors.toList());

        return new AdminProductListResult(rows, total, filter.page(), filter.pageSize());
    }

    // ── Admin product detail ──────────────────────────────────────────────────
    // FIX 1: Eliminated the per-color N+1 query inside the variant loop.
    // Previously: imageService.findByProductIdAndColor() called once per unique
    //             color → 5 colors = 5 extra queries.
    // After fix:  imageService.getByProductId() called once, results grouped in
    //             memory → always exactly 3 queries total regardless of color count.

    public AdminProductDetailDTO getAdminDetail(int productId) {
        Product              product   = productService.getById(productId);
        List<ProductVariant> variants  = variantService.getByProductId(productId);
        List<ProductImage>   allImages = imageService.getByProductId(productId);

        // Group images by color in memory — O(1) lookup replaces per-color query.
        // putIfAbsent: images arrive id ASC, so the first image per color wins.
        Map<String, String> imageUrlByColor = new LinkedHashMap<>();
        for (ProductImage img : allImages) {
            imageUrlByColor.putIfAbsent(img.getColor(), img.getImageUrl());
        }

        Map<String, List<VariantStockDTO>> stockByColor = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            String colorKey = v.getColor();
            stockByColor.computeIfAbsent(colorKey, k -> new ArrayList<>());
            String imageUrl = imageUrlByColor.get(colorKey);
            stockByColor.get(colorKey).add(
                    new VariantStockDTO(v.getId(), v.getSize(), v.getQuantity(), imageUrl)
            );
        }

        return new AdminProductDetailDTO(
                mapper.toCoreDTO(product),
                product.getDescription(),
                isNew(product),
                stockByColor
        );
    }

    // ── Variant-ordered primary image resolver ────────────────────────────────
    //
    // Returns the image URL for the first color (in variant insertion order,
    // variant id ASC) that has an uploaded image. Used by searchProducts and
    // getAdminProducts to set product.imageUrl before the mapper reads it.
    private String resolveVariantOrderedPrimary(List<ProductVariant> variants,
                                                List<ProductImage>   images) {
        if (images == null || images.isEmpty()) return null;

        if (variants == null || variants.isEmpty()) {
            return images.get(0).getImageUrl();
        }

        Map<String, String> imageByColor = new LinkedHashMap<>();
        for (ProductImage img : images) {
            if (img.getColor() != null) {
                imageByColor.putIfAbsent(img.getColor(), img.getImageUrl());
            }
        }

        Set<String> seen = new LinkedHashSet<>();
        for (ProductVariant v : variants) {
            String color = v.getColor();
            if (color == null || !seen.add(color)) continue;
            String url = imageByColor.get(color);
            if (url != null) return url;
        }

        return null;
    }

    // Colour deletion
    // Deletes both variants and images for a colour in one transaction.
    // The EntityManagerFilter wraps the entire request in a transaction, so if
    // either delete throws, both are rolled back automatically.

    public void deleteColor(int productId, String color) {
        variantService.deleteColor(productId, color);
        imageService.deleteColorImages(productId, color);
    }

    // Product deletion
    // Deletes product along with all its variants and images.
    // Returns true if deleted, false if product not found.

    public boolean deleteProduct(int productId) {
        Product product = productService.getById(productId);
        if (product == null) {
            return false;
        }
        // Delete variants and images first (due to foreign key constraints)
        variantService.deleteByProductId(productId);
        imageService.deleteByProductId(productId);
        productService.delete(productId);
        return true;
    }

    // ── Private routing helpers ───────────────────────────────────────────────

    private List<Product> fetchProducts(ProductFilterDTO f) {
        if (f.isSearch())        return productService.searchFiltered(f);
        if (f.isNewOnly())       return productService.findNew(f);
        if (f.isInterestBased()) return productService.findByInterests(f);
        return productService.findFiltered(f);
    }

    private long countProducts(ProductFilterDTO f) {
        if (f.isSearch())        return productService.countSearchFiltered(f);
        if (f.isNewOnly())       return productService.countNew(f);
        if (f.isInterestBased()) return productService.countByInterests(f);
        return productService.countFiltered(f);
    }

    public Integer saveProduct(SaveProductDTO dto) {
        Product product = new Product();
        applyDtoToProduct(product, dto);
        Product saved = productService.save(product);
        return saved.getId();
    }

    public AdminProductDetailDTO updateProduct(Integer id, SaveProductDTO dto) {
        Product product = productService.getById(id);
        if (product == null) return null;

        applyDtoToProduct(product, dto);

        // Handle variants — update existing, add new, remove deleted
        List<Integer> incomingIds = dto.variants().stream()
                .filter(v -> v.variantId() != null)
                .map(VariantSaveDTO::variantId)
                .collect(Collectors.toList());

        // Remove variants not in incoming list
        List<ProductVariant> variantsToRemove = product.getProductVariants().stream()
                .filter(v -> !incomingIds.contains(v.getId()))
                .collect(Collectors.toList());
        variantsToRemove.forEach(product::removeProductVariant);

        // Update existing / add new
        dto.variants().forEach(v -> {
            if (v.variantId() != null) {
                product.getProductVariants().stream()
                        .filter(pv -> pv.getId().equals(v.variantId()))
                        .findFirst()
                        .ifPresent(pv -> {
                            pv.setSize(v.size());
                            pv.setColor(v.color());
                            pv.setQuantity(v.quantity());
                        });
            } else {
                ProductVariant newVariant = new ProductVariant();
                newVariant.setColor(v.color());
                newVariant.setSize(v.size());
                newVariant.setQuantity(v.quantity());
                product.addProductVariant(newVariant);
            }
        });

        Product saved = productService.save(product);
        return getAdminDetail(saved.getId());
    }

    public String updateProductImage(int productId, String imageUrl) {
        Product product = productService.getById(productId);
        if (product == null) return null;
        String old = product.getImageUrl();
        product.setImageUrl(imageUrl);
        productService.save(product);
        return old;
    }

    private void applyDtoToProduct(Product product, SaveProductDTO dto) {
        product.setName(dto.name());
        product.setBasePrice(BigDecimal.valueOf(dto.pricePerDay()));
        product.setImageUrl(dto.imageUrl());
        product.setDescription(dto.description());
        if (dto.categoryId() != null) {
            categoryService.getById(dto.categoryId())
                    .ifPresent(product::setCategory);
        }
    }

    public Product getProductById(int id) {
        return productService.getById(id);
    }

    public String saveColorImage(Product product, String encodedColor,
                                 InputStream inputStream, String webappRoot) throws IOException {
        return imageService.saveColorImage(product, encodedColor, inputStream, webappRoot);
    }

    boolean isNew(Product product) {
        return product.getCreatedAt()
                .isAfter(Instant.now().minus(30, ChronoUnit.DAYS));
    }

    // Deletes all images for a colour.
    public void deleteColorImage(int productId, String color, String webappRoot) {
        imageService.deleteColorImage(productId, color, webappRoot);
    }
}