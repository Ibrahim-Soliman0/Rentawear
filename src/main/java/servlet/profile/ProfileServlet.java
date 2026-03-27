package servlet.profile;

import dto.UserSessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.CategoryService;
import util.AvatarUtil;
import util.JsonUtil;

import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        req.setAttribute("categories", categoryService.getAll());

        /* Serialize user to JSON for window.RW_USER */
        req.setAttribute("userJson", JsonUtil.toJson(user));

        /* Generate initials avatar — used as the default profile picture */
        req.setAttribute("avatarDataUri", AvatarUtil.toDataUri(user.name()));

        req.getRequestDispatcher("/WEB-INF/profile.jsp").forward(req, resp);
    }
}
