package dto;

import model.Product;
import model.ProductVariant;
import util.ImagePathUtil;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductDTO {
    public int    id;
    public String name;
    public String brand;
    public double pricePerDay;
    public boolean isNew;
    public String imageUrl;
    public String description;
    public String category;
    public List<SwatchDTO> swatches;
    public List<String>    sizes;
    public static final int NEW_THRESHOLD_DAYS =30;

    public static ProductDTO from(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.id          = p.getId();
        dto.name        = p.getName();
        dto.brand       = "rentawear"; // hardcoded for now - could be a field in Product later
        dto.pricePerDay = p.getBasePrice().doubleValue();
        dto.imageUrl    = (p.getImageUrl() != null && !p.getImageUrl().isEmpty())
                ? p.getImageUrl()
                : ImagePathUtil.PLACEHOLDER_BASE;

        dto.isNew = p.getCreatedAt() != null
                && p.getCreatedAt().isAfter(
                java.time.Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS));

        dto.description = p.getDescription() != null
                ? p.getDescription().substring(0, Math.min(200, p.getDescription().length()))
                : null;

        dto.category = p.getCategory() != null ? p.getCategory().getName() : null;

        dto.swatches = p.getProductVariants().stream()
                .map(ProductVariant::getColor)
                .distinct()
                .map(SwatchDTO::from)
                .collect(Collectors.toList());

        dto.sizes = p.getProductVariants().stream()
                .map(ProductVariant::getSize)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return dto;
    }
}