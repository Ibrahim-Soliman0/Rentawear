package servlet.profile;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        // ── Tell cart.js to wipe localStorage on the next page load ──────────
        // We can't set a session attribute after invalidate(), so we use a
        // plain cookie instead.
        // cart.js reads it on boot, clears localStorage,
        // then deletes the cookie.  The cookie is short-lived and carries no
        // sensitive data — it is just a one-shot signal.
        Cookie clearCart = new Cookie("rw_cart_clear", "1");
        clearCart.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        clearCart.setMaxAge(60);          // expires in 60 s — consumed immediately by JS
        clearCart.setHttpOnly(false);     // must be readable by cart.js
        resp.addCookie(clearCart);

        resp.sendRedirect(req.getContextPath() + "/home");
    }
}
