package mapper;

import dto.OrderDTO;
import dto.OrderItemDTO;
import dto.AdminOrderDTO;
import dto.AdminOrderItemDTO;
import entity.Order;
import entity.OrderItem;
import entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper
public interface OrderMapper {

    /* ── Order → OrderDTO ── */
    @Mapping(target = "createdAt",  source = "createdAt",  qualifiedByName = "instantToString")
    @Mapping(target = "items",      source = "orderItems")
    OrderDTO toDTO(Order order);
    @Mapping(target = "customerName",  source = "user.name")
    @Mapping(target = "customerEmail", source = "user.email")
    AdminOrderDTO toOrderDTO(Order order);

    List<OrderDTO> toDTOList(List<Order> orders);

    /* ── OrderItem → OrderItemDTO ── */
    @Mapping(target = "productName",     source = "variant.product.name")
    @Mapping(target = "size",            source = "variant.size")
    @Mapping(target = "color",           source = "variant.color")
    @Mapping(target = "imageUrl",        source = "variant",              qualifiedByName = "variantToImageUrl")
    @Mapping(target = "startDate",       source = "startDate",            qualifiedByName = "localDateToString")
    @Mapping(target = "endDate",         source = "endDate",              qualifiedByName = "localDateToString")
    OrderItemDTO toItemDTO(OrderItem item);

    /* ── Named converters ── */

    @Named("instantToString")
    default String instantToString(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZoneId.systemDefault())
                      .toLocalDate()
                      .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Named("localDateToString")
    default String localDateToString(java.time.LocalDate date) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Picks the first ProductImage whose color matches the variant's color.
     * Falls back to product.imageUrl if none found.
     */
    @Named("variantToImageUrl")
    default String variantToImageUrl(entity.ProductVariant variant) {
        if (variant == null) return null;
        String color = variant.getColor();
        List<ProductImage> images = variant.getProduct().getProductImages();
        return images.stream()
                .filter(img -> img.getColor().equalsIgnoreCase(color))
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(variant.getProduct().getImageUrl());
    }
    @Mapping(target = "productName", source = "variant.product.name")
    @Mapping(target = "color",       source = "variant.color")
    @Mapping(target = "size",        source = "variant.size")
    AdminOrderItemDTO toOrderItemDTO(OrderItem orderItem);
}
