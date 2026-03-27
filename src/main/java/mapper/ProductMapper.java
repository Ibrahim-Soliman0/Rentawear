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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @Mapping(target = "primaryImageByColor", expression = "java(buildPrimaryImageByColor(images, variants))")
    ProductCardDTO toCardDTO(Product product, List<ProductVariant> variants, List<ProductImage> images);

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
        for (ProductVariant v : variants) map.put(v.getId(), v.getQuantity());
        return map;
    }

    default boolean isNew(Product product) {
        return product.getCreatedAt().isAfter(Instant.now().minus(30, ChronoUnit.DAYS));
    }

    default boolean isInStock(List<ProductVariant> variants) {
        return variants.stream().anyMatch(v -> v.getQuantity() > 0);
    }

    default int calcTotalStock(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return 0;
        return variants.stream().mapToInt(ProductVariant::getQuantity).sum();
    }

    default List<ColorSwatchDTO> buildSwatches(List<ProductImage> images) {
        Map<String, ProductImage> seen = new LinkedHashMap<>();
        for (ProductImage img : images) seen.putIfAbsent(img.getColor(), img);
        List<ColorSwatchDTO> swatches = new ArrayList<>();
        for (ProductImage img : seen.values()) {
            String[] parts = img.getColor().split("-", 2);
            String hex  = parts[0];
            String name = parts.length > 1 ? parts[1] : parts[0];
            swatches.add(new ColorSwatchDTO(img.getColor(), hex, name,
                    name.toLowerCase().replaceAll("\\s+", "-")));
        }
        return swatches;
    }

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
                swatches.add(new ColorSwatchDTO(color, hex, name,
                        name.toLowerCase().replaceAll("\\s+", "-")));
            }
        }
        return swatches;
    }

    default List<ColorSwatchDTO> buildSwatchesWithFallback(List<ProductImage> images,
                                                           List<ProductVariant> variants) {
        List<ColorSwatchDTO> fromVariants = buildSwatchesFromVariants(
                variants != null ? variants : List.of());
        if (!fromVariants.isEmpty()) return fromVariants;
        return buildSwatches(images != null ? images : List.of());
    }

    default Map<String, String> buildPrimaryImageByColor(List<ProductImage> images,
                                                         List<ProductVariant> variants) {
        // Build color → imageUrl lookup from images; putIfAbsent keeps the first
        // image per color (stable across calls because images arrive id ASC).
        Map<String, String> imageByColor = new LinkedHashMap<>();
        if (images != null) {
            for (ProductImage img : images) {
                if (img.getColor() != null) {
                    imageByColor.putIfAbsent(img.getColor(), img.getImageUrl());
                }
            }
        }

        // No variants — fall back to upload order so something still shows.
        if (variants == null || variants.isEmpty()) return imageByColor;

        // Walk variants in insertion order; emit each color exactly once,
        // only if it has an image.
        Map<String, String> result = new LinkedHashMap<>();
        Set<String> seen = new LinkedHashSet<>();
        for (ProductVariant v : variants) {
            String color = v.getColor();
            if (color == null || !seen.add(color)) continue;
            String url = imageByColor.get(color);
            if (url != null) result.put(color, url);
        }

        return result;
    }

    default Map<String, List<String>> buildSizesByColor(List<ProductVariant> variants) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (ProductVariant v : variants)
            map.computeIfAbsent(v.getColor(), k -> new ArrayList<>()).add(v.getSize());
        return map;
    }

    default Map<String, List<String>> buildAvailableSizes(List<ProductVariant> variants) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            if (v.getQuantity() > 0)
                map.computeIfAbsent(v.getColor(), k -> new ArrayList<>()).add(v.getSize());
        }
        return map;
    }

    default Map<String, List<String>> buildImagesByColor(List<ProductImage> images) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (ProductImage img : images)
            map.computeIfAbsent(img.getColor(), k -> new ArrayList<>()).add(img.getImageUrl());
        return map;
    }

    default Map<String, Map<String, Integer>> buildVariantIdByColorAndSize(
            List<ProductVariant> variants) {
        Map<String, Map<String, Integer>> map = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            String sizeKey = (v.getSize() == null || v.getSize().isBlank()) ? "OS" : v.getSize();
            map.computeIfAbsent(v.getColor(), k -> new LinkedHashMap<>()).put(sizeKey, v.getId());
        }
        return map;
    }

    default Map<String, List<VariantStockDTO>> buildStockByColor(List<ProductVariant> variants) {
        Map<String, List<VariantStockDTO>> map = new LinkedHashMap<>();
        for (ProductVariant v : variants)
            map.computeIfAbsent(v.getColor(), k -> new ArrayList<>()).add(toVariantStockDTO(v));
        return map;
    }
}