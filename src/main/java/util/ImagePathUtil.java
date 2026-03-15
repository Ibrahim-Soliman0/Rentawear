package util;

public final class ImagePathUtil {

    public static final String WEB_ROOT          = "/assets/img";
    public static final String PLACEHOLDER_BASE  = WEB_ROOT + "/placeholder";

    private ImagePathUtil() {}

    /**
     * Base path stored in ProductImage.imageUrl and Product.imageUrl.
     * uuid: 8-char hex string, generated before upload.
     */
    public static String base(int productId, String encodedColor, String uuid) {
        return WEB_ROOT + "/products/"
                + productId + "/"
                + ColorUtil.slug(encodedColor) + "/"
                + uuid;
    }

    /**
     * Full URL for rendering. size = "sm", "md", or "lg".
     * e.g. url("/assets/img/products/42/midnight-navy/a3f7c2d1", "md")
     *   -> "/assets/img/products/42/midnight-navy/a3f7c2d1_md.jpg"
     */
    public static String url(String base, String size) {
        if (base == null || base.isEmpty()) return PLACEHOLDER_BASE + "_" + size + ".jpg";
        return base + "_" + size + ".jpg";
    }

    /**
     * Absolute filesystem directory for writing files.
     * webappRoot: from getServletContext().getRealPath("/")
     */
    public static String absoluteDir(String webappRoot, int productId, String encodedColor) {
        return webappRoot + "assets/img/products/"
                + productId + "/"
                + ColorUtil.slug(encodedColor) + "/";
    }
}