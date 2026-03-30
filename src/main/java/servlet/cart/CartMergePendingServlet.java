package servlet.cart;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * GET /cart/merge-pending
 * <p>
 * Lets cart.js check — on every page load after a redirect — whether a
 * guest-cart merge is waiting.  LoginServlet sets the session flag
 * "pendingCartMerge" = true.  This endpoint reads it, clears it, and
 * returns a JSON boolean so cart.js knows whether to POST to /cart/merge.
 * <p>
 * The flag is consumed on first read so a browser refresh does not
 * re-trigger the merge.
 * <p>
 * Response
 * ─────────
 * { "pending": true  }   — merge needed, flag cleared
 * { "pending": false }   — nothing to do
 */
@WebServlet("/cart/merge-pending")
public class CartMergePendingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        if (session == null) {
            resp.getWriter().write("{\"pending\": false}");
            return;
        }

        boolean pending = Boolean.TRUE.equals(session.getAttribute("pendingCartMerge"));

        if (pending) {
            // Consume the flag — one merge per login, even if the user refreshes
            session.removeAttribute("pendingCartMerge");
        }

        resp.getWriter().write("{\"pending\": " + pending + "}");
    }
}