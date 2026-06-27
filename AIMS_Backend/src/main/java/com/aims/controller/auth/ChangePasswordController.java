package com.aims.controller.auth;

import com.aims.dto.auth.ChangePasswordRequest;
import com.aims.dto.common.ApiResponse;
import com.aims.exception.InvalidPasswordPolicyException;
import com.aims.service.auth.IChangePasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoint for the "Change Password" use case (UC-CP).
 *
 * <p><b>Security</b>:
 * <ul>
 *   <li>The route {@code /api/auth/change-password} is reachable only with a valid JWT
 *       (enforced by the existing {@code JwtAuthFilter} + Spring Security chain).</li>
 *   <li>The authenticated username is extracted from the {@code SecurityContext} via
 *       {@link AuthenticationPrincipal} — the client never supplies the username in the
 *       request body, eliminating horizontal-privilege-escalation risk.</li>
 * </ul>
 *
 * <p><b>SOLID</b>:
 * <ul>
 *   <li><b>SRP</b> — only maps HTTP in/out; all business logic lives in
 *       {@link IChangePasswordService}.</li>
 *   <li><b>DIP</b> — depends on {@link IChangePasswordService} interface, not
 *       the concrete {@code ChangePasswordService}.</li>
 * </ul>
 *
 * <p><b>No existing code was modified</b> — this is a brand-new controller.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ChangePasswordController {

    private final IChangePasswordService changePasswordService;

    /**
     * Changes the authenticated user's own password.
     *
     * <p>HTTP 200 on success; HTTP 400 for any E1–E4 policy violation.
     *
     * @param userDetails Spring Security principal extracted from the JWT.
     * @param request     the three-field payload (currentPassword, newPassword, confirmPassword).
     * @return a standard {@link ApiResponse} wrapper.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangePasswordRequest request) {

        String username = userDetails.getUsername();
        changePasswordService.changePassword(username, request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Password changed successfully.", null));
    }

    /**
     * Handles policy violations (E1–E4) locally so they produce HTTP 400
     * instead of falling through to the generic 500 handler in
     * {@link com.aims.exception.GlobalExceptionHandler}.
     */
    @ExceptionHandler(InvalidPasswordPolicyException.class)
    public ResponseEntity<ApiResponse<Void>> handlePolicyViolation(
            InvalidPasswordPolicyException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
}
