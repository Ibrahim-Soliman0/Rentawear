package servlet;

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
        try {
            // pathInfo = "/42" — strip the leading slash
            return Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product id");
            return null;
        }
    }
}
