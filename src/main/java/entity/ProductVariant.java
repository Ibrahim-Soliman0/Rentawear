package entity;

import jakarta.persistence.*;

@NamedQueries({

        // ── Single-product reads (all filter v.deleted = false) ───────────────

        // All active variants for one product, ordered by id ASC.
        // Used by PDP and admin detail — provides full colour+size+quantity data.
        @NamedQuery(
                name  = "ProductVariant.findByProductId",
                query = "SELECT pv FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "AND pv.deleted = false " +
                        "ORDER BY pv.id ASC"
        ),

        // Distinct colour strings for one product (active variants only).
        @NamedQuery(
                name  = "ProductVariant.findDistinctColorsByProductId",
                query = "SELECT DISTINCT pv.color FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "AND pv.deleted = false " +
                        "ORDER BY pv.color ASC"
        ),

        // All sizes for one specific colour (active only).
        @NamedQuery(
                name  = "ProductVariant.findSizesByProductIdAndColor",
                query = "SELECT pv.size FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "AND pv.color      = :color " +
                        "AND pv.deleted    = false " +
                        "ORDER BY pv.id ASC"
        ),

        // In-stock sizes only (quantity > 0) for one colour (active only).
        @NamedQuery(
                name  = "ProductVariant.findAvailableSizesByProductIdAndColor",
                query = "SELECT pv.size FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "AND pv.color      = :color " +
                        "AND pv.quantity   > 0 " +
                        "AND pv.deleted    = false " +
                        "ORDER BY pv.id ASC"
        ),

        // ── Batch reads ───────────────────────────────────────────────────────

        // All active variants for MULTIPLE products in one query.
        @NamedQuery(
                name  = "ProductVariant.findByProductIds",
                query = "SELECT pv FROM ProductVariant pv " +
                        "WHERE pv.product.id IN :pids " +
                        "AND pv.deleted = false " +
                        "ORDER BY pv.product.id ASC, pv.id ASC"
        ),

        // ── ID-only query used before soft-deleting a color ───────────────────
        // Returns IDs of active variants for a given product+color.
        // Called by the facade to collect which cart_items to purge first.
        @NamedQuery(
                name  = "ProductVariant.findIdsByProductIdAndColor",
                query = "SELECT pv.id FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "AND pv.color     = :color " +
                        "AND pv.deleted   = false"
        ),

        // ── Soft-delete (UPDATE — keeps rows so order_items FK stays valid) ───

        @NamedQuery(
                name  = "ProductVariant.softDeleteByProductIdAndColor",
                query = "UPDATE ProductVariant pv SET pv.deleted = true " +
                        "WHERE pv.product.id = :pid " +
                        "AND pv.color = :color"
        ),

        @NamedQuery(
                name  = "ProductVariant.softDeleteByProductId",
                query = "UPDATE ProductVariant pv SET pv.deleted = true " +
                        "WHERE pv.product.id = :pid"
        ),

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

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isDeleted(){
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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