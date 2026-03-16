package servlet;

import dto.CustomerDTO;
import dto.UserInterestDTO;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.UserService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/admin/getCustomers")
public class GetCustomersServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        List<CustomerDTO> customers = userService.getAllCustomer();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for(CustomerDTO c : customers){
            JsonArrayBuilder interestsArray = Json.createArrayBuilder();
            if(c.interests()!=null){
                for(UserInterestDTO interest : c.interests()){
                    interestsArray.add(interest.categoryName());
                }
            }

            JsonObjectBuilder obj = Json.createObjectBuilder()
                    .add("id",          c.id())
                    .add("name",        c.name())
                    .add("email",       c.email())
                    .add("gender",      c.gender() != null ? c.gender().name() : "")
                    .add("job",         c.job()    != null ? c.job()           : "")
                    .add("address",     c.address()!= null ? c.address()       : "")
                    .add("creditLimit", c.creditLimit() != null ? c.creditLimit().toPlainString() : "0")
                    .add("interests",   interestsArray);

            arrayBuilder.add(obj);
        }

        JsonObjectBuilder response = Json.createObjectBuilder();
        response.add("customers",arrayBuilder);

        PrintWriter out = resp.getWriter();
        out.println(response.build().toString());

    }
}
