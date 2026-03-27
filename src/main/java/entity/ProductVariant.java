package entity;

import jakarta.persistence.*;

@NamedQueries({

        //Single-product reads

        // All variants for one product, ordered by id ASC.
        // Used by PDP and admin detail,  provides full colour+size+quantity data.
        @NamedQuery(
                name  = "ProductVariant.findByProductId",
                query = "SELECT pv FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "ORDER BY pv.id ASC"
        ),

        // Distinct colour strings for one product.
        // Used by upload validation to confirm a colour exists before accepting
        // image uploads for it.
        @NamedQuery(
                name  = "ProductVariant.findDistinctColorsByProductId",
                query = "SELECT DISTINCT pv.color FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "ORDER BY pv.color ASC"
        ),

        // All sizes for one specific colour.
        // Returns all sizes regardless of stock — use findAvailableSizesByProductIdAndColor
        // when you need in-stock only.
        @NamedQuery(
                name  = "ProductVariant.findSizesByProductIdAndColor",
                query = "SELECT pv.size FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "AND   pv.color      = :color " +
                        "ORDER BY pv.id ASC"
        ),

        // In-stock sizes only (quantity > 0) for one colour.
        // Used to drive the disabled state on PDP size buttons.
        @NamedQuery(
                name  = "ProductVariant.findAvailableSizesByProductIdAndColor",
                query = "SELECT pv.size FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "AND   pv.color      = :color " +
                        "AND   pv.quantity   > 0 " +
                        "ORDER BY pv.id ASC"
        ),

        // Batch reads

        // All variants for MULTIPLE products in one query.
        // Used by ProductVariantService.getByProductIds() to build the
        // variantsByProductId map without N+1 on admin listing pages.
        // Results ordered by product.id ASC, variant.id ASC so grouping in
        // memory is sequential.
        @NamedQuery(
                name  = "ProductVariant.findByProductIds",
                query = "SELECT pv FROM ProductVariant pv " +
                        "WHERE pv.product.id IN :pids " +
                        "ORDER BY pv.product.id ASC, pv.id ASC"
        ),

        // Writes

        // Deletes all size rows for a colour.
        // Always called alongside ProductImage.deleteByProductIdAndColor —
        // transaction scope enforced by EntityManagerFilter.
        @NamedQuery(
                name  = "ProductVariant.deleteByProductIdAndColor",
                query = "DELETE FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "AND   pv.color      = :color"
        ),

        // Deletes all variants for a product (used when deleting entire product)
        @NamedQuery(
                name  = "ProductVariant.deleteByProductId",
                query = "DELETE FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid"
        )
})
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "size", length = 20)
    private String size;

    @Column(name = "color", length = 50, nullable = false)
    private String color;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariant that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}