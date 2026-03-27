package mapper;

import dto.*;
import entity.Product;
import entity.ProductImage;
import entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {

    @Mapping(target = "pricePerDay",  expression = "java(product.getBasePrice().doubleValue())")
    @Mapping(target = "categoryId",   expression = "java(product.getCategory() != null ? product.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(product.getCategory() != null ? product.getCategory().getName() : null)")
    @Mapping(target = "gender",       expression = "java(product.getCategory() != null && product.getCategory().getGender() != null ? product.getCategory().getGender().name() : null)")
    @Mapping(target = "brand",        constant = "rentawear")
    ProductCoreDTO toCoreDTO(Product product);

    @Mapping(target = "core",                expression = "java(toCoreDTO(product))")
    @Mapping(target = "isNew",               expression = "java(isNew(product))")
    @Mapping(target = "soldOut",             expression = "java(product.getProductVariants().stream().noneMatch(v -> v.getQuantity() > 0))")
    @Mapping(target = "swatches",            expression = "java(buildSwatchesWithFallback(images, variants))")
    @Mapping(target = "primaryImageByColor", expression = "java(buildPrimaryImageByColor(images))")
    ProductCardDTO toCardDTO(Product product, List<ProductVariant> variants, List<ProductImage> images);

    // FIX 1: Added missing @Mapping for quantityByVariantId — buildQuantityByVariantId()
    //         existed as a helper but was never wired, causing the field to always be null.
    // FIX 2: swatches now uses buildSwatchesFromVariants(variants) when the images list is
    //         empty (products added via admin with no uploaded images). This ensures colour
    //         chips are always rendered in quick-view as long as variants exist.
    @Mapping(target = "core",                      expression = "java(toCoreDTO(product))")
    @Mapping(target = "swatches",                  expression = "java(buildSwatchesWithFallback(images, variants))")
    @Mapping(target = "sizesByColor",              expression = "java(buildSizesByColor(variants))")
    @Mapping(target = "availableSizesByColor",      expression = "java(buildAvailableSizes(variants))")
    @Mapping(target = "imagesByColor",             expression = "java(buildImagesByColor(images))")
    @Mapping(target = "variantIdByColorAndSize",   expression = "java(buildVariantIdByColorAndSize(variants))")
    @Mapping(target = "quantityByVariantId",       expression = "java(buildQuantityByVariantId(variants))")
    ProductDetailDTO toDetailDTO(Product product,
                                 List<ProductVariant> variants,
                                 List<ProductImage> images);

    @Mapping(target = "core", expression = "java(toCoreDTO(product))")
    ProductSearchDTO toSearchDTO(Product product);

    @Mapping(target = "core",       expression = "java(toCoreDTO(product))")
    @Mapping(target = "inStock",    expression = "java(isInStock(variants))")
    @Mapping(target = "totalStock", expression = "java(calcTotalStock(variants))")
    AdminProductRowDTO toAdminRowDTO(Product product, List<ProductVariant> variants);

    @Mapping(target = "core",         expression = "java(toCoreDTO(product))")
    @Mapping(target = "isNew",        expression = "java(isNew(product))")
    @Mapping(target = "stockByColor", expression = "java(buildStockByColor(variants))")
    AdminProductDetailDTO toAdminDetailDTO(Product product, List<ProductVariant> variants);

    @Mapping(target = "variantId", source = "id")
    VariantStockDTO toVariantStockDTO(ProductVariant variant);

    // ── Helpers ───────────────────────────────────────────────────────────────

    default Map<Integer, Integer> buildQuantityByVariantId(List<ProductVariant> variants) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            map.put(v.getId(), v.getQuantity());
        }
        return map;
    }

    default boolean isNew(Product product) {
        return product.getCreatedAt()
                .isAfter(Instant.now().minus(30, ChronoUnit.DAYS));
    }

    default boolean isInStock(List<ProductVariant> variants) {
        return variants.stream().anyMatch(v -> v.getQuantity() > 0);
    }

    default int calcTotalStock(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return 0;
        return variants.stream().mapToInt(ProductVariant::getQuantity).sum();
    }

    // Builds colour swatches from the primary image list.
    // Images arrive ordered by id ASC — putIfAbsent ensures the first image per
    // colour wins, which matches the default-colour guarantee from the repo.
    // Color strings are stored as "#FFFFFF-White"; we split on the first "-".
    default List<ColorSwatchDTO> buildSwatches(List<ProductImage> images) {
        Map<String, ProductImage> seen = new LinkedHashMap<>();
        for (ProductImage img : images) {
            seen.putIfAbsent(img.getColor(), img);
        }
        List<ColorSwatchDTO> swatches = new ArrayList<>();
        for (ProductImage img : seen.values()) {
            String[] parts = img.getColor().split("-", 2);
            String hex  = parts[0];
            String name = parts.length > 1 ? parts[1] : parts[0];
            String slug = name.toLowerCase().replaceAll("\\s+", "-");
            swatches.add(new ColorSwatchDTO(img.getColor(), hex, name, slug));
        }
        return swatches;
    }

    // Builds colour swatches from variant color strings when no images are available.
    // Color strings follow the same "#FFFFFF-White" encoding as images.
    // Uses putIfAbsent so each colour only contributes one swatch regardless of
    // how many size variants share the same colour.
    default List<ColorSwatchDTO> buildSwatchesFromVariants(List<ProductVariant> variants) {
        Map<String, Boolean> seen = new LinkedHashMap<>();
        List<ColorSwatchDTO> swatches = new ArrayList<>();
        for (ProductVariant v : variants) {
            String color = v.getColor();
            if (color == null || color.isBlank()) continue;
            if (seen.putIfAbsent(color, Boolean.TRUE) == null) {
                String[] parts = color.split("-", 2);
                String hex  = parts[0];
                String name = parts.length > 1 ? parts[1] : parts[0];
                String slug = name.toLowerCase().replaceAll("\\s+", "-");
                swatches.add(new ColorSwatchDTO(color, hex, name, slug));
            }
        }
        return swatches;
    }

    // Used by toDetailDTO: prefers images when present, falls back to variants.
    // This ensures colour chips are always rendered in quick-view/PDP even for
    // products that were created via the admin panel without uploaded images.
    default List<ColorSwatchDTO> buildSwatchesWithFallback(List<ProductImage> images,
                                                           List<ProductVariant> variants) {
        if (images != null && !images.isEmpty()) {
            return buildSwatches(images);
        }
        return buildSwatchesFromVariants(variants != null ? variants : List.of());
    }

    // Maps colour → first image URL for swatch-click image swapping on cards.
    default Map<String, String> buildPrimaryImageByColor(List<ProductImage> images) {
        Map<String, String> map = new LinkedHashMap<>();
        for (ProductImage img : images) {
            map.putIfAbsent(img.getColor(), img.getImageUrl());
        }
        return map;
    }

    // Maps colour → all sizes (including out-of-stock).
    // Render all sizes on the PDP; use availableSizesByColor to grey out OOS ones.
    default Map<String, List<String>> buildSizesByColor(List<ProductVariant> variants) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            map.computeIfAbsent(v.getColor(), k -> new ArrayList<>())
                    .add(v.getSize());
        }
        return map;
    }

    // Maps colour → in-stock sizes only (quantity > 0).
    default Map<String, List<String>> buildAvailableSizes(List<ProductVariant> variants) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            if (v.getQuantity() > 0) {
                map.computeIfAbsent(v.getColor(), k -> new ArrayList<>())
                        .add(v.getSize());
            }
        }
        return map;
    }

    // Maps colour → all image URLs for the PDP gallery.
    default Map<String, List<String>> buildImagesByColor(List<ProductImage> images) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (ProductImage img : images) {
            map.computeIfAbsent(img.getColor(), k -> new ArrayList<>())
                    .add(img.getImageUrl());
        }
        return map;
    }

    // Maps color → size → variantId.
    // Used by quick-view.js so Cart.add() can carry the exact product_variants.id.
    // Null/blank size is normalised to "OS" for one-size products.
    default Map<String, Map<String, Integer>> buildVariantIdByColorAndSize(
            List<ProductVariant> variants) {
        Map<String, Map<String, Integer>> map = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            String sizeKey = (v.getSize() == null || v.getSize().isBlank()) ? "OS" : v.getSize();
            map.computeIfAbsent(v.getColor(), k -> new LinkedHashMap<>())
                    .put(sizeKey, v.getId());
        }
        return map;
    }

    // Maps colour → VariantStockDTO list for the admin stock editor.
    default Map<String, List<VariantStockDTO>> buildStockByColor(
            List<ProductVariant> variants) {
        Map<String, List<VariantStockDTO>> map = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            map.computeIfAbsent(v.getColor(), k -> new ArrayList<>())
                    .add(toVariantStockDTO(v));
        }
        return map;
    }
}