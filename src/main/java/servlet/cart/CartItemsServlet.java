package servlet.cart;

import dto.CartItemDTO;
import dto.UserSessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.CartService;
import util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/cart/items")
public class CartItemsServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        // ── Guest user — return an empty items list so cart.js can fall back
        // to its localStorage cache without treating this as an error.
        if (session == null || session.getAttribute("user") == null) {
            resp.getWriter().write("{\"loggedIn\": false, \"items\": []}");
            return;
        }

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        List<CartItemDTO> itemsInCart = cartService.getItems(user.id());

        String json = JsonUtil.toJson(Map.of("loggedIn", true, "items", itemsInCart));
        resp.getWriter().write(json);
    }
}
