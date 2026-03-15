package dto;

import entity.ProductImage;
import util.ColorUtil;
import java.util.*;
import java.util.stream.Collectors;

public class ProductImagesDTO {
    public int    productId;
    public String productName;
    public String defaultColor;           // encoded color of lowest-id variant
    public List<ColorGroupDTO> colorGroups;

    public static class ColorGroupDTO {
        public String color;
        public String hex;
        public String name;
        public String slug;
        public List<ImageEntryDTO> images;
    }

    public static class ImageEntryDTO {
        public String base;
        public String alt;
    }

//    public static ProductImagesDTO from(Product product,
//                                        LinkedHashMap<String, List<ProductImage>> grouped) {
//        ProductImagesDTO dto = new ProductImagesDTO();
//        dto.productId   = product.getId();
//        dto.productName = product.getName();
//
//        // Default color = first variant's color (lowest id, due to @OrderBy)
//        dto.defaultColor = product.getProductVariants().isEmpty()
//                ? null
//                : product.getProductVariants().get(0).getColor();
//
//        dto.colorGroups = new ArrayList<>();
//        for (Map.Entry<String, List<ProductImage>> entry : grouped.entrySet()) {
//            String encoded = entry.getKey();
//            ColorGroupDTO group = new ColorGroupDTO();
//            group.color  = encoded;
//            group.hex    = ColorUtil.hex(encoded);
//            group.name   = ColorUtil.name(encoded);
//            group.slug   = ColorUtil.slug(encoded);
//            group.images = entry.getValue().stream().map(img -> {
//                ImageEntryDTO e = new ImageEntryDTO();
//                e.base = img.getImageUrl();
//                e.alt  = product.getName() + " in " + group.name;
//                return e;
//            }).collect(Collectors.toList());
//            dto.colorGroups.add(group);
//        }
//        return dto;
//    }

    public static ProductImagesDTO from(int productId,
                                        String productName,
                                        String defaultColor,
                                        LinkedHashMap<String, List<ProductImage>> grouped) {
        ProductImagesDTO dto = new ProductImagesDTO();
        dto.productId    = productId;
        dto.productName  = productName;
        dto.defaultColor = defaultColor;
        dto.colorGroups  = new ArrayList<>();

        for (Map.Entry<String, List<ProductImage>> entry : grouped.entrySet()) {
            String encoded      = entry.getKey();
            ColorGroupDTO group = new ColorGroupDTO();
            group.color  = encoded;
            group.hex    = ColorUtil.hex(encoded);
            group.name   = ColorUtil.name(encoded);
            group.slug   = ColorUtil.slug(encoded);
            group.images = entry.getValue().stream().map(img -> {
                ImageEntryDTO e = new ImageEntryDTO();
                e.base = img.getImageUrl();
                e.alt  =  productName + " in " + group.name;
                return e;
            }).collect(Collectors.toList());
            dto.colorGroups.add(group);
        }
        return dto;
    }
}