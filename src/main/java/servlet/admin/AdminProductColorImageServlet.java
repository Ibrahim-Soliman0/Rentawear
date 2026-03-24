package servlet.admin;

import entity.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import service.ProductFacadeService;
import service.ProductImageService;
import util.JsonUtil;

import java.io.IOException;

@WebServlet("/admin/product-color-image")
@MultipartConfig(
        maxFileSize    = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class AdminProductColorImageServlet extends HttpServlet {

    private ProductFacadeService  facade;

    @Override
    public void init() {
        facade       = (ProductFacadeService)  getServletContext().getAttribute("productFacadeService");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String productIdRaw = req.getParameter("productId");
        String color        = req.getParameter("color"); // e.g. "#1B2A4A-Midnight Navy"

        if (productIdRaw == null || color == null || color.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing productId or color");
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdRaw);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid productId");
            return;
        }

        Part imagePart = req.getPart("image");
        if (imagePart == null || imagePart.getSize() == 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing image file");
            return;
        }

        String contentType = imagePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported image type");
            return;
        }

        Product product = facade.getProductById(productId);
        if (product == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
            return;
        }

        String webappRoot = getServletContext().getRealPath("/");
        String basePath   = facade.saveColorImage(
                product, color, imagePart.getInputStream(), webappRoot);

        JsonUtil.writeJson(resp, new ColorImageResponse(color, basePath));
    }

    record ColorImageResponse(String color, String imageUrl) {}
}