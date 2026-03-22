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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// this servlet is used to return the user object inside the session in json format
@WebServlet("/user/session")
public class UserSessionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"loggedIn\": false}");
            return;
        }

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        // Build only what checkout.js needs — no sensitive fields
        // paymentCards: id, cardType, cardNumber (last 4 only), expiryMonth, expiryYear

        List<Map<String, Object>> cards = user.paymentCards() == null
                ? List.of()
                : user.paymentCards().stream()
                .map(card -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("id", card.id());
                            m.put("cardType", card.cardType().toString());
                            // Only send last 4 digits — never expose the full card number
                            String num = card.cardNumber() != null ? card.cardNumber() : "";
                            m.put("lastFour", num.length() >= 4 ?
                                    num.substring(num.length() - 4) : num);
                            m.put("expiryMonth", card.expiryMonth());
                            m.put("expiryYear", card.expiryYear());
                            return m;
                        }
                ).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("loggedIn", true);
        response.put("name", user.name());
        response.put("address", user.address());
        response.put("paymentCards", cards);

        resp.getWriter().write(JsonUtil.toJson(response));
    }
}