package servlet.product;

import entity.Category;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.CategoryService;

import java.io.IOException;
import java.util.List;

/**
 * Serves the product catalog HTML page at GET /catalog.
 *
 * Supports two modes — both handled by the same JSP, distinguished
 * by the presence of the q param:
 *
 *   Browse mode  /catalog?gender=FEMALE&categoryIds=3
 *   Search mode  /catalog?q=shirt&gender=FEMALE
 *
 * The servlet only sets request attributes for the initial server
 * render (title, eyebrow, category list for sidebar). catalog.js
 * then reads all URL params client-side and calls GET /products.
 */
@WebServlet("/catalog")
public class CatalogServlet extends HttpServlet {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String[] interestIds = req.getParameterValues("interestIds");
        if (interestIds != null && interestIds.length > 0) {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }
        }

        List<Category> categories = categoryService.getAll();
        req.setAttribute("filterCategories", categories);

        String   q       = req.getParameter("q");
        String   gender  = req.getParameter("gender");
        String[] catIds  = req.getParameterValues("categoryIds");
        String   newOnly = req.getParameter("newOnly");

        req.setAttribute("pageTitle",   resolveTitle(q, gender, catIds, newOnly, categories));
        req.setAttribute("pageEyebrow", resolveEyebrow(q, gender, newOnly));

        req.getRequestDispatcher("/WEB-INF/catalog.jsp").forward(req, resp);
    }

    /* ── Heading helpers ──────────────────────────────────────── */

    private String resolveTitle(String q, String gender, String[] catIds,
                                String newOnly, List<Category> categories) {
        /* Search mode */
        if (q != null && !q.isBlank()) return "Search Results";

        if ("true".equalsIgnoreCase(newOnly)) return "New Arrivals";

        if (catIds != null && catIds.length == 1) {
            try {
                int id = Integer.parseInt(catIds[0]);
                return categories.stream()
                        .filter(c -> c.getId().equals(id))
                        .findFirst()
                        .map(Category::getName)
                        .orElse(genderTitle(gender));
            } catch (NumberFormatException ignored) {}
        }

        return genderTitle(gender);
    }

    private String genderTitle(String gender) {
        if ("FEMALE".equalsIgnoreCase(gender)) return "Women's Collection";
        if ("MALE".equalsIgnoreCase(gender))   return "Men's Collection";
        return "All Products";
    }

    private String resolveEyebrow(String q, String gender, String newOnly) {
        if (q != null && !q.isBlank())
            return "Showing results for \u201c" + q + "\u201d"; // "q"
        if ("true".equalsIgnoreCase(newOnly))  return "Fresh in this week";
        if ("FEMALE".equalsIgnoreCase(gender)) return "Tailored for her";
        if ("MALE".equalsIgnoreCase(gender))   return "Tailored for him";
        return "Browse the collection";
    }
}