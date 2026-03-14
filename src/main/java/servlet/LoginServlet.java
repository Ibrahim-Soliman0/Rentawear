package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.UserService;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if(session !=null && session.getAttribute("user")!=null ) {
            resp.sendRedirect("index.jsp");
            return;
        }
        req.getRequestDispatcher("/login.jsp").forward(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        boolean remember = "on".equals(req.getParameter("rememberMe"));

        if (isEmpty(email) || isEmpty(password)) {
            forwardWithError(req, resp, "Please enter your email and password.");
        }

        Optional<User> result = userService.login(email.trim().toLowerCase(), password);

        if (result.isEmpty()) {
            req.setAttribute("prefillEmail", email);
            forwardWithError(req, resp, "Invalid email or password.");
            return;
        }

        User user = result.get();
        HttpSession session = req.getSession(true);
        session.setAttribute("user",user);

        if(remember){
            session.setMaxInactiveInterval(60*60*24*30);
        }
        else{
            session.setMaxInactiveInterval(60 * 60 * 24);
        }

        resp.sendRedirect(req.getContextPath() + "/index.jsp");
    }

    private boolean isEmpty(String val) {
        return val == null || val.trim().isEmpty();
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("errorMsg", message);
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }
}
