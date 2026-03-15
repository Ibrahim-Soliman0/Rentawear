package entity;

import jakarta.persistence.*;

import java.util.Objects;

@NamedQueries({

        // Full image list for a product — used by getGroupedByColor in service
        @NamedQuery(
                name = "ProductImage.findByProductId",
                query = "SELECT pi FROM ProductImage pi " +
                        "WHERE pi.product.id = :pid " +
                        "ORDER BY pi.color ASC, pi.id ASC"
        ),

        // Primary image per color — lowest id per color group
        // Note: uses native query in impl (see below) — this is here for documentation
        // Named native queries are defined separately via @NamedNativeQuery
        @NamedQuery(
                name = "ProductImage.findByProductIdAndColor",
                query = "SELECT pi FROM ProductImage pi " +
                        "WHERE pi.product.id = :pid " +
                        "AND pi.color = :color " +
                        "ORDER BY pi.id ASC"
        ),

        // Count images for a product+color — used to determine if uploaded
        // image is the first (and therefore becomes primary)
        @NamedQuery(
                name = "ProductImage.countByProductIdAndColor",
                query = "SELECT COUNT(pi) FROM ProductImage pi " +
                        "WHERE pi.product.id = :pid " +
                        "AND pi.color = :color"
        ),

        // Bulk delete by color — called when admin removes a color variant
        @NamedQuery(
                name = "ProductImage.deleteByProductIdAndColor",
                query = "DELETE FROM ProductImage pi " +
                        "WHERE pi.product.id = :pid " +
                        "AND pi.color = :color"
        )
})
@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "color", length = 50, nullable = false)
    private String color;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductImage that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}