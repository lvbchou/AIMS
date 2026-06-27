package com.aims.exception;

/**
 * Thrown when a submitted password violates the active {@code PasswordValidationPolicy}.
 *
 * Examples: password too short, missing required character class, new password
 * identical to the current one, or confirmation does not match new password.
 *
 * Mapped to HTTP 400 Bad Request by {@link GlobalExceptionHandler}.
 */
public class InvalidPasswordPolicyException extends RuntimeException {

    public InvalidPasswordPolicyException(String message) {
        super(message);
    }
}
