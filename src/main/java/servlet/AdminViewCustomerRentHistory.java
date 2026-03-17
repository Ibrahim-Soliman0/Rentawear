package servlet.profile;

import dto.OrderDTO;
import dto.UserSessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.OrderService;
import util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

@WebServlet("/admin/profile/orders")
public class AdminViewCustomerRentHistory extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        try {
            /* getOrdersForUser returns [ activeList, pastList ] */
            List<List<OrderDTO>> split = orderService.getOrdersForUser(Integer.parseInt(req.getParameter("userId")));

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
