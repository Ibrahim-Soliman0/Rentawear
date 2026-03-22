package servlet;

import dto.UserSessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.JsonUtil;

import java.io.IOException;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);   // never create a new session here

        if (session != null && session.getAttribute("user") != null) {
            UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

            // Serialize to JSON so index.jsp can expose it as window.RW_USER —
            // exactly the same pattern ProfileServlet uses for profile.jsp.
            request.setAttribute("userJson", JsonUtil.toJson(user));
        }
        // If guest: userJson stays null → index.jsp emits window.RW_USER = null
        // and home.js loadInterests() exits early without errors.

        request.getRequestDispatcher("WEB-INF/index.jsp").forward(request, response);
    }
}