package servlet.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import service.ProductFacadeService;
import util.ImagePathUtil;
import util.ImageProcessor;
import util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/admin/product-image")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class AdminProductImageServlet extends HttpServlet {

    private static final String PRIMARY_COLOR = "#000000-Primary";

    private ProductFacadeService facade;

    @Override
    public void init() {
        facade = (ProductFacadeService) getServletContext()
                .getAttribute("productFacadeService");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String productIdRaw = req.getParameter("productId");
        if (productIdRaw == null || productIdRaw.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing productId");
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

        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String basePath = ImagePathUtil.base(productId, PRIMARY_COLOR, uuid);
        String webappRoot = getServletContext().getRealPath("/");
        String absDir = ImagePathUtil.absoluteDir(webappRoot, productId, PRIMARY_COLOR);

        ImageProcessor.process(imagePart.getInputStream(), new File(absDir), uuid);

        String oldBase = facade.updateProductImage(productId, basePath);
        if (oldBase == null) {
            ImageProcessor.deleteAll(basePath, webappRoot);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
            return;
        }

        if (shouldDeleteOld(oldBase)) {
            ImageProcessor.deleteAll(oldBase, webappRoot);
        }

        JsonUtil.writeJson(resp, new ImageUploadResponse(basePath));
    }

    private boolean shouldDeleteOld(String oldBase) {
        if (oldBase == null || oldBase.isBlank()) return false;
        if (!oldBase.startsWith(ImagePathUtil.WEB_ROOT)) return false;
        if (oldBase.contains("placeholder")) return false;
        return !(oldBase.endsWith(".jpg") || oldBase.endsWith(".png"));
    }

    record ImageUploadResponse(String imageUrl) {}
}
