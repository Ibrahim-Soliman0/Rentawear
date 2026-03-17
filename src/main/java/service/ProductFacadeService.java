package service;

import dto.*;
import entity.Product;
import entity.ProductImage;
import entity.ProductVariant;
import mapper.ProductMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Orchestrates ProductService, ProductVariantService, and ProductImageService.
// This is the only class that:
//   1. Calls more than one service in a single operation
//   2. Calls the mapper to assemble final DTOs
//   3. Is called directly by servlets
public class ProductFacadeService {

    private final ProductService        productService;
    private final ProductVariantService variantService;
    private final ProductImageService   imageService;
    private final ProductMapper         mapper;

    public ProductFacadeService(ProductService productService,
                                ProductVariantService variantService,
                                ProductImageService imageService,
                                ProductMapper mapper) {
        this.productService = productService;
        this.variantService = variantService;
        this.imageService   = imageService;
        this.mapper         = mapper;
    }

    // ── Catalog listing ───────────────────────────────────────────────────────
    //
    // Batch strategy to avoid N+1:
    //   1. Fetch the page of products (2-step pagination in repo)
    //   2. Collect all product ids on that page
    //   3. Fetch primary images for ALL those ids in ONE query
    //   4. Group images by product id in memory
    //   5. Map each product with its pre-fetched images
    //
    // Result: 3 queries total regardless of page size
    //   (id query + entity fetch + batch image fetch)

    public ProductListResult getProducts(ProductFilterDTO filter) {
        List<Product> products = fetchProducts(filter);
        long          total    = countProducts(filter);
        PriceRangeDTO range    = productService.getMinMaxPrice(filter);

        // Batch image fetch -> single query for the whole page
        List<Integer> ids = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        Map<Integer, List<ProductImage>> imagesByProductId =
                imageService.getPrimaryPerColorForProducts(ids);

        List<ProductCardDTO> cards = products.stream()
                .map(p -> mapper.toCardDTO(
                        p,
                        imagesByProductId.getOrDefault(p.getId(), List.of())
                ))
                .collect(Collectors.toList());

        return new ProductListResult(cards, total, range, filter.page(), filter.pageSize());
    }

    // Returns ProductSearchDTO ->  no swatches or price range needed in the modal.

    public ProductSearchResult searchProducts(ProductFilterDTO filter) {
        List<Product> products = productService.searchFiltered(filter);
        long          total    = productService.countSearchFiltered(filter);

        List<ProductSearchDTO> results = products.stream()
                .map(mapper::toSearchDTO)
                .collect(Collectors.toList());

        return new ProductSearchResult(results, total, filter.page(), filter.pageSize());
    }


    // Fetches product + all variants + all images — three queries, always.

    public ProductDetailDTO getProductDetail(int productId) {
        Product              product  = productService.getById(productId);
        List<ProductVariant> variants = variantService.getByProductId(productId);
        List<ProductImage>   images   = imageService.getByProductId(productId);
        return mapper.toDetailDTO(product, variants, images);
    }


    public ProductListResult getAdminProducts(ProductFilterDTO filter) {
        List<Product> products = fetchProducts(filter);
        long          total    = countProducts(filter);
        PriceRangeDTO range    = productService.getMinMaxPrice(filter);

        List<Integer> ids = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        Map<Integer, List<ProductImage>>   imagesByProductId   =
                imageService.getPrimaryPerColorForProducts(ids);
        Map<Integer, List<ProductVariant>> variantsByProductId =
                variantService.getByProductIds(ids);

        List<ProductCardDTO> cards = products.stream()
                .map(p -> mapper.toCardDTO(
                        p,
                        imagesByProductId.getOrDefault(p.getId(), List.of())
                ))
                .collect(Collectors.toList());

        return new ProductListResult(cards, total, range, filter.page(), filter.pageSize());
    }

    // Admin product detail

    public AdminProductDetailDTO getAdminDetail(int productId) {
        Product              product  = productService.getById(productId);
        List<ProductVariant> variants = variantService.getByProductId(productId);
        return mapper.toAdminDetailDTO(product, variants);
    }

    // Colour deletion
    // Deletes both variants and images for a colour in one transaction.
    // The EntityManagerFilter wraps the entire request in a transaction, so if
    // either delete throws, both are rolled back automatically.

    public void deleteColor(int productId, String color) {
        variantService.deleteColor(productId, color);
        imageService.deleteColorImages(productId, color);
    }

    // ── Private routing helpers ───────────────────────────────────────────────
    // These mirror the ProductFilterDTO routing flags so the logic lives
    // in one place and both getProducts() and getAdminProducts() stay clean.

    private List<Product> fetchProducts(ProductFilterDTO f) {
        if (f.isSearch())        return productService.searchFiltered(f);
        if (f.isNewOnly())       return productService.findNew(f.pageSize());
        if (f.isInterestBased()) return productService.findByInterests(f);
        return productService.findFiltered(f);
    }

    private long countProducts(ProductFilterDTO f) {
        if (f.isSearch())        return productService.countSearchFiltered(f);
        if (f.isNewOnly())       return productService.countNew();
        if (f.isInterestBased()) return productService.countByInterests(f);
        return productService.countFiltered(f);
    }
}