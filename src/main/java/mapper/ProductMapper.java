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

    @Mapping(target = "pricePerDay", expression = "java(product.getBasePrice().doubleValue())")
    @Mapping(target = "categoryId",  expression = "java(product.getCategory() != null ? product.getCategory().getId() : null)")
    @Mapping(target = "gender",      expression = "java(product.getCategory() != null && product.getCategory().getGender() != null ? product.getCategory().getGender().name() : null)")
    @Mapping(target = "brand",       constant = "rentawear")
    ProductCoreDTO toCoreDTO(Product product);

    @Mapping(target = "core",                expression = "java(toCoreDTO(product))")
    @Mapping(target = "isNew",               expression = "java(isNew(product))")
    @Mapping(target = "soldOut",             expression = "java(product.getProductVariants().stream().noneMatch(v -> v.getQuantity() > 0))")
    @Mapping(target = "swatches",            expression = "java(buildSwatches(images))")
    @Mapping(target = "primaryImageByColor", expression = "java(buildPrimaryImageByColor(images))")
    ProductCardDTO toCardDTO(Product product, List<ProductImage> images);

    @Mapping(target = "core",                      expression = "java(toCoreDTO(product))")
    @Mapping(target = "swatches",                  expression = "java(buildSwatches(images))")
    @Mapping(target = "sizesByColor",              expression = "java(buildSizesByColor(variants))")
    @Mapping(target = "availableSizesByColor",     expression = "java(buildAvailableSizes(variants))")
    @Mapping(target = "imagesByColor",             expression = "java(buildImagesByColor(images))")
    @Mapping(target = "variantIdByColorAndSize",   expression = "java(buildVariantIdByColorAndSize(variants))")
    ProductDetailDTO toDetailDTO(Product product,
                                 List<ProductVariant> variants,
                                 List<ProductImage> images);


    @Mapping(target = "core", expression = "java(toCoreDTO(product))")
    ProductSearchDTO toSearchDTO(Product product);


    @Mapping(target = "core",    expression = "java(toCoreDTO(product))")
    @Mapping(target = "inStock", expression = "java(isInStock(variants))")
    AdminProductRowDTO toAdminRowDTO(Product product, List<ProductVariant> variants);


    @Mapping(target = "core",         expression = "java(toCoreDTO(product))")
    @Mapping(target = "isNew",        expression = "java(isNew(product))")
    @Mapping(target = "stockByColor", expression = "java(buildStockByColor(variants))")
    AdminProductDetailDTO toAdminDetailDTO(Product product, List<ProductVariant> variants);

    @Mapping(target = "variantId", source = "id")
    VariantStockDTO toVariantStockDTO(ProductVariant variant);

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

    // Maps colour , first image URL for swatch-click image swapping on cards.
    default Map<String, String> buildPrimaryImageByColor(List<ProductImage> images) {
        Map<String, String> map = new LinkedHashMap<>();
        for (ProductImage img : images) {
            map.putIfAbsent(img.getColor(), img.getImageUrl());
        }
        return map;
    }

    // Maps colour, all sizes (including out-of-stock).
    // Render all sizes on the PDP; use availableSizesByColor to gray out
    // the ones not in stock.
    default Map<String, List<String>> buildSizesByColor(List<ProductVariant> variants) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            map.computeIfAbsent(v.getColor(), k -> new ArrayList<>())
                    .add(v.getSize());
        }
        return map;
    }

    // Maps colour , in-stock sizes only (quantity > 0).
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

    // Maps colour , all image URLs for the PDP gallery.
    default Map<String, List<String>> buildImagesByColor(List<ProductImage> images) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (ProductImage img : images) {
            map.computeIfAbsent(img.getColor(), k -> new ArrayList<>())
                    .add(img.getImageUrl());
        }
        return map;
    }

    // Maps color → size → variantId.
    // Used by quick-view.js so Cart.add() can carry the exact product_variants.id
    // needed for cart_items inserts at checkout — no reverse lookup required.
    //
    // Size key: v.getSize() is nullable (one-size products have no size row).
    // We normalise null/blank to "OS" here to match the "OS" sentinel that
    // quick-view.js writes to activeSize when renderSizes() finds no sizes.
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

    // Maps colour , VariantStockDTO list for the admin stock editor.
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