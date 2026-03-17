package servlet.profile;

import dto.UserSessionDTO;
import entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.UserService;
import util.JsonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet("/profile/credit/increase")
public class CreditLimitServlet extends HttpServlet {

    private static final BigDecimal INCREMENT = new BigDecimal("500.00");

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /* ── Auth guard ── */
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, new Response(false, "Not logged in.", null));
            return;
        }

        UserSessionDTO sessionUser = (UserSessionDTO) session.getAttribute("user");

        /* ── Load user, add £500, save ── */
        Optional<User> userOpt = userService.getById(sessionUser.id());
        if (userOpt.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, new Response(false, "User not found.", null));
            return;
        }

        User user = userOpt.get();
        BigDecimal current = user.getCreditLimit() != null ? user.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal newLimit = current.add(INCREMENT);
        user.setCreditLimit(newLimit);
        userService.save(user);

        /* ── Refresh session ── */
        UserSessionDTO updated = new UserSessionDTO(
                sessionUser.id(),
                sessionUser.name(),
                sessionUser.email(),
                sessionUser.gender(),
                sessionUser.birthday(),
                sessionUser.job(),
                sessionUser.address(),
                newLimit,
                sessionUser.role(),
                sessionUser.interests(),
                sessionUser.paymentCards()
        );
        session.setAttribute("user", updated);

        JsonUtil.writeJson(resp,
                new Response(true, "Credit limit increased.", newLimit));
    }

    private record Response(boolean success, String message, BigDecimal newLimit) {
    }
}
