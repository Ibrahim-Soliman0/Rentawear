package servlet.profile;

import dto.PaymentCardDTO;
import dto.UserSessionDTO;
import entity.PaymentCard;
import entity.User;
import entity.enums.CardType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mapper.PaymentCardMapper;
import org.mapstruct.factory.Mappers;
import service.PaymentCardService;
import service.UserService;
import util.JsonUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@WebServlet("/profile/cards/*")
public class PaymentCardServlet extends HttpServlet {

    private final PaymentCardService paymentCardService = new PaymentCardService();

    /* ══════════════════════════════════════════════════════════════
       GET  /profile/cards/list
       ══════════════════════════════════════════════════════════════ */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        UserSessionDTO sessionUser = getSessionUser(req);
        if (sessionUser == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in.");
            return;
        }

        JsonUtil.writeJson(resp, sessionUser.paymentCards());
    }

    /* ══════════════════════════════════════════════════════════════
       POST  /profile/cards/add
       POST  /profile/cards/remove
       ══════════════════════════════════════════════════════════════ */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        UserSessionDTO sessionUser = getSessionUser(req);
        if (sessionUser == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in.");
            return;
        }

        String path = req.getPathInfo();

        if ("/add".equals(path)) {
            handleAdd(req, resp, sessionUser);
        } else if ("/remove".equals(path)) {
            handleRemove(req, resp, sessionUser);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown action.");
        }
    }

    /* ── ADD ── */
    private void handleAdd(HttpServletRequest req, HttpServletResponse resp,
                           UserSessionDTO sessionUser) throws IOException {
        try {
            PaymentCardDTO newCard = paymentCardService.addCard(
                    sessionUser.id(),
                    param(req, "cardNumber"),
                    param(req, "cardholderName"),
                    param(req, "expiryMonth"),
                    param(req, "expiryYear"),
                    param(req, "cvv"),
                    param(req, "cardType")
            );

            refreshSession(req, sessionUser, newCard, null);
            JsonUtil.writeJson(resp, new AddCardResponse(true, "Card added successfully.", newCard));

        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    /* ── REMOVE ── */
    private void handleRemove(HttpServletRequest req, HttpServletResponse resp,
                              UserSessionDTO sessionUser) throws IOException {
        String cardIdRaw = param(req, "cardId");
        if (cardIdRaw.isBlank()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Card ID is required.");
            return;
        }

        int cardId;
        try {
            cardId = Integer.parseInt(cardIdRaw);
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid card ID.");
            return;
        }

        try {
            paymentCardService.removeCard(cardId, sessionUser.id());
            refreshSession(req, sessionUser, null, cardId);
            JsonUtil.writeJson(resp, new SimpleResponse(true, "Card removed successfully."));

        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    /* ── Rebuild session DTO after add or remove ── */
    private void refreshSession(HttpServletRequest req,
                                UserSessionDTO current,
                                PaymentCardDTO added,
                                Integer removedId) {

        Set<PaymentCardDTO> cards = new HashSet<>(current.paymentCards());

        if (added != null) cards.add(added);
        if (removedId != null) cards.removeIf(c -> c.id().equals(removedId));

        UserSessionDTO updated = new UserSessionDTO(
                current.id(), current.name(), current.email(),
                current.gender(), current.birthday(), current.job(),
                current.address(), current.creditLimit(), current.role(),
                current.interests(), cards
        );

        req.getSession(false).setAttribute("user", updated);
    }

    /* ── Helpers ── */
    private UserSessionDTO getSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object attr = session.getAttribute("user");
        return attr instanceof UserSessionDTO u ? u : null;
    }

    private void sendError(HttpServletResponse resp, int status, String message)
            throws IOException {
        resp.setStatus(status);
        JsonUtil.writeJson(resp, new SimpleResponse(false, message));
    }

    private String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    /* ══════════════════════════════════════════════════════════════
       RESPONSE RECORDS  (Gson serialises these to JSON)
       ══════════════════════════════════════════════════════════════ */

    private record SimpleResponse(boolean success, String message) {
    }

    private record AddCardResponse(boolean success, String message, PaymentCardDTO card) {
    }
}
