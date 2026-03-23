package filter;

import entity.Category;
import entity.enums.Gender;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import service.CategoryService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NavigationFilter implements Filter {

    private final CategoryService categoryService = new CategoryService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        if (isPageRequest(req)) {
            try {
                List<Category> all = categoryService.getAll();

                req.setAttribute("navCategoriesFemale",
                        all.stream()
                                .filter(c -> c.getGender() == Gender.FEMALE)
                                .collect(Collectors.toList()));

                req.setAttribute("navCategoriesMale",
                        all.stream()
                                .filter(c -> c.getGender() == Gender.MALE)
                                .collect(Collectors.toList()));

            } catch (Exception ignored) {
                req.setAttribute("navCategoriesFemale", Collections.emptyList());
                req.setAttribute("navCategoriesMale",   Collections.emptyList());
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPageRequest(HttpServletRequest req) {
        // Skip AJAX calls from our own JS (fetch/XHR)
        if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) return false;

        // Skip pure JSON requests (API endpoints called by fetch())
        String accept = req.getHeader("Accept");
        if (accept != null
                && accept.contains("application/json")
                && !accept.contains("text/html")) return false;

        // Skip static assets
        String path = req.getRequestURI().substring(req.getContextPath().length());
        return !path.startsWith("/assets/");
    }
}