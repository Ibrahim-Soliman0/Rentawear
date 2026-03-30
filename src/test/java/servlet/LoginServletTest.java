package servlet;

import dto.UserSessionDTO;
import entity.enums.UserRole;
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
import service.UserService;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LoginServletTest {

    // ── Mocks ─────────────────────────────────────────────────────────────────

    @Mock private HttpServletRequest  request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession         session;
    @Mock private RequestDispatcher   dispatcher;
    @Mock private UserService         userService;

    // ── System under test ─────────────────────────────────────────────────────

    private LoginServlet servlet;

    @BeforeEach
    void setUp() {
        /*
         * Use the package-private testable constructor so we control
         * userService completely.
         */
        servlet = new LoginServlet(userService);

        /*
         * The error path calls req.getRequestDispatcher(...).forward(...).
         * We need getRequestDispatcher to return our mock dispatcher so
         * forward() can be verified.
         */
        lenient().when(request.getRequestDispatcher("/WEB-INF/login.jsp"))
                .thenReturn(dispatcher);
    }

    /*
     * Helper: creates a UserSessionDTO with sensible defaults for testing.
     */
    private UserSessionDTO createUserSessionDTO(Integer id, String email, UserRole role) {
        return new UserSessionDTO(
                id,
                "Ahmed Hassan",
                email,
                null,                    // gender
                null,                    // birthday
                null,                    // job
                null,                    // address
                BigDecimal.ZERO,         // creditLimit
                role,
                Set.of(),               // interests
                Set.of()                // paymentCards
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  doGet()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("doGet()")
    class DoGet {

        @Test
        @DisplayName("should forward to login.jsp when there is no active session")
        void doGet_noSession_forwardsToLoginJsp() throws Exception {
            /*
             * getSession(false) returns null → no session at all → show the page
             */
            when(request.getSession(false)).thenReturn(null);

            servlet.doGet(request, response);

            // The request must be forwarded to the JSP — NOT redirected
            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
        }

        @Test
        @DisplayName("should forward to login.jsp when session exists but has no user")
        void doGet_sessionWithNoUser_forwardsToLoginJsp() throws Exception {
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(null);  // no user in session

            servlet.doGet(request, response);

            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
        }

        @Test
        @DisplayName("should redirect to /home when user is already logged in")
        void doGet_userAlreadyLoggedIn_redirectsToHome() throws Exception {
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(new Object()); // any non-null value

            servlet.doGet(request, response);

            verify(response).sendRedirect("home");

            // Must NOT forward — bail out immediately
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
         * Helper: stubs both required fields with valid values.
         * Individual tests can then override one field to null/empty
         * to trigger the specific validation they want to test.
         */
        private void stubValidRequiredFields() {
            when(request.getParameter("email")).thenReturn("user@example.com");
            when(request.getParameter("password")).thenReturn("password123");
            when(request.getParameter("rememberMe")).thenReturn(null);
        }

        /*
         * Helper: verifies the error path was taken.
         * Checks that:
         *   1. errorMsg attribute was set on the request
         *   2. The request was forwarded to login.jsp (not redirected)
         *   3. userService.login() was NEVER called
         */
        private void assertForwardedWithError(String expectedMessage) throws Exception {
            ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
            verify(request).setAttribute(eq("errorMsg"), errorCaptor.capture());
            assertEquals(expectedMessage, errorCaptor.getValue());

            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
            verify(userService, never()).login(anyString(), anyString());
        }

        @Test
        @DisplayName("should forward with error when email is missing")
        void doPost_missingEmail_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("email")).thenReturn(null);

            servlet.doPost(request, response);

            assertForwardedWithError("Please enter your email and password.");
        }

        @Test
        @DisplayName("should forward with error when email is blank")
        void doPost_blankEmail_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("email")).thenReturn("   "); // only spaces

            servlet.doPost(request, response);

            assertForwardedWithError("Please enter your email and password.");
        }

        @Test
        @DisplayName("should forward with error when password is missing")
        void doPost_missingPassword_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("password")).thenReturn(null);

            servlet.doPost(request, response);

            assertForwardedWithError("Please enter your email and password.");
        }

        @Test
        @DisplayName("should forward with error when password is blank")
        void doPost_blankPassword_forwardsWithError() throws Exception {
            stubValidRequiredFields();
            when(request.getParameter("password")).thenReturn("   "); // only spaces

            servlet.doPost(request, response);

            assertForwardedWithError("Please enter your email and password.");
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
            when(request.getParameter("email")).thenReturn("user@example.com");
            when(request.getParameter("password")).thenReturn("password123");
            when(request.getParameter("rememberMe")).thenReturn(null);
            when(request.getContextPath()).thenReturn("");
        }

        @Test
        @DisplayName("should call userService.login() exactly once with normalized credentials")
        void doPost_validData_callsLoginOnce() throws Exception {
            when(userService.login(anyString(), anyString()))
                    .thenReturn(Optional.of(createUserSessionDTO(1, "user@example.com", UserRole.USER)));
            when(request.getSession(true)).thenReturn(session);

            servlet.doPost(request, response);

            verify(userService, times(1)).login("user@example.com", "password123");
        }

        @Test
        @DisplayName("should trim and lowercase the email before login")
        void doPost_emailWithSpacesAndUppercase_normalised() throws Exception {
            when(request.getParameter("email")).thenReturn("  User@Example.COM  ");
            when(userService.login(anyString(), anyString()))
                    .thenReturn(Optional.of(createUserSessionDTO(1, "user@example.com", UserRole.USER)));
            when(request.getSession(true)).thenReturn(session);

            servlet.doPost(request, response);

            verify(userService).login("user@example.com", "password123");
        }

        @Test
        @DisplayName("should redirect to /home for regular USER role")
        void doPost_userRole_redirectsToHome() throws Exception {
            UserSessionDTO userDto = createUserSessionDTO(1, "user@example.com", UserRole.USER);
            when(userService.login(anyString(), anyString())).thenReturn(Optional.of(userDto));
            when(request.getSession(true)).thenReturn(session);

            servlet.doPost(request, response);

            verify(session).setAttribute("user", userDto);
            verify(response).sendRedirect("/home");
            verify(response, never()).sendRedirect(contains("/admin"));
        }

        @Test
        @DisplayName("should redirect to /admin/dashboard for ADMIN role")
        void doPost_adminRole_redirectsToAdminDashboard() throws Exception {
            UserSessionDTO adminDto = createUserSessionDTO(2, "admin@example.com", UserRole.ADMIN);
            when(userService.login(anyString(), anyString())).thenReturn(Optional.of(adminDto));
            when(request.getSession(true)).thenReturn(session);

            servlet.doPost(request, response);

            verify(session).setAttribute("user", adminDto);
            verify(response).sendRedirect("/admin/dashboard");
        }

        @Test
        @DisplayName("should set user in session on successful login")
        void doPost_validData_setsUserInSession() throws Exception {
            UserSessionDTO userDto = createUserSessionDTO(1, "user@example.com", UserRole.USER);
            when(userService.login(anyString(), anyString())).thenReturn(Optional.of(userDto));
            when(request.getSession(true)).thenReturn(session);

            servlet.doPost(request, response);

            verify(session).setAttribute("user", userDto);
            verify(dispatcher, never()).forward(any(), any());
        }

        @Test
        @DisplayName("should set session timeout to 24 hours when remember-me is not checked")
        void doPost_noRememberMe_defaultSessionTimeout() throws Exception {
            when(request.getParameter("rememberMe")).thenReturn(null);
            UserSessionDTO userDto = createUserSessionDTO(1, "user@example.com", UserRole.USER);
            when(userService.login(anyString(), anyString())).thenReturn(Optional.of(userDto));
            when(request.getSession(true)).thenReturn(session);

            servlet.doPost(request, response);

            // 60 * 60 * 24 = 86400 seconds (24 hours)
            verify(session).setMaxInactiveInterval(86400);
        }

        @Test
        @DisplayName("should set session timeout to 30 days when remember-me is checked")
        void doPost_withRememberMe_extendedSessionTimeout() throws Exception {
            when(request.getParameter("rememberMe")).thenReturn("on");
            UserSessionDTO userDto = createUserSessionDTO(1, "user@example.com", UserRole.USER);
            when(userService.login(anyString(), anyString())).thenReturn(Optional.of(userDto));
            when(request.getSession(true)).thenReturn(session);

            servlet.doPost(request, response);

            // 60 * 60 * 24 * 30 = 2592000 seconds (30 days)
            verify(session).setMaxInactiveInterval(2592000);
        }

        @Test
        @DisplayName("should not redirect when remember-me has unexpected value")
        void doPost_rememberMeUnexpectedValue_treatsAsNotChecked() throws Exception {
            when(request.getParameter("rememberMe")).thenReturn("yes"); // not "on"
            UserSessionDTO userDto = createUserSessionDTO(1, "user@example.com", UserRole.USER);
            when(userService.login(anyString(), anyString())).thenReturn(Optional.of(userDto));
            when(request.getSession(true)).thenReturn(session);

            servlet.doPost(request, response);

            // Should use default 24-hour timeout, not 30-day
            verify(session).setMaxInactiveInterval(86400);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  doPost() — invalid credentials
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("doPost() — invalid credentials")
    class DoPostInvalidCredentials {

        @BeforeEach
        void stubValidRequest() {
            when(request.getParameter("email")).thenReturn("user@example.com");
            when(request.getParameter("password")).thenReturn("wrongpassword");
            when(request.getParameter("rememberMe")).thenReturn(null);
        }

        @Test
        @DisplayName("should forward with error when credentials are invalid")
        void doPost_invalidCredentials_forwardsWithError() throws Exception {
            when(userService.login(anyString(), anyString())).thenReturn(Optional.empty());

            servlet.doPost(request, response);

            ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
            verify(request).setAttribute(eq("errorMsg"), errorCaptor.capture());
            assertEquals("Invalid email or password.", errorCaptor.getValue());

            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
        }

        @Test
        @DisplayName("should prefill email on failed login attempt")
        void doPost_invalidCredentials_prefillsEmail() throws Exception {
            when(request.getParameter("email")).thenReturn("  user@example.com  ");
            when(userService.login(anyString(), anyString())).thenReturn(Optional.empty());

            servlet.doPost(request, response);

            ArgumentCaptor<String> prefillCaptor = ArgumentCaptor.forClass(String.class);
            verify(request).setAttribute(eq("prefillEmail"), prefillCaptor.capture());
            assertEquals("  user@example.com  ", prefillCaptor.getValue());
        }

        @Test
        @DisplayName("should not create session on failed login")
        void doPost_invalidCredentials_noSession() throws Exception {
            when(userService.login(anyString(), anyString())).thenReturn(Optional.empty());

            servlet.doPost(request, response);

            // getSession(true) should never be called on failed login
            verify(request, never()).getSession(true);
        }
    }
}




