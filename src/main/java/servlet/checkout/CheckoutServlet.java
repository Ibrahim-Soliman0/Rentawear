package servlet.checkout;

import dto.UserSessionDTO;
import exception.InsufficientFundsException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.OrderService;
import util.JsonUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            resp.getWriter().write("{\"success\": false, " +
                    "\"message\": \"Session expired. Please log in again.\"}");
            return;
        }

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        // Only handle placeOrder — ignore anything else
        String action = req.getParameter("action");
        if (!"placeOrder".equals(action)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\": false, \"message\": \"Unknown action.\"}");
            return;
        }

        try {
            // ── Read parameters ───────────────────────────────────────────
            String cartJson = req.getParameter("cartJson");
            Double totalAmount = Double.parseDouble(req.getParameter("totalAmount"));

            // ── Validate ──────────────────────────────────────────────────
            if (cartJson == null || cartJson.isBlank()) {
                resp.getWriter().write("{\"success\": false, \"message\": \"Cart is empty.\"}");
                return;
            }

            if (user.address() == null || user.address().isBlank()) {
                resp.getWriter().write("{\"success\": false, " +
                        "\"message\": \"No delivery address on your account.\"}");
                return;
            }

            // ── Place the order ───────────────────────────────────────────
            // OrderService.placeOrder() should:
            //   1. Parse cartJson into a list of cart item data
            //   2. Create an Order record in the DB linked to user.id()
            //   3. Create OrderItem records for each cart item
            //   4. Link the selected payment card
            //   5. Clear the cart_items rows from the DB for this user
            //   6. Return the new order ID

            Integer orderId = orderService.placeOrder(
                    user.id(),
                    cartJson,
                    totalAmount
            );

            // ── Success — tell JS where to redirect ───────────────────────
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("orderId", orderId);
            response.put("redirect", req.getContextPath() + "/profile");

            resp.getWriter().write(JsonUtil.toJson(response));

        } catch (InsufficientFundsException e) {
            // user exceed his credit limit
            resp.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
            resp.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\": false," +
                    " \"message\": \"Invalid payment method.\"}");

        } catch (IllegalStateException e) {
            // Business rule violations from OrderService
            // e.g. "Item no longer available", "Variant out of stock"
            resp.getWriter().write(
                    "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}"
            );

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(
                    "{\"success\": false, " +
                            "\"message\": \"Could not place your order. Please try again.\"}"
            );

            // Log the real error server-side — never expose it to the client
            System.err.println("[CheckoutServlet] order failed for user " +
                    user.id() + ": " + e.getMessage());
        }
    }
}
