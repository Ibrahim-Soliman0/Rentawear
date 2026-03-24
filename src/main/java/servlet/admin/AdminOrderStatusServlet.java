package servlet.admin;

import entity.enums.OrderStatus;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.OrderService;
import util.JsonUtil;

import java.io.IOException;

@WebServlet("/admin/orders/*")
public class AdminOrderStatusServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing order id");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length < 3 || !"status".equalsIgnoreCase(parts[2])) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        Integer orderId;
        try {
            orderId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order id");
            return;
        }

        UpdateStatusRequest payload = JsonUtil.fromJson(req, UpdateStatusRequest.class);
        if (payload == null || payload.status() == null || payload.status().isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing status");
            return;
        }

        OrderStatus status;
        try {
            status = OrderStatus.valueOf(payload.status().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid status");
            return;
        }

        boolean updated = orderService.updateOrderStatus(orderId, status);
        if (!updated) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
            return;
        }

        JsonUtil.writeJson(resp, new StatusResponse(orderId, status.name()));
    }

    record UpdateStatusRequest(String status) {}
    record StatusResponse(Integer orderId, String status) {}
}
