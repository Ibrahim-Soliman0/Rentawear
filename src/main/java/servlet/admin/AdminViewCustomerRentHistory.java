package servlet.admin;

import dto.OrderDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.OrderService;
import util.JsonUtil;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/profile/orders")
public class AdminViewCustomerRentHistory extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String userIdParam = req.getParameter("userId");

        if (userIdParam == null || userIdParam.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp,
                    new ErrorResponse(false, "Missing userId parameter."));
            return;
        }

        try {
            /* getOrdersForUser returns [ activeList, pastList ] */
            List<List<OrderDTO>> split = orderService.getOrdersForUser(Integer.parseInt(userIdParam));

            JsonUtil.writeJson(resp, new OrdersResponse(split.get(0), split.get(1)));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, new ErrorResponse(false,
                    "Could not load orders: " + e.getMessage()));
        }
    }

    private record OrdersResponse(
            List<OrderDTO> active,
            List<OrderDTO> past
    ) {
    }

    private record ErrorResponse(boolean success, String message) {
    }
}
