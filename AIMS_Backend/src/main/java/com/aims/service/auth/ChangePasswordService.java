package com.aims.service.auth;

import com.aims.dto.auth.ChangePasswordRequest;
import com.aims.entity.user.User;
import com.aims.exception.InvalidPasswordPolicyException;
import com.aims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of {@link IChangePasswordService}.
 *
 * <p><b>Responsibility (SRP)</b>: owns the entire change-password workflow — verification,
 * policy enforcement, persistence, and audit logging — and nothing else.
 * Login, token generation, and user-management concerns live in separate services.
 *
 * <p><b>Loose coupling (DIP)</b>: every collaborator is injected via its interface or
 * abstract type ({@link UserRepository}, {@link PasswordEncoder},
 * {@link PasswordValidationPolicy}).  No concrete type is referenced except for
 * {@link User}, which is a pure domain entity.
 *
 * <p><b>High cohesion</b>: all private helpers directly support the single public
 * method {@link #changePassword}.
 */
@Service
@RequiredArgsConstructor
public class ChangePasswordService implements IChangePasswordService {

    private static final Logger log = LoggerFactory.getLogger(ChangePasswordService.class);

    /** Persistence gateway — interface from Spring Data. */
    private final UserRepository userRepository;

    /** BCrypt (or any configured) encoder — Spring Security abstraction. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Active password policy — injected as the {@link PasswordValidationPolicy} bean.
     * To switch to a different policy, register another bean and qualify it here;
     * no change to this class is necessary (OCP).
     */
    private final PasswordValidationPolicy passwordPolicy;

    // ── Main flow ────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Exception mapping (aligned with use-case spec):
     * <ul>
     *   <li>E1 — current password mismatch  → {@link InvalidPasswordPolicyException}</li>
     *   <li>E2 — confirm password mismatch  → {@link InvalidPasswordPolicyException}</li>
     *   <li>E3 — new password fails policy  → {@link InvalidPasswordPolicyException}</li>
     *   <li>E4 — new == current             → {@link InvalidPasswordPolicyException}</li>
     *   <li>E5 — DB error                   → runtime exception, transaction rolled back</li>
     * </ul>
     */
    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {

        // Step 1 — load user (throws UsernameNotFoundException if not found)
        User user = loadUser(username);

        // Step 2 (E1) — verify current password
        verifyCurrentPassword(request.getCurrentPassword(), user.getPassword(), username);

        // Step 3 (E4) — reject if new password equals current password
        rejectIfSameAsCurrent(request.getCurrentPassword(), request.getNewPassword());

        // Step 4 (E2) — verify confirmation matches
        verifyConfirmation(request.getNewPassword(), request.getConfirmPassword());

        // Step 5 (E3) — validate new password against active policy
        passwordPolicy.validate(request.getNewPassword());

        // Step 6 — hash and persist
        String hashedNew = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedNew);
        userRepository.save(user);

        // Step 7 — audit log (BR2)
        log.info("[AUDIT] Password changed successfully for user='{}'", username);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
    }

    private void verifyCurrentPassword(String supplied, String storedHash, String username) {
        if (!passwordEncoder.matches(supplied, storedHash)) {
            log.warn("[AUDIT] Failed change-password attempt for user='{}': wrong current password", username);
            throw new InvalidPasswordPolicyException("Current password is incorrect.");
        }
    }

    private void rejectIfSameAsCurrent(String currentPassword, String newPassword) {
        // Compare plaintext against plaintext here because we have the supplied current
        // plaintext available; do NOT store or log either value.
        if (currentPassword.equals(newPassword)) {
            throw new InvalidPasswordPolicyException(
                    "New password must be different from the current password.");
        }
    }

    private void verifyConfirmation(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new InvalidPasswordPolicyException(
                    "New password and confirmation do not match.");
        }
    }
}
