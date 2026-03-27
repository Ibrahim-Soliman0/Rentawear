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
        this.productService  = productService;
        this.variantService  = variantService;
        this.imageService    = imageService;
        this.mapper          = mapper;
        this.categoryService = categoryService;
    }

    // ── Catalog listing ───────────────────────────────────────────────────────
    // 3 queries total. Variant-ordered primary image is handled inside the
    // mapper via buildPrimaryImageByColor(images, variants).

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
    // Batch-fetches variants and images, resolves variant-ordered primary image
    // per product, sets it on the entity before the mapper reads core.imageUrl.
    // 4 queries total (products, count, variants, images).

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

    // ── Product detail (PDP) ──────────────────────────────────────────────────
    // 3 queries always.

    public ProductDetailDTO getProductDetail(int productId) {
        Product              product  = productService.getById(productId);
        List<ProductVariant> variants = variantService.getByProductId(productId);
        List<ProductImage>   images   = imageService.getByProductId(productId);
        return mapper.toDetailDTO(product, variants, images);
    }

    // ── Admin product list ────────────────────────────────────────────────────
    // Same variant-ordered resolution as searchProducts.

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
    // N+1 eliminated. Always 3 queries (product + variants + images).

    public AdminProductDetailDTO getAdminDetail(int productId) {
        Product              product   = productService.getById(productId);
        List<ProductVariant> variants  = variantService.getByProductId(productId);
        List<ProductImage>   allImages = imageService.getByProductId(productId);

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
                            imageUrlByColor.get(colorKey))
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
    // i.e. variant id ASC) that has an uploaded image.
    //
    // Falls back to the first image in upload order when no variants exist.

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

    // ── Colour / product deletion ─────────────────────────────────────────────

    public void deleteColor(int productId, String color) {
        variantService.deleteColor(productId, color);
        imageService.deleteColorImages(productId, color);
    }

    public boolean deleteProduct(int productId) {
        Product product = productService.getById(productId);
        if (product == null) return false;
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
        return productService.save(product).getId();
    }

    public AdminProductDetailDTO updateProduct(Integer id, SaveProductDTO dto) {
        Product product = productService.getById(id);
        if (product == null) return null;

        applyDtoToProduct(product, dto);

        List<Integer> incomingIds = dto.variants().stream()
                .filter(v -> v.variantId() != null)
                .map(VariantSaveDTO::variantId)
                .collect(Collectors.toList());

        product.getProductVariants().stream()
                .filter(v -> !incomingIds.contains(v.getId()))
                .collect(Collectors.toList())
                .forEach(product::removeProductVariant);

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
                ProductVariant nv = new ProductVariant();
                nv.setColor(v.color());
                nv.setSize(v.size());
                nv.setQuantity(v.quantity());
                product.addProductVariant(nv);
            }
        });

        return getAdminDetail(productService.save(product).getId());
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

    public void deleteColorImage(int productId, String color, String webappRoot) {
        imageService.deleteColorImage(productId, color, webappRoot);
    }
}