package servlet;

import dto.AdminOrderDTO;
import dto.AdminOrderItemDTO;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.OrderService;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/admin/getOrders")
public class GetOrdersServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        List<AdminOrderDTO> orders = orderService.getAllOrders();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (AdminOrderDTO o : orders) {

            // Build order items array
            JsonArrayBuilder itemsArray = Json.createArrayBuilder();
            if (o.orderItems() != null) {
                for (AdminOrderItemDTO item : o.orderItems()) {
                    itemsArray.add(Json.createObjectBuilder()
                            .add("id",               item.id())
                            .add("productName",      item.productName())
                            .add("color",            item.color()    != null ? item.color()    : "")
                            .add("size",             item.size()     != null ? item.size()     : "")
                            .add("quantity",         item.quantity() != null ? item.quantity() : 0)
                            .add("priceAtPurchase",  item.priceAtPurchase() != null ? item.priceAtPurchase().toPlainString() : "0")
                            .add("startDate",        item.startDate() != null ? item.startDate().format(DATE_FMT) : "")
                            .add("endDate",          item.endDate()   != null ? item.endDate().format(DATE_FMT)   : "")
                    );
                }
            }

            // Format createdAt
            String createdAt = o.createdAt() != null
                    ? o.createdAt().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    : "";

            JsonObjectBuilder obj = Json.createObjectBuilder()
                    .add("id",            o.id())
                    .add("customerName",  o.customerName()  != null ? o.customerName()  : "")
                    .add("customerEmail", o.customerEmail() != null ? o.customerEmail() : "")
                    .add("totalAmount",   o.totalAmount()   != null ? o.totalAmount().toPlainString() : "0")
                    .add("status",        o.status()        != null ? o.status().name() : "")
                    .add("createdAt",     createdAt)
                    .add("orderItems",    itemsArray);

            arrayBuilder.add(obj);
        }

        JsonObjectBuilder response = Json.createObjectBuilder();
        response.add("orders", arrayBuilder);

        PrintWriter out = resp.getWriter();
        out.println(response.build().toString());
    }
}