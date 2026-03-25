package servlet.admin;

import dto.CategoryDTO;
import dto.SaveCategoryDTO;
import entity.Category;
import entity.enums.Gender;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CategoryService;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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

        CategoryDTO saved = categoryService.saveCategory(dto);
        JsonUtil.writeJson(resp, saved);
    }
}