package exception;

public class EmailAlreadyExistsException extends RuntimeException {

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super("An account with this email already exists: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}