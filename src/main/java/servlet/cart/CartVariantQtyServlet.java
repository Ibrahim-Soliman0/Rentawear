package servlet.cart;

import dto.UserSessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.CartItemService;
import util.JsonUtil;

import java.io.IOException;
import java.util.Map;

/**
 * GET /cart/variant-qty?variantId=X
 * <p>
 * Returns the total quantity of a specific variant already sitting in the
 * logged-in user's DB cart, summed across ALL line-items (all date ranges).
 * <p>
 * cart.js calls this in add() before the local inventory check so that guest
 * users — who have an empty localStorage after logout but may still have items
 * reserved in their DB cart — cannot exceed their per-user inventory limit.
 * <p>
 * Response (application/json)
 * ────────────────────────────
 * Logged-in:  { "loggedIn": true,  "reservedQty": 2 }
 * Guest:      { "loggedIn": false, "reservedQty": 0 }
 */
@WebServlet("/cart/variant-qty")
public class CartVariantQtyServlet extends HttpServlet {

    private final CartItemService cartItemService = new CartItemService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        // Guest — no DB cart to check against
        if (session == null || session.getAttribute("user") == null) {
            JsonUtil.writeJson(resp, Map.of("loggedIn", false, "reservedQty", 0));
            return;
        }

        try {
            int variantId = Integer.parseInt(req.getParameter("variantId"));

            UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
            int reservedQty = cartItemService.getReservedQty(user.id(), variantId);

            JsonUtil.writeJson(resp, Map.of("loggedIn", true, "reservedQty", reservedQty));

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, Map.of("success", false, "message", "Invalid variantId."));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, Map.of("success", false, "message", e.getMessage()));
        }
    }
}