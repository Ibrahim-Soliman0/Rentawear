package servlet.product;

import dto.ProductFilterDTO;
import dto.ProductSearchResult;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ProductFacadeService;
import util.FilterBuilder;
import util.JsonUtil;

import java.io.IOException;
import java.util.List;

// GET /products/search
//
// Powers the search modal. Returns ProductSearchResult — lighter than
// ProductListResult (no swatches, no price range).
//
// Requires at least ?q= parameter. Optional filters:
//   ?q=shirt&gender=MALE&categoryIds=3&minPrice=10&maxPrice=50
//
// Returns 400 if q is blank so the caller always gets a meaningful error
// rather than a full unfiltered product list.
//
// Note on servlet ordering: /products/search must be declared before
// /products/* in web.xml (or alphabetically earlier with annotation scanning)
// so this servlet wins over ProductDetailServlet for /products/search.
@WebServlet("/products/search")
public class ProductSearchServlet extends HttpServlet {

    private ProductFacadeService facade;

    @Override
    public void init() {
        facade = (ProductFacadeService) getServletContext()
                .getAttribute("productFacadeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        ProductFilterDTO filter = FilterBuilder.fromRequest(req);

        if (!filter.isSearch()) {
            JsonUtil.writeJson(resp, new ProductSearchResult(List.of(), 0, null, 0, filter.pageSize()));
            return;
        }
        ProductSearchResult result = facade.searchProducts(filter);
        JsonUtil.writeJson(resp, result);
    }
}
