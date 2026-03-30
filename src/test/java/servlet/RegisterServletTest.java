package servlet;

import exception.EmailAlreadyExistsException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.CategoryService;
import service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class RegisterServletTest {

    // ── Mocks ─────────────────────────────────────────────────────────────────

    @Mock private HttpServletRequest  request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession         session;
    @Mock private RequestDispatcher   dispatcher;
    @Mock private UserService         userService;
    @Mock private CategoryService     categoryService;

    // ── System under test ─────────────────────────────────────────────────────

    private RegisterServlet servlet;

    @BeforeEach
    void setUp() {
        /*
         * Use the package-private testable constructor so we control
         * userService and categoryService completely.
         */
        servlet = new RegisterServlet(userService, categoryService);

        /*
         * categoryService.getAll() is called on EVERY forward (both the
         * initial GET and every error path in POST). Stub it once here so
         * we never get a NullPointerException from the JSP attribute setter.
         */
        lenient().when(categoryService.getAll()).thenReturn(List.of());

        /*
         * The error path calls req.getRequestDispatcher(...).forward(...).
         * We need getRequestDispatcher to return our mock dispatcher so
         * forward() can be verified.
         */
        lenient().when(request.getRequestDispatcher("/WEB-INF/register.jsp"))
                .thenReturn(dispatcher);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  doGet()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("doGet()")
    class DoGet {

        @Test
        @DisplayName("should forward to register.jsp when there is no active session")
        void doGet_noSession_forwardsToRegisterJsp() throws Exception {
            /*
             * getSession() returns null → no session at all → show the page
             */
            when(request.getSession()).thenReturn(null);

            servlet.doGet(request, response);

            // Categories must be loaded and set as a request attribute
            verify(request).setAttribute(eq("categories"), any());

            // The request must be forwarded to the JSP — NOT redirected
            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
        }

        @Test
        @DisplayName("should forward to register.jsp when session exists but has no user")
        void doGet_sessionWithNoUser_forwardsToRegisterJsp() throws Exception {
            when(request.getSession()).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(null);  // no user in session

            servlet.doGet(request, response);

            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
        }

        @Test
        @DisplayName("should redirect to /home when user is already logged in")
        void doGet_userAlreadyLoggedIn_redirectsToHome() throws Exception {
            when(request.getSession()).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(new Object()); // any non-null value

            servlet.doGet(request, response);

            verify(response).sendRedirect("home");

            // Must NOT load categories or forward — bail out immediately
            verify(request, never()).setAttribute(eq("categories"), any());
            verify(dispatcher, never()).forward(any(), any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  doPost() — required field validation
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("doPost() — required field validation")
    class DoPostValidation {

        /*
         * Helper: stubs all four required fields with valid values.
         * Individual tests can then override one field to null/empty
         * to trigger the specific validation they want to test.
         */
        private void stubValidRequiredFields() {
            when(request.getParameter("fullName")).thenReturn("Ahmed Hassan");
            when(request.getParameter("email")).thenReturn("ahmed@example.com");
            when(request.getParameter("password")).thenReturn("securepass1");
            when(request.getParameter("gender")).thenReturn("MALE");
            when(request.getParameter("dob")).thenReturn(null);
            when(request.getParameter("jobTitle")).thenReturn(null);
            when(request.getParameter("shippingAddress")).thenReturn(null);
            when(request.getParameter("creditLimit")).thenReturn("0");
            when(request.getParameterValues("styleInterests")).thenReturn(null);
        }

        /*
         * Helper: verifies the error path was taken.
         * Checks that:
         *   1. errorMsg attribute was set on the request
         *   2. The request was forwarded to register.jsp (not redirected)
         *   3. userService.register() was NEVER called
         */
        private void assertForwardedWithError(String expectedMessage) throws Exception {
            ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
            verify(request).setAttribute(eq("errorMsg"), errorCaptor.capture());
            assertEquals(expectedMessage, errorCaptor.getValue());

            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
            verify(userService, never()).register(any());
        }

        @Test
        @DisplayName("should forward with error when fullName is missing")
        void doPost_missingFullName_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("fullName")).thenReturn(null);

            servlet.doPost(request, response);

            assertForwardedWithError("Please fill in all required fields.");
        }

        @Test
        @DisplayName("should forward with error when fullName is blank")
        void doPost_blankFullName_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("fullName")).thenReturn("   "); // only spaces

            servlet.doPost(request, response);

            assertForwardedWithError("Please fill in all required fields.");
        }

        @Test
        @DisplayName("should forward with error when email is missing")
        void doPost_missingEmail_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("email")).thenReturn(null);

            servlet.doPost(request, response);

            assertForwardedWithError("Please fill in all required fields.");
        }

        @Test
        @DisplayName("should forward with error when password is missing")
        void doPost_missingPassword_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("password")).thenReturn(null);

            servlet.doPost(request, response);

            assertForwardedWithError("Please fill in all required fields.");
        }

        @Test
        @DisplayName("should forward with error when gender is missing")
        void doPost_missingGender_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("gender")).thenReturn(null);

            servlet.doPost(request, response);

            assertForwardedWithError("Please fill in all required fields.");
        }

        @Test
        @DisplayName("should forward with error when password is shorter than 8 characters")
        void doPost_shortPassword_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("password")).thenReturn("abc123"); // only 6 chars

            servlet.doPost(request, response);

            assertForwardedWithError("Password must be at least 8 characters.");
        }

        @Test
        @DisplayName("should forward with error when date of birth has wrong format")
        void doPost_invalidDobFormat_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("dob")).thenReturn("1995-06-15"); // ISO format, not MM/dd/yyyy

            servlet.doPost(request, response);

            assertForwardedWithError("Invalid date of birth format.");
        }

        @Test
        @DisplayName("should forward with error when credit limit is negative")
        void doPost_negativeCreditLimit_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("creditLimit")).thenReturn("-1");

            servlet.doPost(request, response);

            assertForwardedWithError("Credit limit must be between 0 and 999,999.");
        }

        @Test
        @DisplayName("should forward with error when credit limit exceeds 999,999")
        void doPost_excessiveCreditLimit_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("creditLimit")).thenReturn("1000000");

            servlet.doPost(request, response);

            assertForwardedWithError("Credit limit must be between 0 and 999,999.");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  doPost() — happy path
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("doPost() — happy path")
    class DoPostSuccess {

        @BeforeEach
        void stubValidRequest() {
            /*
             * Stub a fully valid form submission.
             * Every test in this class starts from a valid state.
             */
            when(request.getParameter("fullName")).thenReturn("Ahmed Hassan");
            when(request.getParameter("email")).thenReturn("ahmed@example.com");
            when(request.getParameter("password")).thenReturn("securepass1");
            when(request.getParameter("gender")).thenReturn("MALE");
            when(request.getParameter("dob")).thenReturn(null);
            when(request.getParameter("jobTitle")).thenReturn(null);
            when(request.getParameter("shippingAddress")).thenReturn(null);
            when(request.getParameter("creditLimit")).thenReturn("500");
            when(request.getParameterValues("styleInterests")).thenReturn(null);
            when(request.getContextPath()).thenReturn("");
        }

        @Test
        @DisplayName("should call userService.register() exactly once with valid data")
        void doPost_validData_callsRegisterOnce() throws Exception {
            servlet.doPost(request, response);

            verify(userService, times(1)).register(any());
        }

        @Test
        @DisplayName("should redirect to /login with success message after registration")
        void doPost_validData_redirectsToLogin() throws Exception {
            servlet.doPost(request, response);

            verify(response).sendRedirect(
                    "/login?successMsg=Account+created+successfully.+Please+sign+in.");

            // Must NOT forward to JSP on success
            verify(dispatcher, never()).forward(any(), any());
        }

        @Test
        @DisplayName("should default credit limit to 0 when value is not a valid number")
        void doPost_nonNumericCreditLimit_defaultsToZero() throws Exception {
            when(request.getParameter("creditLimit")).thenReturn("not-a-number");

            /*
             * The servlet catches NumberFormatException and defaults to 0.
             * The registration should still proceed — verify register() is called.
             */
            servlet.doPost(request, response);

            verify(userService, times(1)).register(any());
            verify(response).sendRedirect(anyString());
        }

        @Test
        @DisplayName("should parse styleInterests into a list of integer ids")
        void doPost_withStyleInterests_parsesIdsCorrectly() throws Exception {
            when(request.getParameterValues("styleInterests"))
                    .thenReturn(new String[]{"1", "3", "7"});

            servlet.doPost(request, response);

            /*
             * Capture the DTO that was passed to register() and verify
             * the interests list was parsed correctly.
             */
            ArgumentCaptor<dto.UserRegisterDTO> dtoCaptor =
                    ArgumentCaptor.forClass(dto.UserRegisterDTO.class);
            verify(userService).register(dtoCaptor.capture());

            assertEquals(List.of(1, 3, 7), dtoCaptor.getValue().interests());
        }

        @Test
        @DisplayName("should trim and lowercase the email before registering")
        void doPost_emailWithSpacesAndUppercase_normalised() throws Exception {
            when(request.getParameter("email")).thenReturn("  Ahmed@Example.COM  ");

            servlet.doPost(request, response);

            ArgumentCaptor<dto.UserRegisterDTO> dtoCaptor =
                    ArgumentCaptor.forClass(dto.UserRegisterDTO.class);
            verify(userService).register(dtoCaptor.capture());

            assertEquals("ahmed@example.com", dtoCaptor.getValue().email());
        }

        @Test
        @DisplayName("should parse a valid date of birth in MM/dd/yyyy format")
        void doPost_validDob_parsedCorrectly() throws Exception {
            when(request.getParameter("dob")).thenReturn("06/15/1995");

            servlet.doPost(request, response);

            ArgumentCaptor<dto.UserRegisterDTO> dtoCaptor =
                    ArgumentCaptor.forClass(dto.UserRegisterDTO.class);
            verify(userService).register(dtoCaptor.capture());

            java.time.LocalDate expected = java.time.LocalDate.of(1995, 6, 15);
            assertEquals(expected, dtoCaptor.getValue().birthday());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  doPost() — service exception handling
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("doPost() — service exception handling")
    class DoPostExceptions {

        @BeforeEach
        void stubValidRequest() {
            when(request.getParameter("fullName")).thenReturn("Ahmed Hassan");
            when(request.getParameter("email")).thenReturn("ahmed@example.com");
            when(request.getParameter("password")).thenReturn("securepass1");
            when(request.getParameter("gender")).thenReturn("MALE");
            when(request.getParameter("dob")).thenReturn(null);
            when(request.getParameter("jobTitle")).thenReturn(null);
            when(request.getParameter("shippingAddress")).thenReturn(null);
            when(request.getParameter("creditLimit")).thenReturn("0");
            when(request.getParameterValues("styleInterests")).thenReturn(null);
        }

        @Test
        @DisplayName("should forward with specific error when email is already registered")
        void doPost_emailAlreadyExists_forwardsWithEmailError() throws Exception {
            /*
             * Make userService.register() throw EmailAlreadyExistsException.
             * doThrow() is used instead of when().thenThrow() because register()
             * is a void method.
             */
            doThrow(new EmailAlreadyExistsException("ahmed@example.com"))
                    .when(userService).register(any());

            servlet.doPost(request, response);

            ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
            verify(request).setAttribute(eq("errorMsg"), errorCaptor.capture());
            assertEquals("An account with this email already exists.",
                    errorCaptor.getValue());

            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
        }

        @Test
        @DisplayName("should forward with generic error on unexpected exception")
        void doPost_unexpectedException_forwardsWithGenericError() throws Exception {
            doThrow(new RuntimeException("DB is down"))
                    .when(userService).register(any());

            servlet.doPost(request, response);

            ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
            verify(request).setAttribute(eq("errorMsg"), errorCaptor.capture());
            assertEquals("Something went wrong. Please try again.",
                    errorCaptor.getValue());

            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
        }
    }
}