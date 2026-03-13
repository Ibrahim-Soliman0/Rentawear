package util;

import org.mindrot.jbcrypt.BCrypt;

public class HashUtil {

    // Hash a plain password (used during registration)
    public String hashPassword(String plainPassword) {
        // The number 12 is the "cost factor" (higher = slower but more secure)
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Verify a password against the stored hash (used during login)
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}

