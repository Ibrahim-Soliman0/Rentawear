package servlet.admin;

import dto.CategoryDTO;
import dto.SaveCategoryDTO;
import entity.enums.Gender;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CategoryService;
import util.JsonUtil;

import java.io.IOException;

@WebServlet("/admin/categories")
public class AdminCategoryServlet extends HttpServlet {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        SaveCategoryDTO dto = JsonUtil.fromJson(req, SaveCategoryDTO.class);

        if (dto.name() == null || dto.name().isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Category name is required.");
            return;
        }
        if (dto.gender() == null || dto.gender().isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Gender is required.");
            return;
        }

        // Validate gender value to avoid IllegalArgumentException from Gender.valueOf(...) downstream
        try {
            Gender.valueOf(dto.gender().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid gender value.");
            return;
        }
        CategoryDTO saved = categoryService.saveCategory(dto);
        JsonUtil.writeJson(resp, saved);
    }
}