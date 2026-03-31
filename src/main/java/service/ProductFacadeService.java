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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// Orchestrates ProductService, ProductVariantService, ProductImageService,
// and CartItemService.
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
    private final CartItemService       cartItemService;

    public ProductFacadeService(ProductService productService,
                                ProductVariantService variantService,
                                ProductImageService imageService,
                                ProductMapper mapper,
                                CategoryService categoryService,
                                CartItemService cartItemService) {
        this.productService  = productService;
        this.variantService  = variantService;
        this.imageService    = imageService;
        this.mapper          = mapper;
        this.categoryService = categoryService;
        this.cartItemService = cartItemService;
    }

    // ── Catalog listing ───────────────────────────────────────────────────────
    //
    // Batch strategy to avoid N+1:
    //   1. Fetch the page of products (2-step pagination in repo)
    //   2. Collect all product ids on that page
    //   3. Fetch primary images for ALL those ids in ONE query
    //   4. Group images by product id in memory
    //   5. Map each product with its pre-fetched images

    public ProductListResult getProducts(ProductFilterDTO filter) {
        List<Product> products = fetchProducts(filter);
        long          total    = countProducts(filter);
        PriceRangeDTO range    = filter.isInterestBased()
                ? productService.getMinMaxPriceForInterests(filter)
                : productService.getMinMaxPrice(filter);

        List<Integer> ids = products.stream().map(Product::getId).collect(Collectors.toList());

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

        List<Integer> ids = products.stream().map(Product::getId).collect(Collectors.toList());

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
        long          total    = countProducts(filter);

        List<Integer> ids = products.stream().map(Product::getId).collect(Collectors.toList());

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
    // Images grouped in memory — always exactly 3 queries regardless of color count.

    public AdminProductDetailDTO getAdminDetail(int productId) {
        Product              product   = productService.getById(productId);
        List<ProductVariant> variants  = variantService.getByProductId(productId); // deleted=false
        List<ProductImage>   allImages = imageService.getByProductId(productId);

        // Group images by color in memory — first image per color wins.
        Map<String, String> imageUrlByColor = new LinkedHashMap<>();
        for (ProductImage img : allImages) {
            imageUrlByColor.putIfAbsent(img.getColor(), img.getImageUrl());
        }

        Map<String, List<VariantStockDTO>> stockByColor = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            String colorKey = v.getColor();
            stockByColor.computeIfAbsent(colorKey, k -> new ArrayList<>());
            stockByColor.get(colorKey).add(
                    new VariantStockDTO(v.getId(), v.getSize(), v.getQuantity(),
                            imageUrlByColor.get(colorKey)));
        }

        return new AdminProductDetailDTO(
                mapper.toCoreDTO(product),
                product.getDescription(),
                isNew(product),
                stockByColor
        );
    }

    // ── Variant-ordered primary image resolver ────────────────────────────────

    private String resolveVariantOrderedPrimary(List<ProductVariant> variants,
                                                List<ProductImage>   images) {
        if (images == null || images.isEmpty()) return null;
        if (variants == null || variants.isEmpty()) return images.get(0).getImageUrl();

        Map<String, String> imageByColor = new LinkedHashMap<>();
        for (ProductImage img : images) {
            if (img.getColor() != null) imageByColor.putIfAbsent(img.getColor(), img.getImageUrl());
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

    // ── Color deletion ────────────────────────────────────────────────────────
    //
    // Safe delete sequence for a single color:
    //
    //  1. Collect active variant IDs for this product+color
    //  2. Hard-delete cart_items referencing those variants   (ephemeral — safe)
    //  3. Soft-delete the variants                            (order_items FK stays valid)
    //  4. Delete the color image file + product_images row    (no inbound FKs)
    //
    // All four steps run inside the same transaction (EntityManagerFilter).

    public void deleteColor(int productId, String color, String webappRoot) {
        // Step 1 — snapshot IDs before touching anything
        List<Integer> variantIds = variantService.findIdsByColor(productId, color);

        // Step 2 — purge cart items (hard delete is safe; they're ephemeral)
        cartItemService.deleteByVariantIds(variantIds);

        // Step 3 — soft-delete variants (keeps order_items FK valid)
        variantService.softDeleteColor(productId, color);

//        // Step 4 — delete image file + DB record (no FK constraints on product_images)
//        imageService.deleteColorImage(productId, color, webappRoot);
    }

    // ── Product deletion ──────────────────────────────────────────────────────
    //
    // Safe delete sequence for an entire product:
    //
    //  1. Snapshot ALL active variant IDs
    //  2. Hard-delete cart_items referencing those variants
    //  3. Soft-delete all variants
    //  4. Hard-delete all product_images rows (no inbound FKs — safe)
    //  5. Soft-delete the product itself
    //
    // Order history (order_items) is untouched throughout.

    public boolean deleteProduct(int productId) {
        Product product = productService.getById(productId);
        if (product == null) return false;

        // Step 1 — snapshot active variant IDs (collection is loaded, EM is open)
        List<Integer> variantIds = product.getProductVariants().stream()
                .filter(v -> !v.isDeleted())
                .map(ProductVariant::getId)
                .collect(Collectors.toList());

        // Step 2 — purge cart items
        cartItemService.deleteByVariantIds(variantIds);

        // Step 3 — soft-delete variants
        variantService.softDeleteByProductId(productId);

//        // Step 4 — hard-delete image DB rows (product_images has no inbound FKs)
//        imageService.deleteByProductId(productId);

        // Step 5 — soft-delete product
        productService.softDelete(productId);

        return true;
    }

    // ── Update product ────────────────────────────────────────────────────────
    //
    // Variants removed from the edit form must NOT go through
    // product.removeProductVariant() — that triggers orphanRemoval which issues
    // a hard DELETE and will violate the order_items FK if any orders exist.
    //
    // Instead:
    //   a) Identify active variants not present in the incoming payload
    //   b) Purge their cart items first (hard delete — safe)
    //   c) Set deleted = true on the managed entity
    //      → Hibernate flushes this as UPDATE, never DELETE

    public AdminProductDetailDTO updateProduct(Integer id, SaveProductDTO dto) {
        Product product = productService.getById(id);
        if (product == null) return null;

        applyDtoToProduct(product, dto);

        List<Integer> incomingIds = dto.variants().stream()
                .filter(v -> v.variantId() != null)
                .map(VariantSaveDTO::variantId)
                .collect(Collectors.toList());

        // Active variants whose ID is NOT in the incoming payload → soft-delete
        List<ProductVariant> variantsToRemove = product.getProductVariants().stream()
                .filter(v -> !v.isDeleted())
                .filter(v -> !incomingIds.contains(v.getId()))
                .collect(Collectors.toList());

        for (ProductVariant v : variantsToRemove) {
            cartItemService.deleteByVariantIds(List.of(v.getId())); // purge cart first
            v.setDeleted(true); // UPDATE only — entity stays in collection
        }

        // Update existing / add new (or resurrect a previously soft-deleted variant)
        //
        // WHY resurrection matters:
        //   product_variants has UNIQUE (product_id, color, size).
        //   A soft-deleted row still occupies that slot in the DB.
        //   Blindly INSERTing the same color+size would cause a constraint violation.
        //   Instead we search the full collection (which Hibernate loads including
        //   deleted=true rows, since the filter only lives in named queries, not the
        //   mapping) and flip the deleted flag back if a match is found.
        dto.variants().forEach(v -> {
            if (v.variantId() != null) {
                // ── Update an existing active variant ─────────────────────────
                product.getProductVariants().stream()
                        .filter(pv -> !pv.isDeleted() && pv.getId().equals(v.variantId()))
                        .findFirst()
                        .ifPresent(pv -> {
                            pv.setSize(v.size());
                            pv.setColor(v.color());
                            pv.setQuantity(v.quantity());
                        });
            } else {
                // ── New variant from the form (no variantId) ──────────────────
                // First check if a soft-deleted row with the same color+size exists.
                // product.getProductVariants() includes deleted rows because the
                // lazy collection has no @Where filter — Hibernate loads everything.
                Optional<ProductVariant> softDeleted = product.getProductVariants().stream()
                        .filter(pv -> pv.isDeleted()
                                && Objects.equals(pv.getColor(), v.color())
                                && Objects.equals(pv.getSize(),  v.size()))
                        .findFirst();

                if (softDeleted.isPresent()) {
                    // Resurrect: un-delete the row, update quantity.
                    // Hibernate flushes this as UPDATE — no INSERT, no constraint hit.
                    ProductVariant pv = softDeleted.get();
                    pv.setDeleted(false);
                    pv.setQuantity(v.quantity());
                } else {
                    // Genuinely new color+size — safe to INSERT.
                    ProductVariant newVariant = new ProductVariant();
                    newVariant.setColor(v.color());
                    newVariant.setSize(v.size());
                    newVariant.setQuantity(v.quantity());
                    product.addProductVariant(newVariant);
                }
            }
        });

        Product saved = productService.save(product);
        return getAdminDetail(saved.getId());
    }

    // ── Image management ──────────────────────────────────────────────────────

    public String updateProductImage(int productId, String imageUrl) {
        Product product = productService.getById(productId);
        if (product == null) return null;
        String old = product.getImageUrl();
        product.setImageUrl(imageUrl);
        productService.save(product);
        return old;
    }

    public String saveColorImage(Product product, String encodedColor,
                                 InputStream inputStream, String webappRoot) throws IOException {
        return imageService.saveColorImage(product, encodedColor, inputStream, webappRoot);
    }

    // Image-only deletion — no variant or cart changes.
    // Still used by AdminProductColorImageServlet.doDelete (upload-replace flow).
    public void deleteColorImage(int productId, String color, String webappRoot) {
        imageService.deleteColorImage(productId, color, webappRoot);
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public Integer saveProduct(SaveProductDTO dto) {
        Product product = new Product();
        applyDtoToProduct(product, dto);
        return productService.save(product).getId();
    }

    // ── Misc ──────────────────────────────────────────────────────────────────

    public Product getProductById(int id) {
        return productService.getById(id);
    }

    boolean isNew(Product product) {
        return product.getCreatedAt().isAfter(Instant.now().minus(30, ChronoUnit.DAYS));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

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

    private void applyDtoToProduct(Product product, SaveProductDTO dto) {
        product.setName(dto.name());
        product.setBasePrice(BigDecimal.valueOf(dto.pricePerDay()));
        product.setImageUrl(dto.imageUrl());
        product.setDescription(dto.description());
        if (dto.categoryId() != null) {
            categoryService.getById(dto.categoryId()).ifPresent(product::setCategory);
        }
    }
}