package servlet;

import exception.EmailAlreadyExistsException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Category;
import model.User;
import model.enums.Gender;
import service.CategoryService;
import service.UserService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if(session !=null && session.getAttribute("user")!=null ) {
            resp.sendRedirect("index.jsp");
            return;
        }
        req.setAttribute("categories", categoryService.getAll());
        req.getRequestDispatcher("/register.jsp").forward(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String fullName         = req.getParameter("fullName");
        String email            = req.getParameter("email");
        String password         = req.getParameter("password");
        String gender           = req.getParameter("gender");
        String dobStr           = req.getParameter("dob");
        String jobTitle         = req.getParameter("jobTitle");
        String shippingAddress  = req.getParameter("shippingAddress");
        String creditLimitStr   = req.getParameter("creditLimit");
        String[] styleInterests = req.getParameterValues("styleInterests");

        // Server-side validation
        if (isEmpty(fullName) || isEmpty(email) || isEmpty(password) || isEmpty(gender)) {
            forwardWithError(req, resp, "Please fill in all required fields.");
        }
        if(password.length()<8){
            forwardWithError(req,resp,"Password must be at leats 8 characters.");
        }

        // Build User object
        User user = new User();
        user.setName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(password);
        user.setGender(Gender.valueOf(gender));
        user.setJob(jobTitle != null ? jobTitle.trim() : null);
        user.setAddress(shippingAddress != null ? shippingAddress.trim() : null);

        // Parse date of birth
        if (!isEmpty(dobStr)) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                user.setBirthday(LocalDate.parse(dobStr, fmt));
            } catch (DateTimeParseException e) {
                forwardWithError(req, resp, "Invalid date of birth format.");
            }
        }

        // Parse credit limit
        try {
            BigDecimal creditLimit = new BigDecimal(creditLimitStr);
            if (creditLimit.compareTo(BigDecimal.ZERO) < 0 ||
                    creditLimit.compareTo(new BigDecimal("999999")) > 0) {
                forwardWithError(req, resp, "Credit limit must be between 0 and 999,999.");
                return;
            }
            user.setCreditLimit(creditLimit);
        } catch (NumberFormatException e) {
            user.setCreditLimit(BigDecimal.ZERO);
        }

        if(styleInterests != null){
            for(String categoryId : styleInterests){
               Category category = categoryService.getById(Integer.parseInt(categoryId));
               if(category!=null){
                   user.addInterest(category);
               }
            }
        }

        // Register via service
        try {
            userService.register(user);
            resp.sendRedirect(req.getContextPath() +
                    "/login.jsp?success=Account+created+successfully.+Please+sign+in.");

        } catch (EmailAlreadyExistsException e) {
            forwardWithError(req, resp, "An account with this email already exists,.");

        } catch (Exception e) {
            forwardWithError(req, resp, "Something went wrong. Please try again.");
        }

    }
    private boolean isEmpty(String val) {
        return val == null || val.trim().isEmpty();
    }
    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("errorMsg", message);
        req.setAttribute("categories", categoryService.getAll());
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }
}
