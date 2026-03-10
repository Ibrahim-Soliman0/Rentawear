package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import model.enums.Gender;
import model.enums.UserRole;
import service.UserService;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@WebServlet("/user")
public class UserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = new User();
        user.setAddress("street");
        user.setGender(Gender.MALE);
        user.setPasswordHash("hash");
        user.setJob("developer");
        user.setCreditLimit(new BigDecimal(55));
        user.setRole(UserRole.USER);
        user.setCreatedAt(Instant.now());
        user.setBirthday(LocalDate.now());
        user.setName("ibrahim");
        user.setEmail("ibrahimsoliman269@gmail.com");

        UserService userService = new UserService();
        userService.save(user);

        User returenedUser = userService.getById(1);
        User returenedUser2 = userService.getUserByEmail("ibrahimsoliman269@gmail.com");

        PrintWriter out = resp.getWriter();
        out.println(returenedUser.getName());
        out.println(returenedUser2.getName());
    }
}
