package mapper;

import dto.CartItemDTO;
import dto.ProductCoreDTO;
import entity.CartItem;
import entity.Product;
import entity.ProductImage;
import entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * MapStruct mapper — CartItem entity  →  CartItemDTO
 * <p>
 * Mapping decisions
 * ─────────────────────────────────────────────────────────────
 * CartItem.id              → CartItemDTO.cartItemId
 * CartItem.variant.id      → CartItemDTO.variantId
 * CartItem.variant.color   → CartItemDTO.color
 * CartItem.variant.size    → CartItemDTO.size
 * CartItem.startDate       → CartItemDTO.startDate
 * CartItem.endDate         → CartItemDTO.endDate
 * CartItem.endDate
 * - startDate (days)     → CartItemDTO.rentalDays   (derived)
 * CartItem.variant.product → CartItemDTO.core         (nested DTO)
 * <p>
 * CartItemDTO.totalPrice is derived:
 * core.pricePerDay × rentalDays × quantity
 * <p>
 * Assumptions about your entity graph
 * ─────────────────────────────────────────────────────────────
 * ProductVariant has:
 * - int id
 * - String color
 * - String size
 * - int inventoryQty  (stock level)
 * - Product product   (the parent product)
 * <p>
 * Product has:
 * - int id
 * - String name
 * - String brand
 * - double pricePerDay
 * - String imageUrl
 * - String gender
 * - int ( Category category with getId())
 * <p>
 * Adjust the @Mapping expressions below if your field names differ.
 */
@Mapper
public interface CartItemMapper {

    // ── Single item ───────────────────────────────────────────────────────────

    /**
     * Maps a CartItem entity (with its full variant → product graph) to a
     * CartItemDTO ready to be serialised to JSON for the JS cart.
     *
     * @param cartItem a fully-loaded CartItem (variant + product must not be null)
     * @return the flat DTO the JS layer expects
     */
    @Mappings({
            // cartItemId ← entity primary key
            @Mapping(target = "cartItemId", source = "id"),

            // Variant fields
            @Mapping(target = "variantId", source = "variant.id"),
            @Mapping(target = "color", source = "variant.color"),
            @Mapping(target = "size", source = "variant.size"),

            // Dates — map directly (LocalDate → LocalDate, no conversion needed)
            @Mapping(target = "startDate", expression = "java(cartItem.getStartDate().toString())"),
            @Mapping(target = "endDate", expression = "java(cartItem.getEndDate().toString())"),

            // rentalDays — derived from the two dates
            @Mapping(target = "rentalDays", expression = "java(calculateRentalDays(cartItem))"),

            // totalPrice — derived: pricePerDay × rentalDays × quantity
            @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(cartItem))"),

            // inventoryQty
            @Mapping(target = "inventoryQty", source = "variant.quantity"),

            // item qty
            @Mapping(target = "qty", source = "quantity"),

            // core — mapped via the helper method below
            @Mapping(target = "core", expression = "java(toProductCoreDTO(cartItem.getVariant()))"),
    })
    CartItemDTO toDTO(CartItem cartItem);

    /**
     * Maps a list of CartItem entities to a list of CartItemDTOs.
     * Used by CartItemsServlet to serialise the full cart in one call.
     */
    List<CartItemDTO> toDTOList(List<CartItem> cartItems);

    // ── Derived field helpers ─────────────────────────────────────────────────

    /**
     * Number of rental days — exclusive end date.
     * e.g. Nov 12 → Nov 16 = 4 days (ChronoUnit.DAYS between gives 4, not 5)
     */
    default int calculateRentalDays(CartItem item) {
        if (item.getStartDate() == null || item.getEndDate() == null) return 0;
        return (int) ChronoUnit.DAYS.between(
                item.getStartDate(),
                item.getEndDate()
        );
    }

    /**
     * Total price for this line item:
     * pricePerDay  ×  rentalDays  ×  quantity
     */
    default double calculateTotalPrice(CartItem item) {
        if (item.getVariant() == null || item.getVariant().getProduct() == null) return 0.0;
        int days = calculateRentalDays(item);
        double ppd = item.getVariant().getProduct().getBasePrice().doubleValue();
        int qty = item.getQuantity() != null ? item.getQuantity() : 1;
        return ppd * days * qty;
    }

    /**
     * Builds the nested ProductCoreDTO from the ProductVariant's parent Product.
     *
     * Image resolution — reads from product_images filtered by this variant's
     * color, same pattern as OrderMapper.variantToImageUrl. This is the correct
     * source for the cart item image: it gives the color-specific image the user
     * saw when they added the item, and it remains correct across page refreshes
     * because it reads the ProductImage table rather than the stale
     * products.image_url entity field.
     *
     * Falls back to null when no image exists for this color, the JS layer
     * (CartItemNormaliser.fromDTO) substitutes '/assets/img/placeholder' when
     * core.imageUrl is null.
     */
    default ProductCoreDTO toProductCoreDTO(ProductVariant variant) {
        if (variant == null || variant.getProduct() == null) return null;
        Product p = variant.getProduct();

        // Resolve color-specific image from product_images — never reads
        // the stale products.image_url column.
        String color    = variant.getColor();
        String imageUrl = p.getProductImages() == null ? null :
                p.getProductImages().stream()
                        .filter(img -> img.getColor() != null
                                && img.getColor().equalsIgnoreCase(color))
                        .findFirst()
                        .map(ProductImage::getImageUrl)
                        .orElse(null);

        return new ProductCoreDTO(
                p.getId(),
                p.getName(),
                "rentawear",
                p.getBasePrice().doubleValue(),
                imageUrl,
                p.getCategory().getGender().toString(),
                p.getCategory().getId(),
                p.getCategory().getName()
        );
    }
}