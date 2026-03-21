package servlet.cart;

import dto.UserSessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.CartService;

import java.io.IOException;

@WebServlet("/cart/update-qty")
public class CartUpdateQtyServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();

        if (session == null) {
            resp.getWriter().write("{\"success\": false, \"message\": \"Not logged in.\"}");
            return;
        }

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            resp.getWriter().write("{\"success\": false, \"message\": \"Not logged in.\"}");
            return;
        }

        try {
            Integer cartItemId = Integer.parseInt(req.getParameter("cartItemId"));
            Integer newQty = Integer.parseInt(req.getParameter("qty"));

            cartService.updateItemQty(user.id(), cartItemId, newQty);

            resp.getWriter().write("{\"success\": true, \"message\": \"Qty updated.\"}");

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\": false, \"message\": \"Invalid cartItemId.\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
