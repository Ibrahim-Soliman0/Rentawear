package servlet.profile;

import dto.UpdateProfileDTO;
import dto.UserSessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.UserService;
import util.JsonUtil;

import java.io.IOException;

@WebServlet("/profile/update")
public class UpdateProfileServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /* ── Auth guard ── */
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in.");
            return;
        }

        UserSessionDTO sessionUser = (UserSessionDTO) session.getAttribute("user");

        /* ── Parse JSON body ── */
        UpdateProfileDTO dto;
        try {
            dto = JsonUtil.fromJson(req, UpdateProfileDTO.class);
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body.");
            return;
        }

        if (dto == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Empty request body.");
            return;
        }

        /* ── Update via service ── */
        try {
            UserSessionDTO updated = userService.updateProfile(sessionUser.id(), dto);

            /* Refresh session with updated DTO */
            session.setAttribute("user", updated);

            JsonUtil.writeJson(resp,
                    new SuccessResponse(true, "Profile updated successfully."));

        } catch (IllegalArgumentException e) {
            /* Validation or business rule failure — send message back to JS */
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message)
            throws IOException {
        resp.setStatus(status);
        JsonUtil.writeJson(resp, new ErrorResponse(false, message));
    }

    private record SuccessResponse(boolean success, String message) {}
    private record ErrorResponse(boolean success, String message) {}
}
