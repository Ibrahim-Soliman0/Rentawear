package util;

import java.util.Base64;

public final class AvatarUtil {

    /* Palette — one colour picked by hashing the name so the
       same person always gets the same colour */
    private static final String[] BACKGROUNDS = {
            "#2bbf87", "#3b82f6", "#8b5cf6", "#ec4899",
            "#f59e0b", "#ef4444", "#06b6d4", "#84cc16"
    };

    private AvatarUtil() {
    }

    /**
     * Returns a Base64-encoded SVG data URI that can be used directly
     * as an <img src="..."> value.
     * <p>
     * Example:  data:image/svg+xml;base64,PHN2ZyB4bWxu...
     */
    public static String toDataUri(String name) {
        String initials = extractInitials(name);
        String background = pickBackground(name);
        String svg = buildSvg(initials, background);
        String encoded = Base64.getEncoder().encodeToString(svg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + encoded;
    }

    /* ── Helpers ── */

    private static String extractInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        /* First + last word initials */
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                .toUpperCase();
    }

    private static String pickBackground(String name) {
        if (name == null || name.isBlank()) return BACKGROUNDS[0];
        int hash = Math.abs(name.hashCode());
        return BACKGROUNDS[hash % BACKGROUNDS.length];
    }

    private static String buildSvg(String initials, String background) {
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"128\" height=\"128\" viewBox=\"0 0 128 128\">"
                + "<rect width=\"128\" height=\"128\" rx=\"64\" fill=\"" + background + "\"/>"
                + "<text x=\"64\" y=\"64\" dominant-baseline=\"central\" text-anchor=\"middle\" "
                + "font-family=\"-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif\" "
                + "font-size=\"48\" font-weight=\"600\" fill=\"#ffffff\">"
                + initials
                + "</text>"
                + "</svg>";
    }
}