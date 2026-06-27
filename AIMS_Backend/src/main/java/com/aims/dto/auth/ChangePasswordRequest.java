package com.aims.dto.auth;

import lombok.Data;

/**
 * DTO carrying the three fields submitted by a user when changing their own password.
 *
 * Design notes:
 *  - Plain Data Object — no behaviour, no coupling to persistence or security layers.
 *  - Keeping this separate from {@link LoginRequest} honours the Interface Segregation
 *    Principle: callers of the change-password endpoint do not need to know about
 *    username (it is extracted server-side from the JWT SecurityContext).
 */
@Data
public class ChangePasswordRequest {

    /** The user's current password (plaintext, verified against the stored hash). */
    private String currentPassword;

    /** The desired new password (must satisfy the active {@code PasswordValidationPolicy}). */
    private String newPassword;

    /** Confirmation value — must equal {@code newPassword} exactly. */
    private String confirmPassword;
}
