package com.aims.service.auth;

import com.aims.dto.auth.ChangePasswordRequest;

/**
 * Service contract for the "Change Password" use case (UC-CP).
 *
 * <p>Actors: Administrator, Product Manager (any authenticated user with at least one role).
 *
 * <p>SOLID rationale:
 * <ul>
 *   <li><b>ISP</b> — deliberately kept separate from {@link IAuthService}; callers that
 *       only need login functionality do not depend on change-password behaviour.</li>
 *   <li><b>DIP</b> — controllers depend on this abstraction, never on the concrete
 *       {@link ChangePasswordService}.</li>
 * </ul>
 */
public interface IChangePasswordService {

    /**
     * Changes the password for the authenticated user identified by {@code username}.
     *
     * <p>The implementation must:
     * <ol>
     *   <li>Verify {@code request.currentPassword} against the stored hash.</li>
     *   <li>Reject if {@code newPassword} equals {@code currentPassword} (E4).</li>
     *   <li>Verify {@code newPassword == confirmPassword} (E2).</li>
     *   <li>Validate {@code newPassword} against the active {@link PasswordValidationPolicy} (E3).</li>
     *   <li>Hash and persist the new password.</li>
     *   <li>Log the event.</li>
     * </ol>
     *
     * @param username the authenticated user's username (sourced from JWT SecurityContext).
     * @param request  the three-field payload.
     * @throws com.aims.exception.InvalidPasswordPolicyException for E1/E2/E3/E4 violations.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the user does not exist.
     */
    void changePassword(String username, ChangePasswordRequest request);
}
