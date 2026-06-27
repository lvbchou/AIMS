package com.aims.service.auth;

/**
 * Strategy interface for password validation rules.
 *
 * <p>SOLID rationale:
 * <ul>
 *   <li><b>SRP</b> — each implementor owns exactly one cohesive set of rules.</li>
 *   <li><b>OCP</b> — new password policies (e.g. NIST-2024, corporate policy) can be
 *       added by creating new implementations without touching existing code.</li>
 *   <li><b>DIP</b> — {@code ChangePasswordService} depends on this abstraction, not on
 *       any concrete policy class.</li>
 * </ul>
 *
 * <p>Implementations throw {@link com.aims.exception.InvalidPasswordPolicyException}
 * if the supplied password does not satisfy the policy.
 */
public interface PasswordValidationPolicy {

    /**
     * Validates {@code password} against this policy.
     *
     * @param password the plaintext password to validate (never {@code null}).
     * @throws com.aims.exception.InvalidPasswordPolicyException if the policy is violated.
     */
    void validate(String password);
}
