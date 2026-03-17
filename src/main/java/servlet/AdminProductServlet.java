package servlet;

import dto.AdminProductDetailDTO;
import dto.AdminProductListResult;
import dto.ProductFilterDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ProductFacadeService;
import servlet.util.FilterBuilder;
import util.JsonUtil;

import java.io.IOException;

// GET /admin/products           paginated product list (AdminProductRowDTO)
// GET /admin/products/{id}      full product detail (AdminProductDetailDTO)

@WebServlet("/admin/products/*")
public class AdminProductServlet extends HttpServlet {

    private ProductFacadeService facade;

    @Override
    public void init() {
        facade = (ProductFacadeService) getServletContext()
                .getAttribute("productFacadeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /admin/products -> listing
            handleList(req, resp);
        } else {
            // GET /admin/products/{id} -> detail
            handleDetail(pathInfo, resp);
        }
    }

    //Handlers

    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        ProductFilterDTO  filter = FilterBuilder.fromRequest(req);
        AdminProductListResult result = facade.getAdminProducts(filter);
        JsonUtil.writeJson(resp, result);
    }

    private void handleDetail(String pathInfo, HttpServletResponse resp)
            throws IOException {
        Integer id = parseId(pathInfo, resp);
        if (id == null) return;

        AdminProductDetailDTO detail = facade.getAdminDetail(id);
        if (detail == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
            return;
        }
        JsonUtil.writeJson(resp, detail);
    }


    private Integer parseId(String pathInfo, HttpServletResponse resp)
            throws IOException {
        try {
            // pathInfo = "/42" or "/42/delete" — take only the first segment
            String segment = pathInfo.split("/")[1];
            return Integer.parseInt(segment);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product id");
            return null;
        }
    }
}
