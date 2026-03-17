package servlet;

import dto.ProductFilterDTO;
import dto.ProductListResult;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ProductFacadeService;
import servlet.util.FilterBuilder;
import util.JsonUtil;

import java.io.IOException;

// GET /products
//
// Handles all catalog listing scenarios via ProductFilterDTO routing:
//   ?newOnly=true                      → new arrivals
//   ?interestIds=1,2,3                 → personalized feed
//   ?gender=MALE&categoryIds=4&...     → filtered browse
//   (no params)                        → all products
//
// All paths return ProductListResult which includes:
//   - current page of ProductCardDTO
//   - total count for pagination controls
//   - PriceRangeDTO for the price slider
//   - page + pageSize echoed back
//
// This servlet is read-only and requires no authentication.
@WebServlet("/products")
public class ProductCatalogServlet extends HttpServlet {

    private ProductFacadeService facade;

    @Override
    public void init() {
        // Wire dependencies — replace with your DI approach if applicable
        facade = (ProductFacadeService) getServletContext()
                .getAttribute("productFacadeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        ProductFilterDTO  filter = FilterBuilder.fromRequest(req);
        ProductListResult result = facade.getProducts(filter);
        JsonUtil.writeJson(resp, result);
    }
}
