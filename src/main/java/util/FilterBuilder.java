package util;

import dto.ProductFilterDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// Shared utility for building ProductFilterDTO from raw servlet request params.
// Lives in servlet.util so all product servlets parse parameters identically.
// Returns a valid ProductFilterDTO regardless of which params are missing —
// null means "no filter on this dimension" downstream.
public final class FilterBuilder {

    private FilterBuilder() {}

    public static ProductFilterDTO fromRequest(HttpServletRequest req) {
        return new ProductFilterDTO(
                parseString(req, "q"),
                parseString(req, "gender"),
                parseIntList(req, "categoryIds"),
                parseDouble(req, "minPrice"),
                parseDouble(req, "maxPrice"),
                parseBoolean(req, "newOnly"),
                parseIntList(req, "interestIds"),
                parseInt(req, "page",     0),
                parseInt(req, "pageSize", 20)
        );
    }

    private static String parseString(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v != null && !v.isBlank()) ? v.trim() : null;
    }

    private static Double parseDouble(HttpServletRequest req, String name) {
        try {
            String v = req.getParameter(name);
            return (v != null && !v.isBlank()) ? Double.parseDouble(v) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parseInt(HttpServletRequest req, String name, int fallback) {
        try {
            String v = req.getParameter(name);
            int parsed = (v != null && !v.isBlank()) ? Integer.parseInt(v) : fallback;
            return Math.max(parsed, 0); // never negative
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static Boolean parseBoolean(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v != null) ? Boolean.parseBoolean(v) : null;
    }

    private static List<Integer> parseIntList(HttpServletRequest req, String name) {
        String[] values = req.getParameterValues(name);
        if (values == null || values.length == 0) return null;
        try {
            List<Integer> result = Arrays.stream(values)
                    .filter(v -> v != null && !v.isBlank())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            return result.isEmpty() ? null : result;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
