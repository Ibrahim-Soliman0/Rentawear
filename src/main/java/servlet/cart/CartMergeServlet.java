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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.CartService;
import util.JsonUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * POST /cart/merge
 * <p>
 * Called by cart.js on the first page load after login.
 * Receives the guest cart (items stored in localStorage while the user was
 * not logged in) and merges them into the user's DB cart.
 * <p>
 * Merge rule
 * ──────────
 * For each guest item, look up the matching DB line-item by variant key
 * (variantId : size : color : startDate : endDate):
 * <p>
 * Case 1 — key NOT in DB cart:
 * Check total qty already reserved for this variantId across ALL the user's
 * cart line-items (all date ranges).  If there is room under inventoryQty,
 * add the guest item (capped to the remaining room).  Otherwise discard.
 * <p>
 * Case 2 — key IS in DB cart (same variant + same dates):
 * The line-item already exists.  Try to top it up by the guest qty.
 * Calculate remaining room = inventoryQty − totalReservedForVariant.
 * If room > 0, call updateItemQty() to increment by min(guestQty, room).
 * If no room, discard.
 * <p>
 * This means a guest who had 1 unit of a variant with 3 in stock, whose DB
 * cart already has 2 of that variant on the same dates, will correctly have
 * their qty bumped to 3 rather than being silently discarded.
 * <p>
 * Request body (application/json)
 * ────────────────────────────────
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
 * { "success": true, "merged": 2, "skipped": 1 }
 */
@WebServlet("/cart/merge")
public class CartMergeServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartMergeServlet.class);
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

        // ── Load the user's current DB cart ───────────────────────────────────
        List<CartItemDTO> existingItems = cartService.getItems(user.id());

        // Map of variantKey → CartItemDTO for fast lookup of matching line-items
        Map<String, CartItemDTO> existingByKey = existingItems.stream()
                .collect(Collectors.toMap(
                        item -> variantKey(item.variantId(), item.size(), item.color(),
                                item.startDate(), item.endDate()),
                        item -> item,
                        // If duplicate keys exist in DB (shouldn't happen), keep first
                        (a, b) -> a
                ));

        // Running total of qty per variantId across all line-items in the DB cart.
        // We mutate this as we merge so that multiple guest items for the same
        // variantId correctly consume from the same shared pool.
        Map<Integer, Integer> reservedQtyByVariant = new HashMap<>();
        for (CartItemDTO item : existingItems) {
            reservedQtyByVariant.merge(item.variantId(), item.qty(), Integer::sum);
        }

        // ── Merge ─────────────────────────────────────────────────────────────
        int merged = 0;
        int skipped = 0;

        for (GuestItem guest : guestItems) {

            if (guest.id == null || guest.startDate == null || guest.endDate == null) {
                skipped++;
                continue;
            }

            int guestQty = guest.qty != null ? guest.qty : 1;
            String key = variantKey(guest.id, guest.size, guest.color,
                    guest.startDate, guest.endDate);

            // How many of this variantId does the user already have in total?
            int alreadyReserved = reservedQtyByVariant.getOrDefault(guest.id, 0);

            // How many units of this variant does this product have in stock?
            // We read it from the existing DB cart items if available, since
            // CartItemDTO carries inventoryQty.  If this guest item's variant
            // isn't in the DB cart at all we fall back to fetching it via
            // CartService.getReservedQty() — but inventoryQty isn't available
            // there.  The cleanest approach: read it from any existing DB item
            // that shares the same variantId, or from the matched line-item.
            Integer inventoryQty = existingItems.stream()
                    .filter(i -> i.variantId() == guest.id)
                    .map(CartItemDTO::inventoryQty)
                    .findFirst()
                    .orElse(null);

            // How much room is left under the inventory cap?
            // If we don't know inventoryQty (variant not in DB cart at all),
            // we allow the add and let CartService.addItem() enforce the limit.
            int room = (inventoryQty != null)
                    ? Math.max(0, inventoryQty - alreadyReserved)
                    : guestQty; // unknown cap — attempt the add

            if (room == 0) {
                // No room at all for this variant
                skipped++;
                continue;
            }

            // How many can we actually add?
            int qtyToAdd = Math.min(guestQty, room);

            CartItemDTO matchingDbItem = existingByKey.get(key);

            try {
                if (matchingDbItem != null) {
                    // ── Case 2: same variant + same dates already in DB cart ──
                    // Top up the existing line-item by qtyToAdd.
                    int newQty = matchingDbItem.qty() + qtyToAdd;
                    cartService.updateItemQty(user.id(), matchingDbItem.cartItemId(), newQty);
                } else {
                    // ── Case 1: new line-item (different dates or not in cart) ─
                    cartService.addItem(user.id(), new SimpleCartItemDTO(
                            guest.id,
                            qtyToAdd,
                            guest.startDate,
                            guest.endDate
                    ));
                }

                // Update our running total so subsequent guest items for the
                // same variantId consume from the correctly reduced pool.
                reservedQtyByVariant.merge(guest.id, qtyToAdd, Integer::sum);
                merged++;

            } catch (Exception e) {
                LOGGER.error("[CartMergeServlet] Failed to merge guest item (variantId={}): {}",
                        guest.id, e.getMessage());
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
    public static class GuestItem {
        public Integer id;
        public Integer qty;
        public String startDate;
        public String endDate;
        public String size;
        public String color;
    }
}