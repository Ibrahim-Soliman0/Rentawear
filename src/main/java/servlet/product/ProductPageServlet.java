package servlet.product;

import dto.ProductDetailDTO;
import dto.ProductFilterDTO;
import dto.ProductListResult;
import entity.Category;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ProductFacadeService;
import util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serves the product detail page (PDP) at GET /product/{id}.
 *
 * Distinct from ProductDetailServlet (/products/{id}) which returns JSON
 * only and is used by quick-view.js. This servlet returns HTML.
 *
 * Sets request attributes:
 *   productJson   — full ProductDetailDTO as JSON for window.RW_PRODUCT
 *   relatedJson   — List<ProductCardDTO> as JSON for window.RW_RELATED
 *   pageTitle     — product name for <title>
 *
 * Related products: same category, same gender, first 5 results,
 * current product excluded client-side in product.js.
 */
@WebServlet("/product/*")
public class ProductPageServlet extends HttpServlet {

    private ProductFacadeService facade;

    @Override
    public void init() {
        facade = (ProductFacadeService) getServletContext()
                .getAttribute("productFacadeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer id = parseId(req.getPathInfo(), resp);
        if (id == null) return;

        ProductDetailDTO detail = facade.getProductDetail(id);
        if (detail == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
            return;
        }

        /* ── Related products ── */
        String relatedJson = "[]";
        try {
            int    categoryId = detail.core().categoryId();
            String gender     = detail.core().gender();

            ProductFilterDTO relatedFilter = new ProductFilterDTO(
                    null,                              // q
                    gender,                            // gender
                    List.of(categoryId),               // categoryIds
                    null, null,                        // price range
                    null,                              // newOnly
                    null,                              // interestIds
                    0, 5                               // page, pageSize (fetch 5, exclude self client-side)
            );

            ProductListResult related = facade.getProducts(relatedFilter);
            relatedJson = JsonUtil.toJson(
                    related.products().stream()
                            .filter(p -> p.core().id() != id)
                            .limit(4)
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            System.err.println("[ProductPageServlet] related products failed: " + e.getMessage());
        }

        req.setAttribute("productJson",  JsonUtil.toJson(detail));
        req.setAttribute("relatedJson",  relatedJson);
        req.setAttribute("pageTitle",    detail.core().name());

        req.getRequestDispatcher("/WEB-INF/product.jsp").forward(req, resp);
    }

    private Integer parseId(String pathInfo, HttpServletResponse resp)
            throws IOException {
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Product id required");
            return null;
        }
        String segment = pathInfo.substring(1).split("/")[0];
        if (!segment.matches("\\d+")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product id");
            return null;
        }
        try {
            return Integer.parseInt(segment);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product id");
            return null;
        }
    }
}