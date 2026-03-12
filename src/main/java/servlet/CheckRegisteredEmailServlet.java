package servlet;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.UserService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/checkRegisteredEmail")
public class CheckRegisteredEmailServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        boolean taken = false;
        if (email != null && !email.trim().isEmpty()) {
            taken = userService.getUserByEmail(email.trim().toLowerCase()).isPresent();
        }

        JsonObjectBuilder objBuilder = Json.createObjectBuilder();
        objBuilder.add("taken", taken);
        JsonObject json = objBuilder.build();

        PrintWriter out = resp.getWriter();
        out.println(json.toString());
    }
}
