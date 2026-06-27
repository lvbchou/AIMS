package com.aims.service.auth;

import com.aims.exception.InvalidPasswordPolicyException;
import org.springframework.stereotype.Component;

/**
 * Concrete {@link PasswordValidationPolicy} that enforces the AIMS strong-password rule set.
 *
 * <p>Assumption A1 — Password must satisfy ALL of the following:
 * <ul>
 *   <li>At least 8 characters long.</li>
 *   <li>Contains at least one uppercase letter (A–Z).</li>
 *   <li>Contains at least one lowercase letter (a–z).</li>
 *   <li>Contains at least one digit (0–9).</li>
 * </ul>
 *
 * <p>SOLID rationale:
 * <ul>
 *   <li><b>SRP</b> — only responsible for the "strong password" rules; logging, persistence,
 *       and notification are in separate classes.</li>
 *   <li><b>OCP</b> — behaviour can be changed by registering a different
 *       {@link PasswordValidationPolicy} bean without altering this class.</li>
 * </ul>
 */
@Component
public class StrongPasswordPolicy implements PasswordValidationPolicy {

    private static final int MIN_LENGTH = 8;

    @Override
    public void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new InvalidPasswordPolicyException(
                    "Password must be at least " + MIN_LENGTH + " characters long.");
        }
        if (!containsUppercase(password)) {
            throw new InvalidPasswordPolicyException(
                    "Password must contain at least one uppercase letter.");
        }
        if (!containsLowercase(password)) {
            throw new InvalidPasswordPolicyException(
                    "Password must contain at least one lowercase letter.");
        }
        if (!containsDigit(password)) {
            throw new InvalidPasswordPolicyException(
                    "Password must contain at least one digit.");
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private boolean containsUppercase(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) return true;
        }
        return false;
    }

    private boolean containsLowercase(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isLowerCase(c)) return true;
        }
        return false;
    }

    private boolean containsDigit(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) return true;
        }
        return false;
    }
}
