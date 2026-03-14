package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
@NamedQueries({

        @NamedQuery(
                name = "Product.findNew",
                query = "SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.productVariants " +
                        "LEFT JOIN FETCH p.category " +
                        "WHERE p.createdAt >= :cutoff " +
                        "ORDER BY p.createdAt DESC"
        ),

        @NamedQuery(
                name = "Product.findFiltered",
                query = "SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.productVariants " +
                        "LEFT JOIN FETCH p.category c " +
                        "WHERE (:gender    IS NULL OR c.gender        = :gender) " +
                        "AND   (:minPrice  IS NULL OR p.basePrice    >= :minPrice) " +
                        "AND   (:maxPrice  IS NULL OR p.basePrice    <= :maxPrice) " +
                        "ORDER BY p.createdAt DESC"
        ),

        @NamedQuery(
                name = "Product.countFiltered",
                query = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE (:gender    IS NULL OR c.gender        = :gender) " +
                        "AND   (:minPrice  IS NULL OR p.basePrice    >= :minPrice) " +
                        "AND   (:maxPrice  IS NULL OR p.basePrice    <= :maxPrice)"
        ),

        @NamedQuery(
                name = "Product.searchPaged",
                query = "SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.productVariants " +
                        "LEFT JOIN FETCH p.category c " +
                        "WHERE (LOWER(p.name) LIKE :q) " +
                        "AND   (:gender IS NULL OR c.gender = :gender) " +
                        "ORDER BY p.createdAt DESC"
        ),

        @NamedQuery(
                name = "Product.countSearch",
                query = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE (LOWER(p.name) LIKE :q) " +
                        "AND   (:gender IS NULL OR c.gender = :gender)"
        ),

        @NamedQuery(
                name = "Product.findByInterests",
                query = "SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.productVariants " +
                        "LEFT JOIN FETCH p.category c " +
                        "WHERE c.id IN :ids " +
                        "AND (:gender IS NULL OR c.gender = :gender) " +
                        "ORDER BY p.createdAt DESC"
        ),

        @NamedQuery(
                name = "Product.findByCategories",
                query = "SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.productVariants " +
                        "LEFT JOIN FETCH p.category c " +
                        "WHERE c.id IN :ids " +
                        "AND (:gender   IS NULL OR c.gender     = :gender) " +
                        "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice) " +
                        "ORDER BY p.createdAt DESC"
        ),

        @NamedQuery(
                name = "Product.countByCategories",
                query = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE c.id IN :ids " +
                        "AND (:gender   IS NULL OR c.gender     = :gender) " +
                        "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)"
        )
})
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "image_url")
    private String imageUrl;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "product",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE},
            orphanRemoval = true)
    @OrderBy("id ASC") // to ensure we always get the first image uploaded per color
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE},
            orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ProductVariant> productVariants = new ArrayList<>();

    public void addProductVariant(ProductVariant productVariant) {
        productVariant.setProduct(this);
        productVariants.add(productVariant);
    }

    public void removeProductVariant(ProductVariant productVariant) {
        productVariants.remove(productVariant);
        productVariant.setProduct(null);
    }

    public void addProductImage(ProductImage productImage, String color) {
        productImage.setProduct(this);
        productImage.setColor(color);
        productImages.add(productImage);
    }

    public void removeProductImage(ProductImage productImage) {
        productImages.remove(productImage);
        productImage.setProduct(null);
    }

    public List<ProductImage> getProductImages() {
        return productImages;
    }

    public List<ProductVariant> getProductVariants() {return productVariants;}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return id != null && id.equals(product.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}