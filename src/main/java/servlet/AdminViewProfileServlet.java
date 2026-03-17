package servlet;

import dto.UserSessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CategoryService;
import service.UserService;
import util.AvatarUtil;
import util.JsonUtil;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/admin/profile")
public class AdminViewProfileServlet extends HttpServlet {

    private final UserService     userService     = new UserService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            return;
        }

        // Load user by id
        Optional<UserSessionDTO> userOpt = userService.getSessionDTOById(Integer.parseInt(idParam));
        if (userOpt.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            return;
        }

        UserSessionDTO user = userOpt.get();

        req.setAttribute("categories",  categoryService.getAll());
        req.setAttribute("userJson",     JsonUtil.toJson(user));
        req.setAttribute("avatarDataUri", AvatarUtil.toDataUri(user.name()));
        req.setAttribute("readOnly",     true);
        req.setAttribute("viewUserId", user.id());

        req.getRequestDispatcher("/WEB-INF/profile.jsp").forward(req, resp);
    }
}