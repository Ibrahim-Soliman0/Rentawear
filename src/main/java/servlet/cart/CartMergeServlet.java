package servlet.cart;

import dto.CartItemDTO;
import dto.SimpleCartItemDTO;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * POST /cart/merge
 * <p>
 * Called by cart.js on the first page load after login.
 * Receives the guest cart (items stored in localStorage while the user was
 * not logged in) and merges them into the user's DB cart.
 * <p>
 * Merge rule <p>
 * ──────────
 * <p>
 * A "variant key" uniquely identifies a line-item:
 * variantId : size : color : startDate : endDate
 * <p>
 * • Key already exists in the DB cart  → discard the guest item (DB wins)
 * <p>
 * • Key not in the DB cart             → add the guest item via CartService
 * <p>
 * Request body (application/json)
 * ────────────────────────────────
 * <p>
 * [
 * {
 * "id"        : 42,
 * "qty"       : 1,
 * "startDate" : "2025-06-01",
 * "endDate"   : "2025-06-05",
 * "size"      : "M",
 * "color"     : "#2D5A3D-Forest Green"
 * },
 * ...
 * ]
 * <p>
 * Response (application/json)
 * ────────────────────────────
 * <p>
 * { "success": true, "merged": 2, "skipped": 1 }
 */
@WebServlet("/cart/merge")
public class CartMergeServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ── Auth check ────────────────────────────────────────────────────────
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, Map.of("success", false, "message", "Not logged in."));
            return;
        }

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        // ── Parse guest items from request body ───────────────────────────────
        // Gson deserialises the JSON array into GuestItem[] using the public fields.
        // fromJson returns null if the body is empty or the JSON is "null".
        GuestItem[] guestArray;
        try {
            guestArray = JsonUtil.fromJson(req, GuestItem[].class);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, Map.of("success", false, "message", "Invalid JSON body."));
            return;
        }

        if (guestArray == null || guestArray.length == 0) {
            JsonUtil.writeJson(resp, Map.of("success", true, "merged", 0, "skipped", 0));
            return;
        }

        List<GuestItem> guestItems = Arrays.asList(guestArray);

        // ── Build a set of existing variant keys from the DB cart ─────────────
        // Key format mirrors variantKey() in cart.js:
        //   `${item.id}:${item.size||''}:${item.color||''}:${item.startDate||''}:${item.endDate||''}`
        List<CartItemDTO> existingItems = cartService.getItems(user.id());
        Set<String> existingKeys = existingItems.stream()
                .map(item -> variantKey(
                        item.variantId(),
                        item.size(),
                        item.color(),
                        item.startDate(),
                        item.endDate()
                ))
                .collect(Collectors.toSet());

        // ── Merge ─────────────────────────────────────────────────────────────
        int merged = 0;
        int skipped = 0;

        for (GuestItem guest : guestItems) {

            // Skip malformed guest items silently
            if (guest.id == null || guest.startDate == null || guest.endDate == null) {
                skipped++;
                continue;
            }

            String key = variantKey(guest.id, guest.size, guest.color, guest.startDate, guest.endDate);

            if (existingKeys.contains(key)) {
                // DB cart already has this variant+dates combo → discard guest item
                skipped++;
                continue;
            }

            try {
                cartService.addItem(user.id(), new SimpleCartItemDTO(
                        guest.id,
                        guest.qty != null ? guest.qty : 1,
                        guest.startDate,
                        guest.endDate
                ));
                existingKeys.add(key); // prevent duplicates within the guest list itself
                merged++;
            } catch (Exception e) {
                System.err.println("[CartMergeServlet] Failed to add guest item (variantId="
                        + guest.id + "): " + e.getMessage());
                skipped++;
            }
        }

        JsonUtil.writeJson(resp, Map.of("success", true, "merged", merged, "skipped", skipped));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String variantKey(Integer variantId, String size, String color,
                              String startDate, String endDate) {
        return variantId
                + ":" + nullToEmpty(size)
                + ":" + nullToEmpty(color)
                + ":" + nullToEmpty(startDate)
                + ":" + nullToEmpty(endDate);
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    // ── Inner DTO for the JSON request body ───────────────────────────────────
    // Public fields let Gson deserialise without extra configuration.
    // Field names match the normalised item shape in cart.js.
    public static class GuestItem {
        public Integer id;        // variantId (cart.js normalised name)
        public Integer qty;
        public String startDate;
        public String endDate;
        public String size;
        public String color;
    }
}