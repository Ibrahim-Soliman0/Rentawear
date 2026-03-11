package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.PaymentCard;
import model.User;
import model.enums.CardType;
import model.enums.Gender;
import model.enums.UserRole;
import service.UserService;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;

@WebServlet("/user")
public class UserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = new User();
        user.setAddress("street");
        user.setGender(Gender.MALE);
        user.setPasswordHash("hash");
        user.setJob("developer");
        user.setCreditLimit(new BigDecimal(55));
        user.setRole(UserRole.USER);
        user.setBirthday(LocalDate.now());
        user.setName("ibrahim");
        user.setEmail("ibrahimsoliman269@gmail.com");

        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardNumber("12345678912345678");
        paymentCard.setCardType(CardType.MASTERCARD);
        paymentCard.setCvv("1234");
        paymentCard.setCardholderName("ibrahim soliman");
        paymentCard.setExpiryMonth("12");
        paymentCard.setExpiryYear("2022");

        user.addPaymentCard(paymentCard);

        UserService userService = new UserService();
        user = userService.save(user);

        User returenedUser = userService.getById(user.getId());
        User returenedUser2 = userService.getUserByEmail("ibrahimsoliman269@gmail.com").get();

        PrintWriter out = resp.getWriter();
        out.println(returenedUser.getName());
        out.println(returenedUser2.getName());

        userService.delete(user);
    }
}
