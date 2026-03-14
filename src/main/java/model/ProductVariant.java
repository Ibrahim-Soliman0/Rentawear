package model;

import jakarta.persistence.*;

@NamedQueries({

        @NamedQuery(
                name = "ProductVariant.findByProductId",
                query = "SELECT pv FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "ORDER BY pv.id ASC"
        ),

        // for rendering color swatches
        @NamedQuery(
                name = "ProductVariant.findDistinctColorsByProductId",
                query = "SELECT DISTINCT pv.color FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "ORDER BY pv.color ASC"
        ),

        // Used when a color variant is deleted, remove all size rows for that color
        @NamedQuery(
                name = "ProductVariant.deleteByProductIdAndColor",
                query = "DELETE FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "AND pv.color = :color"
        ),

        // sizes available for one specific color
        @NamedQuery(
                name = "ProductVariant.findSizesByProductIdAndColor",
                query = "SELECT pv.size FROM ProductVariant pv " +
                        "WHERE pv.product.id = :pid " +
                        "AND pv.color = :color " +
                        "ORDER BY pv.id ASC"
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