package servlet;

import dto.ProductDTO;
import dto.ProductImagesDTO;
//import dto.UserDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import repository.impl.ProductImageRepositoryImpl;
import repository.impl.ProductRepositoryImpl;
import repository.impl.ProductVariantRepositoryImpl;
import service.ProductImageService;
import service.ProductService;
import util.JsonUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/ProductServlet")
public class ProductServlet extends HttpServlet {

    private ProductService      productService;
    private ProductImageService productImageService;


    @Override
    public void init() {
        ProductRepositoryImpl        productRepo = new ProductRepositoryImpl();
        ProductImageRepositoryImpl   imageRepo   = new ProductImageRepositoryImpl();
        ProductVariantRepositoryImpl variantRepo = new ProductVariantRepositoryImpl();

        productService      = new ProductService(productRepo);
        productImageService = new ProductImageService(imageRepo, variantRepo, productRepo);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String action = req.getParameter("action");
        int    limit  = parseIntOrDefault(req.getParameter("limit"),  12);
        int    offset = parseIntOrDefault(req.getParameter("offset"),  0);

        try {
            if      ("images".equals(action))    handleImages(req, resp);
            else if ("priceRange".equals(action)) handlePriceRange(req, resp);
            else if ("search".equals(action))    handleSearch(req, resp);
//            else if ("interests".equals(action)) handleInterests(req, resp, limit);
            else                                 handleList(req, resp, limit, offset);
        } catch (Exception e) {
            resp.setStatus(500);
            JsonUtil.writeJson(resp, Map.of(
                    "error", "internal",
                    "msg",   e.getMessage() != null ? e.getMessage() : "unknown error"));
        }
    }

    // Handlers

    /**
     * Default action. Handles every catalog and home-strip scenario:
     *
     *   Home new arrivals strip  ?sort=new
     *   Home women's strip       ?category=women
     *   Home men's strip         ?category=men
     *   Catalog — all            (no extra params)
     *   Catalog — by gender      ?category=women|men
     *   Catalog — by categories  ?categoryIds=3,7,12
     *   Catalog — women's shoes  ?category=women&categoryIds=5
     *   Catalog — price filter   ?minPrice=20&maxPrice=80
     *   Catalog — combined       ?category=women&categoryIds=5&minPrice=20
     *   Catalog — paginated      ?limit=12&offset=24
     *
     * Always returns: { "results": ProductDTO[], "total": long }
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp,
                            int limit, int offset) throws IOException {

        String        sort     = req.getParameter("sort");
        String        gender   = toGender(req.getParameter("category"));
        List<Integer> catIds   = parseCategoryIds(req.getParameter("categoryIds"));
        Double        minPrice = parseDoubleOrNull(req.getParameter("minPrice"));
        Double        maxPrice = parseDoubleOrNull(req.getParameter("maxPrice"));

        List<ProductDTO> dtos;
        long total;

        if ("new".equals(sort)) {
            int days = parseIntOrDefault(req.getParameter("days"), ProductDTO.NEW_THRESHOLD_DAYS);
            dtos  = productService.getNew(limit, days);
            total = dtos.size();
        } else {

            dtos  = productService.getFiltered(gender, catIds, minPrice, maxPrice, limit, offset);
            total = productService.countFiltered(gender, catIds, minPrice, maxPrice);
        }

        JsonUtil.writeJson(resp, Map.of("results", dtos, "total", total));
    }

    /**
     * Returns all image groups for a product.
     * Called asynchronously by quick-view when the overlay opens.
     *
     *   ?action=images&id=42
     *
     * Cached for 5 minutes — image data only changes when the admin
     * uploads or deletes, which is infrequent.
     *
     * Returns: ProductImagesDTO
     */
    private void handleImages(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = parseIntOrDefault(req.getParameter("id"), -1);
        if (id < 0) {
            resp.sendError(400, "Missing or invalid id");
            return;
        }

        ProductImagesDTO dto = productImageService.getImagesDTOForProduct(id);
        if (dto == null) {
            resp.sendError(404, "Product not found");
            return;
        }

        resp.setHeader("Cache-Control", "public, max-age=300");
        JsonUtil.writeJson(resp, dto);
    }

    /**
     * Handles both the search modal preview and the full search results page.
     *
     *   Modal preview (small slice, no offset):
     *     ?action=search&q=dress&category=women&limit=5
     *
     *   Search results page (paginated):
     *     ?action=search&q=dress&category=women&limit=12&offset=0&paged=true
     *
     * Optional filters (when supported by the backend):
     *   - minPrice: minimum price (inclusive)
     *   - maxPrice: maximum price (inclusive)
     *   - categoryIds: comma-separated list of category IDs (e.g. "1,2,5")
     *
     * Minimum query length is 2 characters - returns empty result below that.
     *
     * Returns: { "results": ProductDTO[], "total": long }
     */
    private void handleSearch(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String  q      = req.getParameter("q");
        String  gender = toGender(req.getParameter("category"));
        int     limit  = parseIntOrDefault(req.getParameter("limit"),  6);
        int     offset = parseIntOrDefault(req.getParameter("offset"), 0);
        boolean paged  = "true".equals(req.getParameter("paged"));

        // Optional price range filters
        Double minPrice = parseDoubleOrNull(req.getParameter("minPrice"));
        Double maxPrice = parseDoubleOrNull(req.getParameter("maxPrice"));

        // Optional category ID filters, comma-separated (e.g. "1,2,3")
        List<Integer> categoryIds = parseCategoryIds(req.getParameter("categoryIds"));

        if (q == null || q.trim().length() < 2) {
            JsonUtil.writeJson(resp, Map.of("results", List.of(), "total", 0));
            return;
        }

        String term = q.trim();

        List<ProductDTO> dtos = paged
                ? productService.searchPaged(term, gender, limit, offset)
                : productService.search(term, gender, limit);

        long total = productService.countSearch(term, gender);

        JsonUtil.writeJson(resp, Map.of("results", dtos, "total", total));
    }

//    /**
//     * Parses a string into a Double, returning null for null/blank/invalid values.
//     */
//    private Double parseDoubleOrNull(String value) {
//        if (value == null) {
//            return null;
//        }
//        String trimmed = value.trim();
//        if (trimmed.isEmpty()) {
//            return null;
//        }
//        try {
//            return Double.valueOf(trimmed);
//        } catch (NumberFormatException ex) {
//            // Invalid input - treat as "no filter" rather than failing the entire request
//            return null;
//        }
//    }
//
//    /**
//     * Parses a comma-separated list of category IDs into a List<Integer>.
//     * Returns null when the input is null/blank or contains no valid IDs.
//     */
//    private List<Integer> parseCategoryIds(String raw) {
//        if (raw == null) {
//            return null;
//        }
//        String trimmed = raw.trim();
//        if (trimmed.isEmpty()) {
//            return null;
//        }
//
//        List<Integer> ids = Arrays.stream(trimmed.split(","))
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
//                .map(s -> {
//                    try {
//                        return Integer.valueOf(s);
//                    } catch (NumberFormatException ex) {
//                        // Skip invalid IDs instead of failing the entire parse
//                        return null;
//                    }
//                })
//                .filter(id -> id != null)
//                .collect(Collectors.toList());
//
//        return ids.isEmpty() ? null : ids;
//    }
//
//    /**
//     * Returns personalised recommendations for the logged-in user.
//     * Filtered by the user's saved interest categories and their gender
//     * so women only see women's products and vice versa.
//     *
//     *   ?action=interests&limit=8
//     *
//     * Returns an empty list silently for guests or users with no interests.
//     * The home page section hides itself when it receives an empty list.
//     *
//     * Returns: ProductDTO[]
//     */



//    private void handleInterests(HttpServletRequest req, HttpServletResponse resp,
//                                 int limit) throws IOException {
//
//        HttpSession session = req.getSession(false);
//        if (session == null) {
//            JsonUtil.writeJson(resp, List.of());
//            return;
//        }
//
//        // Session stores UserDTO — never the User entity, which would be
//        // detached and throw LazyInitializationException on collection access
//        UserDTO user = (UserDTO) session.getAttribute("currentUser");
//
//        if (user == null
//                || user.interestCategoryIds == null
//                || user.interestCategoryIds.isEmpty()) {
//            JsonUtil.writeJson(resp, List.of());
//            return;
//        }
//
//        // user.gender stored as "FEMALE" or "MALE" on UserDTO at login time
//        List<ProductDTO> dtos = productService.getByInterests(
//                user.interestCategoryIds,
//                user.gender,
//                limit);
//
//        JsonUtil.writeJson(resp, dtos);
//    }

    private void handlePriceRange(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String gender = toGender(req.getParameter("category"));
        List<Integer> catIds = parseCategoryIds(req.getParameter("categoryIds"));
        dto.PriceRangeDTO pr = productService.getPriceRange(gender, catIds);
        JsonUtil.writeJson(resp, Map.of("min", pr.min, "max", pr.max));
    }

    //Helpers

    /**
     * Maps the frontend category param to the Gender enum name used by
     * the repository. Returns null for "all" or any unrecognised value —
     * null is safe because named queries handle it with IS NULL OR.
     */
    private String toGender(String category) {
        if ("women".equals(category)) return "FEMALE";
        if ("men".equals(category))   return "MALE";
        return null;
    }

    /**
     * Parses a comma-separated list of category IDs.
     * Returns null (not empty list) on blank input so the IS NULL OR
     * pattern in named queries treats it as "no category filter".
     * Returns null on parse error so malformed input silently falls
     * back to no filter rather than causing a 500.
     */
    private List<Integer> parseCategoryIds(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try {
            return Arrays.stream(val.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a string to int. Returns the default on null or bad input.
     */
    private int parseIntOrDefault(String val, int def) {
        try { return val != null ? Integer.parseInt(val.trim()) : def; }
        catch (NumberFormatException e) { return def; }
    }

    /**
     * Parses a string to Double. Returns null on blank or bad input.
     * Null is safe to pass through — named queries treat it as no filter.
     */
    private Double parseDoubleOrNull(String val) {
        try {
            return (val != null && !val.trim().isEmpty())
                    ? Double.parseDouble(val.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}