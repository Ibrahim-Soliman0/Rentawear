package entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

// ── Named query strategy ──────────────────────────────────────────────────────
//
// All listing queries use TWO-STEP PAGINATION to avoid HHH90003004.
//
// Step 1 -->>> *Ids queries: SELECT p.id only, LEFT JOIN (no FETCH).
//   Hibernate can push LIMIT/OFFSET directly to SQL because there is no
//   collection FETCH join. Returns List<Integer>.
//
// Step 2 -->>> findByIds: JOIN FETCH on the page-sized ID list, NO pagination.
//   Safe because setMaxResults is never called on a FETCH query.
//
// Count and search queries never use FETCH joins — they only need scalars.
//
// All filters go through LEFT JOIN p.category c and use c.gender.

@NamedQueries({

        // ── Step 2 entity fetch (shared by all listing paths)
        // Called after any *Ids query returns a page-sized list.
        // JOIN FETCH productVariants + category so no lazy loads are triggered
        // during mapper execution.
        // ORDER BY restores sort after the IN clause reorders rows.
        @NamedQuery(
                name  = "Product.findByIds",
                query = "SELECT DISTINCT p FROM Product p " +
                        "LEFT JOIN FETCH p.productVariants " +
                        "LEFT JOIN FETCH p.category " +
                        "WHERE p.id IN :ids " +
                        "ORDER BY p.createdAt DESC"
        ),

        // ── New arrivals ──────────────────────────────────────────────────────
        @NamedQuery(
                name  = "Product.findNewIds",
                query = "SELECT p.id FROM Product p " +
                        "WHERE p.createdAt >= :cutoff " +
                        "ORDER BY p.createdAt DESC"
        ),
        @NamedQuery(
                name  = "Product.countNew",
                query = "SELECT COUNT(p) FROM Product p " +
                        "WHERE p.createdAt >= :cutoff"
        ),

        //Filtered browse (no category filter)
        @NamedQuery(
                name  = "Product.findFilteredIds",
                query = "SELECT p.id FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE (:gender   IS NULL OR c.gender     = :gender) " +
                        "AND   (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND   (:maxPrice IS NULL OR p.basePrice <= :maxPrice) " +
                        "ORDER BY p.createdAt DESC"
        ),
        @NamedQuery(
                name  = "Product.countFiltered",
                query = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE (:gender   IS NULL OR c.gender     = :gender) " +
                        "AND   (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND   (:maxPrice IS NULL OR p.basePrice <= :maxPrice)"
        ),

        //Filtered browse with category
        @NamedQuery(
                name  = "Product.findByCategoriesIds",
                query = "SELECT p.id FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE c.id IN :ids " +
                        "AND (:gender   IS NULL OR c.gender     = :gender) " +
                        "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice) " +
                        "ORDER BY p.createdAt DESC"
        ),
        @NamedQuery(
                name  = "Product.countByCategories",
                query = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE c.id IN :ids " +
                        "AND (:gender   IS NULL OR c.gender     = :gender) " +
                        "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)"
        ),

        //Personalized / interest-based feed
        @NamedQuery(
                name  = "Product.findByInterestsIds",
                query = "SELECT p.id FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE c.id IN :ids " +
                        "AND (:gender IS NULL OR c.gender = :gender) " +
                        "ORDER BY p.createdAt DESC"
        ),
        @NamedQuery(
                name  = "Product.countByInterests",
                query = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE c.id IN :ids " +
                        "AND (:gender IS NULL OR c.gender = :gender)"
        ),

        // Search (no category filter)
        @NamedQuery(
                name  = "Product.searchFilteredIds",
                query = "SELECT p.id FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE LOWER(p.name) LIKE :q " +
                        "AND (:gender   IS NULL OR c.gender     = :gender) " +
                        "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice) " +
                        "ORDER BY p.createdAt DESC"
        ),
        @NamedQuery(
                name  = "Product.countSearchFiltered",
                query = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE LOWER(p.name) LIKE :q " +
                        "AND (:gender   IS NULL OR c.gender     = :gender) " +
                        "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)"
        ),

        // ── Search with category filter ───────────────────────────────────────
        @NamedQuery(
                name  = "Product.searchByCategoriesIds",
                query = "SELECT p.id FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE LOWER(p.name) LIKE :q " +
                        "AND c.id IN :ids " +
                        "AND (:gender   IS NULL OR c.gender     = :gender) " +
                        "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice) " +
                        "ORDER BY p.createdAt DESC"
        ),
        @NamedQuery(
                name  = "Product.countSearchFilteredByCategories",
                query = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE LOWER(p.name) LIKE :q " +
                        "AND c.id IN :ids " +
                        "AND (:gender   IS NULL OR c.gender     = :gender) " +
                        "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)"
        ),

        // ── Price range aggregates ────────────────────────────────────────────
        // Returns Object[] { MIN(basePrice), MAX(basePrice) }
        @NamedQuery(
                name  = "Product.getMinMaxPrice",
                query = "SELECT MIN(p.basePrice), MAX(p.basePrice) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE (:gender IS NULL OR c.gender = :gender)"
        ),
        @NamedQuery(
                name  = "Product.getMinMaxPriceByCategories",
                query = "SELECT MIN(p.basePrice), MAX(p.basePrice) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE c.id IN :ids " +
                        "AND (:gender IS NULL OR c.gender = :gender)"
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

    // TODO: add brand column — ALTER TABLE products ADD COLUMN brand VARCHAR(100)
    // @Column(name = "brand", length = 100)
    // private String brand;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "product",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE},
            orphanRemoval = true)
    @OrderBy("id ASC")
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

    public List<ProductVariant> getProductVariants() {
        return productVariants;
    }

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