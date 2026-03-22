package servlet.product;

import entity.Category;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CategoryService;

import java.io.IOException;
import java.util.List;

/**
 * Serves the product catalog page (WEB-INF/catalog.jsp).
 *
 * All URL params are read client-side by catalog.js — the servlet only:
 *   1. Loads all categories for the filter sidebar.
 *   2. Resolves a human-readable page title + eyebrow from the URL params
 *      so the initial server render has the correct heading without a flash.
 *
 * Catalog URL convention:
 *   /catalog                             -> All Products
 *   /catalog?gender=FEMALE               -> Women's Collection
 *   /catalog?gender=MALE                 -> Men's Collection
 *   /catalog?gender=FEMALE&categoryIds=3 -> [Category Name]
 *   /catalog?newOnly=true                -> New Arrivals
 *   /catalog?categoryIds=1&categoryIds=2 -> personalised / multi-cat
 *
 * Filtering + pagination is handled entirely by catalog.js, which
 * calls the existing ProductCatalogServlet at GET /products.
 */
@WebServlet("/catalog")
public class CatalogServlet extends HttpServlet {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<Category> categories = categoryService.getAll();

        // All categories for the filter sidebar checkbox list
        req.setAttribute("filterCategories", categories);

        // Resolve headings from URL params for correct first-render
        String   gender  = req.getParameter("gender");
        String[] catIds  = req.getParameterValues("categoryIds");
        String   newOnly = req.getParameter("newOnly");

        req.setAttribute("pageTitle",   resolveTitle(gender, catIds, newOnly, categories));
        req.setAttribute("pageEyebrow", resolveEyebrow(gender, newOnly));

        req.getRequestDispatcher("/WEB-INF/catalog.jsp").forward(req, resp);
    }

    /* ── heading helpers ──────────────────────────────────────────────── */

    private String resolveTitle(String gender, String[] catIds,
                                String newOnly, List<Category> categories) {

        if ("true".equalsIgnoreCase(newOnly)) return "New Arrivals";

        // Single category → use its name as title
        if (catIds != null && catIds.length == 1) {
            try {
                int id = Integer.parseInt(catIds[0]);
                return categories.stream()
                        .filter(c -> c.getId().equals(id))
                        .findFirst()
                        .map(Category::getName)
                        .orElse(genderTitle(gender));
            } catch (NumberFormatException ignored) { /* fall through */ }
        }

        return genderTitle(gender);
    }

    private String genderTitle(String gender) {
        if ("FEMALE".equalsIgnoreCase(gender)) return "Women's Collection";
        if ("MALE".equalsIgnoreCase(gender))   return "Men's Collection";
        return "All Products";
    }

    private String resolveEyebrow(String gender, String newOnly) {
        if ("true".equalsIgnoreCase(newOnly))  return "Fresh in this week";
        if ("FEMALE".equalsIgnoreCase(gender)) return "Tailored for her";
        if ("MALE".equalsIgnoreCase(gender))   return "Tailored for him";
        return "Browse the collection";
    }
}