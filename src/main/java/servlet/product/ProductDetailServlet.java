package servlet.product;

import dto.ProductDetailDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ProductFacadeService;
import util.JsonUtil;

import java.io.IOException;

// GET /products/{id}
//
// Returns ProductDetailDTO for the product detail page (PDP).
// Fetches product + all variants + all images — three queries.
//
// Path format: /products/42
// The id is parsed from pathInfo — anything after /products/
@WebServlet("/products/*")
public class ProductDetailServlet extends HttpServlet {

    private ProductFacadeService facade;

    @Override
    public void init() {
        facade = (ProductFacadeService) getServletContext()
                .getAttribute("productFacadeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        Integer id = parseId(req.getPathInfo(), resp);
        if (id == null) return; // response already written by parseId

        ProductDetailDTO detail = facade.getProductDetail(id);

        if (detail == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
            return;
        }

        JsonUtil.writeJson(resp, detail);
    }

    private Integer parseId(String pathInfo, HttpServletResponse resp)
            throws IOException {
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Product id required");
            return null;
        }
        // Strip leading slash — pathInfo = "/42"
        String segment = pathInfo.substring(1);

        // Hard-reject non-numeric segments so /products/search is never
        // intercepted by this servlet even if annotation scanning fires it
        // before ProductSearchServlet.
        // ProductSearchServlet is mapped to the exact path /products/search
        // so it wins in compliant containers, but this guard makes it safe
        // in all containers.
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