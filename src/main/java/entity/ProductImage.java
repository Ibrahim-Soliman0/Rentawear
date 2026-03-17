package entity;

import jakarta.persistence.*;

import java.util.Objects;

@NamedQueries({

        // Single-product reads

        // Full image list for one product ordered by id ASC.
        // Service groups the flat list into imagesByColor map (LinkedHashMap
        // preserves insertion order so the first group = default colour).
        @NamedQuery(
                name  = "ProductImage.findByProductId",
                query = "SELECT pi FROM ProductImage pi " +
                        "WHERE pi.product.id = :pid " +
                        "ORDER BY pi.id ASC"
        ),

        // All images for one specific colour, ordered by id ASC.
        @NamedQuery(
                name  = "ProductImage.findByProductIdAndColor",
                query = "SELECT pi FROM ProductImage pi " +
                        "WHERE pi.product.id = :pid " +
                        "AND   pi.color      = :color " +
                        "ORDER BY pi.id ASC"
        ),

        @NamedQuery(
                name  = "ProductImage.findPrimaryPerColor",
                query = "SELECT pi FROM ProductImage pi " +
                        "WHERE pi.product.id = :pid " +
                        "AND   pi.id = (" +
                        "  SELECT MIN(pi2.id) FROM ProductImage pi2 " +
                        "  WHERE pi2.product.id = pi.product.id " +
                        "  AND   pi2.color      = pi.color" +
                        ") " +
                        "ORDER BY pi.id ASC"
        ),

        // Batch reads

        // Lowest-id image per colour for MULTIPLE products in one query.
        // Eliminates N+1 on catalog listing pages where findPrimaryPerColor
        // would otherwise be called once per product.
        //
        // Same correlated-MIN pattern as above but scoped to a list of product
        // ids. Results are ordered by product.id ASC, image.id ASC so that
        // ProductImageService.getPrimaryPerColorForProducts() can group them
        // sequentially without sorting.
        @NamedQuery(
                name  = "ProductImage.findPrimaryPerColorForProducts",
                query = "SELECT pi FROM ProductImage pi " +
                        "WHERE pi.product.id IN :pids " +
                        "AND   pi.id = (" +
                        "  SELECT MIN(pi2.id) FROM ProductImage pi2 " +
                        "  WHERE pi2.product.id = pi.product.id " +
                        "  AND   pi2.color      = pi.color" +
                        ") " +
                        "ORDER BY pi.product.id ASC, pi.id ASC"
        ),

        // **** Not sure if I would need to verify that in server uploads
        // decide whether the uploaded image is the
        // first for a given colour
        @NamedQuery(
                name  = "ProductImage.countByProductIdAndColor",
                query = "SELECT COUNT(pi) FROM ProductImage pi " +
                        "WHERE pi.product.id = :pid " +
                        "AND   pi.color      = :color"
        ),

        // Writes

        // Bulk delete by colour — called when an admin removes a colour variant.
        // Transaction must be active (enforced by EntityManagerFilter).
        @NamedQuery(
                name  = "ProductImage.deleteByProductIdAndColor",
                query = "DELETE FROM ProductImage pi " +
                        "WHERE pi.product.id = :pid " +
                        "AND   pi.color      = :color"
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