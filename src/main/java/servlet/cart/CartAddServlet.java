package servlet.cart;

import dto.SimpleCartItemDTO;
import dto.UserSessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.CartService;

import java.io.IOException;

@WebServlet("/cart/add")
public class CartAddServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            resp.getWriter().write("{\"success\": false, \"message\": \"Not logged in.\"}");
            return;
        }

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        try {
            int variantId = Integer.parseInt(req.getParameter("variantId"));
            int qty = Integer.parseInt(req.getParameter("qty"));
            String startDate = req.getParameter("startDate");
            String endDate = req.getParameter("endDate");

            if (startDate == null || startDate.isBlank() || endDate == null || endDate.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"success\": false, \"message\": \"Dates are required.\"}");
                return;
            }

            SimpleCartItemDTO itemToAdd = new SimpleCartItemDTO(variantId, qty, startDate, endDate);

            Integer cartItemId = cartService.addItem(user.id(), itemToAdd);

            // Return the new cartItemId so cart.js can store it immediately
            // and use it for remove / update-qty calls without needing a re-sync
            resp.getWriter().write(
                    "{\"success\": true, \"cartItemId\": " + cartItemId + "}"
            );

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\": false, \"message\": \"Invalid variantId or qty.\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
