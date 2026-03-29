package service;

import dto.UpdateProfileDTO;
import dto.UserRegisterDTO;
import dto.UserSessionDTO;
import entity.User;
import entity.enums.Gender;
import exception.EmailAlreadyExistsException;
import exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.UserRepository;
import util.HashUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // ── Mocks (fake objects we control) ──────────────────────────────────────

    @Mock
    private UserRepository userRepository;          // fake DB — no real database used

    // ── The real object under test ────────────────────────────────────────────

    @InjectMocks
    private UserService userService;                // real UserService, with mock injected

    // ── Shared test data ──────────────────────────────────────────────────────

    private User existingUser;

    @BeforeEach
    void setUp() {
        /*
         * Build a reusable User that most tests can work with.
         * We reset this before every test so one test can't
         * accidentally affect another.
         *
         * NOTE: interests and paymentCards are declared as Set<> on the
         * entity, so we use HashSet — not ArrayList.
         * Gender is non-null in the DB schema, so we set it here to
         * avoid NullPointerExceptions inside the mapper.
         */
        existingUser = new User();
        existingUser.setId(1);
        existingUser.setName("Ahmed Hassan");
        existingUser.setEmail("ahmed@example.com");
        existingUser.setPasswordHash(HashUtil.hashPassword("secret123"));
        existingUser.setGender(Gender.MALE);
        // interests and paymentCards are already initialised to new HashSet<>()
        // inside the User class itself — no need to set them here.
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  register()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("register()")
    class Register {

        private UserRegisterDTO buildDto(String email, String password, List<Integer> interests) {
            /*
             * UserRegisterDTO field order (from the record definition):
             *   id, name, email, passwordHash, gender, birthday, job, address, creditLimit, interests
             *
             * We pass null for optional fields (id, birthday, job, address, creditLimit)
             * and a fixed gender so the mapper never receives a null where the
             * entity column is non-nullable.
             */
            return new UserRegisterDTO(
                    null,               // id — auto-generated, not needed on registration
                    "Ahmed Hassan",     // name
                    email,              // email
                    password,           // passwordHash (raw — hashed inside register())
                    "MALE",             // gender — must not be null (non-nullable DB column)
                    null,               // birthday — optional
                    null,               // job — optional
                    null,               // address — optional
                    BigDecimal.ZERO,    // creditLimit — default 0.00
                    interests           // interests (category IDs)
            );
        }

        @Test
        @DisplayName("should save user when email is not already registered")
        void register_newEmail_savesUser() throws EmailAlreadyExistsException {
            /*
             * ARRANGE
             * ───────
             * Tell the mock: when someone calls findByEmail("new@example.com"),
             * return null — meaning the email does NOT exist yet.
             */
            when(userRepository.findByEmail("new@example.com")).thenReturn(null);

            /*
             * ACT
             * ───
             * Call the real method we want to test.
             */
            userService.register(buildDto("new@example.com", "password123", null));

            /*
             * ASSERT
             * ──────
             * verify() checks that save() was actually called exactly once
             * with ANY User object (we don't care which specific instance).
             * This confirms the user was persisted.
             */
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("should throw EmailAlreadyExistsException when email is taken")
        void register_duplicateEmail_throwsException() {
            /*
             * ARRANGE: make the mock pretend the email already exists
             */
            when(userRepository.findByEmail("ahmed@example.com")).thenReturn(existingUser);

            /*
             * ASSERT + ACT combined:
             * assertThrows() runs the lambda and checks that the expected
             * exception type is thrown. The test FAILS if no exception is thrown.
             */
            assertThrows(
                    EmailAlreadyExistsException.class,
                    () -> userService.register(buildDto("ahmed@example.com", "password123", null))
            );

            /*
             * Also verify save() was NEVER called — we should bail out early.
             */
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("should hash password before saving")
        void register_passwordIsHashed() throws EmailAlreadyExistsException {
            when(userRepository.findByEmail(anyString())).thenReturn(null);

            /*
             * We use a capturing argument to grab the User that was passed to save().
             * Then we can inspect it and verify the password was hashed.
             */
            userService.register(buildDto("new@example.com", "plaintext", null));

            // Capture the User object passed to save()
            org.mockito.ArgumentCaptor<User> captor =
                    org.mockito.ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User savedUser = captor.getValue();

            /*
             * The saved password must NOT equal the raw password.
             * It should also pass verification via HashUtil.
             */
            assertNotEquals("plaintext", savedUser.getPasswordHash(),
                    "Password should not be stored in plain text");

            assertTrue(
                    HashUtil.verifyPassword("plaintext", savedUser.getPasswordHash()),
                    "Stored hash should verify against the original password"
            );
        }

        @Test
        @DisplayName("should not crash when interests list is null")
        void register_nullInterests_noException() {
            when(userRepository.findByEmail(anyString())).thenReturn(null);

            // If interests is null, the code should skip the loop — not throw NPE
            assertDoesNotThrow(
                    () -> userService.register(buildDto("new@example.com", "password123", null))
            );
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  login()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should return UserSessionDTO when credentials are correct")
        void login_validCredentials_returnsSession() {
            /*
             * ARRANGE: the email resolves to our existing user
             */
            when(userRepository.findByEmail("ahmed@example.com")).thenReturn(existingUser);

            /*
             * ACT
             */
            Optional<UserSessionDTO> result = userService.login("ahmed@example.com", "secret123");

            /*
             * ASSERT: we got a non-empty Optional back
             */
            assertTrue(result.isPresent(), "Should return a session DTO for valid credentials");
        }

        @Test
        @DisplayName("should return empty Optional when email is not found")
        void login_unknownEmail_returnsEmpty() {
            when(userRepository.findByEmail("ghost@example.com")).thenReturn(null);

            Optional<UserSessionDTO> result = userService.login("ghost@example.com", "anything");

            assertTrue(result.isEmpty(), "Should return empty Optional for unknown email");
        }

        @Test
        @DisplayName("should return empty Optional when password is wrong")
        void login_wrongPassword_returnsEmpty() {
            when(userRepository.findByEmail("ahmed@example.com")).thenReturn(existingUser);

            Optional<UserSessionDTO> result = userService.login("ahmed@example.com", "wrongpassword");

            assertTrue(result.isEmpty(), "Should return empty Optional for wrong password");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  updateUserSession()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateUserSession()")
    class UpdateUserSession {

        @Test
        @DisplayName("should return session DTO for existing user")
        void updateUserSession_existingId_returnsDto() {
            /*
             * getById() is inherited from BaseService which calls repository.findById().
             * We mock findById on the repository here.
             */
            when(userRepository.findById(1)).thenReturn(existingUser);

            UserSessionDTO result = assertDoesNotThrow(
                    () -> userService.updateUserSession(1)
            );

            assertNotNull(result, "Should return a non-null session DTO");
        }

        @Test
        @DisplayName("should throw UserNotFoundException for unknown userId")
        void updateUserSession_unknownId_throwsException() {
            when(userRepository.findById(999)).thenReturn(null);

            assertThrows(
                    UserNotFoundException.class,
                    () -> userService.updateUserSession(999)
            );
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  updateProfile()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfile {

        /*
         * Helper: builds a fully valid UpdateProfileDTO.
         *
         * Inferred field order from how updateProfile() reads the DTO:
         *   name, email, birthday, job, address, interests,
         *   currentPassword, newPassword, confirmPassword
         */
        private UpdateProfileDTO validDto() {
            return new UpdateProfileDTO(
                    "Ahmed Hassan Updated",   // name
                    "ahmed@example.com",      // email — same as existingUser, skips uniqueness check
                    null,                     // birthday — optional, null clears it
                    "Software Engineer",      // job
                    "Cairo, Egypt",           // address
                    Set.of(),                // interests — empty, no category lookups needed
                    null,                     // currentPassword — no password change
                    null,                     // newPassword
                    null                      // confirmPassword
            );
        }

        @BeforeEach
        void arrangeRepositoryForUpdateTests() {
            /*
             * Most update tests need the user to be found by ID.
             * We set this up once for the whole Nested class.
             * Individual tests can override it if needed.
             */
            when(userRepository.findById(1)).thenReturn(existingUser);
        }

        @Test
        @DisplayName("should return session DTO after a valid update")
        void updateProfile_validData_returnsDto() {
            when(userRepository.save(any(User.class))).thenReturn(existingUser);
            UserSessionDTO result = assertDoesNotThrow(
                    () -> userService.updateProfile(1, validDto())
            );
            assertNotNull(result);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when name is blank")
        void updateProfile_blankName_throwsException() {
            UpdateProfileDTO dto = new UpdateProfileDTO(
                    "",                    // blank name ← triggers the guard
                    "ahmed@example.com",   // email
                    null,                  // birthday
                    null,                  // job
                    null,                  // address
                    null,                  // interests
                    null,                  // currentPassword
                    null,                  // newPassword
                    null                   // confirmPassword
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateProfile(1, dto)
            );

            assertEquals("Full name is required.", ex.getMessage());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when email is blank")
        void updateProfile_blankEmail_throwsException() {
            UpdateProfileDTO dto = new UpdateProfileDTO(
                    "Ahmed Hassan",        // name
                    "",                    // blank email ← triggers the guard
                    null,                  // birthday
                    null,                  // job
                    null,                  // address
                    null,                  // interests
                    null,                  // currentPassword
                    null,                  // newPassword
                    null                   // confirmPassword
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateProfile(1, dto)
            );

            assertEquals("Email address is required.", ex.getMessage());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when new email is already taken")
        void updateProfile_emailTakenByAnotherUser_throwsException() {
            User otherUser = new User();
            otherUser.setId(2);
            otherUser.setEmail("taken@example.com");

            /*
             * The NEW email resolves to a DIFFERENT user → conflict
             */
            when(userRepository.findByEmail("taken@example.com")).thenReturn(otherUser);

            UpdateProfileDTO dto = new UpdateProfileDTO(
                    "Ahmed Hassan",        // name
                    "taken@example.com",   // new email that belongs to someone else
                    null,                  // birthday
                    null,                  // job
                    null,                  // address
                    null,                  // interests
                    null,                  // currentPassword
                    null,                  // newPassword
                    null                   // confirmPassword
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateProfile(1, dto)
            );

            assertEquals("That email address is already in use.", ex.getMessage());
        }

        @Test
        @DisplayName("should not check email uniqueness when email is unchanged")
        void updateProfile_sameEmail_noUniqueCheck() {
            /*
             * User keeps the same email → the uniqueness check is skipped entirely.
             * We verify findByEmail is NEVER called for the same address.
             */
            when(userRepository.save(any(User.class))).thenReturn(existingUser);
            assertDoesNotThrow(() -> userService.updateProfile(1, validDto()));

            verify(userRepository, never()).findByEmail("ahmed@example.com");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid birthday format")
        void updateProfile_invalidBirthday_throwsException() {
            UpdateProfileDTO dto = new UpdateProfileDTO(
                    "Ahmed Hassan",        // name
                    "ahmed@example.com",   // email
                    "not-a-date",          // birthday ← invalid format, triggers DateTimeParseException
                    null,                  // job
                    null,                  // address
                    null,                  // interests
                    null,                  // currentPassword
                    null,                  // newPassword
                    null                   // confirmPassword
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateProfile(1, dto)
            );

            assertEquals("Invalid date of birth format.", ex.getMessage());
        }

        @Test
        @DisplayName("should throw when new password is too short")
        void updateProfile_shortNewPassword_throwsException() {
            UpdateProfileDTO dto = new UpdateProfileDTO(
                    "Ahmed Hassan",        // name
                    "ahmed@example.com",   // email
                    null,                  // birthday
                    null,                  // job
                    null,                  // address
                    null,                  // interests
                    "secret123",           // currentPassword (correct)
                    "abc",                 // newPassword — only 3 chars ← too short
                    "abc"                  // confirmPassword
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateProfile(1, dto)
            );

            assertEquals("New password must be at least 8 characters.", ex.getMessage());
        }

        @Test
        @DisplayName("should throw when new passwords do not match")
        void updateProfile_passwordMismatch_throwsException() {
            UpdateProfileDTO dto = new UpdateProfileDTO(
                    "Ahmed Hassan",        // name
                    "ahmed@example.com",   // email
                    null,                  // birthday
                    null,                  // job
                    null,                  // address
                    null,                  // interests
                    "secret123",           // currentPassword
                    "newpassword1",        // newPassword
                    "newpassword2"         // confirmPassword ← different!
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateProfile(1, dto)
            );

            assertEquals("New passwords do not match.", ex.getMessage());
        }

        @Test
        @DisplayName("should throw when current password is missing during password change")
        void updateProfile_missingCurrentPassword_throwsException() {
            UpdateProfileDTO dto = new UpdateProfileDTO(
                    "Ahmed Hassan",        // name
                    "ahmed@example.com",   // email
                    null,                  // birthday
                    null,                  // job
                    null,                  // address
                    null,                  // interests
                    null,                  // currentPassword ← missing
                    "newpassword1",        // newPassword provided
                    "newpassword1"         // confirmPassword
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateProfile(1, dto)
            );

            assertEquals("Current password is required to set a new one.", ex.getMessage());
        }

        @Test
        @DisplayName("should throw when current password is wrong")
        void updateProfile_wrongCurrentPassword_throwsException() {
            UpdateProfileDTO dto = new UpdateProfileDTO(
                    "Ahmed Hassan",        // name
                    "ahmed@example.com",   // email
                    null,                  // birthday
                    null,                  // job
                    null,                  // address
                    null,                  // interests
                    "wrongpassword",       // currentPassword ← incorrect
                    "newpassword1",        // newPassword
                    "newpassword1"         // confirmPassword
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateProfile(1, dto)
            );

            assertEquals("Current password is incorrect.", ex.getMessage());
        }

        @Test
        @DisplayName("should update password when all password fields are valid")
        void updateProfile_validPasswordChange_hashesNewPassword(){
            when(userRepository.save(any(User.class))).thenReturn(existingUser);
            UpdateProfileDTO dto = new UpdateProfileDTO(
                    "Ahmed Hassan",        // name
                    "ahmed@example.com",   // email
                    null,                  // birthday
                    null,                  // job
                    null,                  // address
                    null,                  // interests
                    "secret123",           // currentPassword — correct
                    "newpassword1",        // newPassword — valid (8+ chars)
                    "newpassword1"         // confirmPassword — matches
            );

            userService.updateProfile(1, dto);

            // Capture what was saved
            org.mockito.ArgumentCaptor<User> captor =
                    org.mockito.ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User saved = captor.getValue();

            // The new hash should verify against the new password
            assertTrue(
                    HashUtil.verifyPassword("newpassword1", saved.getPasswordHash()),
                    "Saved hash should match the new password"
            );
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getUserByEmail()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getUserByEmail()")
    class GetUserByEmail {

        @Test
        @DisplayName("should return Optional with user for a known email")
        void getUserByEmail_knownEmail_returnsUser() {
            when(userRepository.findByEmail("ahmed@example.com")).thenReturn(existingUser);

            Optional<User> result = userService.getUserByEmail("ahmed@example.com");

            assertTrue(result.isPresent());
            assertEquals("ahmed@example.com", result.get().getEmail());
        }

        @Test
        @DisplayName("should return empty Optional for an unknown email")
        void getUserByEmail_unknownEmail_returnsEmpty() {
            when(userRepository.findByEmail("nobody@example.com")).thenReturn(null);

            Optional<User> result = userService.getUserByEmail("nobody@example.com");

            assertTrue(result.isEmpty());
        }
    }
}