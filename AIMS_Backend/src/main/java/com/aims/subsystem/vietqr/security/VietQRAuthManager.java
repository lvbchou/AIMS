package com.aims.subsystem.vietqr.security;

import com.aims.subsystem.vietqr.config.VietQRProperties;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Manager class responsible for processing and validating authentication credentials
 * for both inbound webhook validation and outbound API requests.
 */
@Component
public class VietQRAuthManager implements ICallbackAuthGuard, IApiCredentialProvider {

    private final String partnerUsername;
    private final String partnerPassword;
    private final String partnerTokenSecret;

    // Chain of validators to verify inbound authentication headers
    private final List<Function<String, Boolean>> AUTH_CHAIN;

    public VietQRAuthManager(
            @Value("${vietqr.partner-username:}") String partnerUsername,
            @Value("${vietqr.partner-password:}") String partnerPassword,
            @Value("${vietqr.partner-token-secret:}") String partnerTokenSecret) {
        this.partnerUsername = partnerUsername;
        this.partnerPassword = partnerPassword;
        this.partnerTokenSecret = partnerTokenSecret;

        this.AUTH_CHAIN = List.of(
                this::validateBasicScheme,
                this::validateBearerScheme
        );
    }

    // ── ICallbackAuthGuard (Inbound: VietQR → AIMS) ──────────────

    @Override
    public boolean validate(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return false;
        }
        return AUTH_CHAIN.stream()
                .anyMatch(validator -> validator.apply(authorizationHeader));
    }

    @Override
    public String getTokenSecret() {
        return partnerTokenSecret == null ? "" : partnerTokenSecret;
    }

    // ── IApiCredentialProvider (Outbound: AIMS → VietQR) ─────────

    @Override
    public String buildBasicAuthHeader() {
        String raw = (partnerUsername == null ? "" : partnerUsername)
                + ":"
                + (partnerPassword == null ? "" : partnerPassword);
        return "Basic " + Base64.getEncoder()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    // ── Private Chain Nodes ───────────────────────────────────────

    private boolean validateBasicScheme(String header) {
        if (!header.startsWith("Basic ")) return false;
        try {
            byte[] decoded = Base64.getDecoder()
                    .decode(header.substring("Basic ".length()).trim());
            String creds = new String(decoded, StandardCharsets.UTF_8);
            String expected = (partnerUsername == null ? "" : partnerUsername)
                    + ":" + (partnerPassword == null ? "" : partnerPassword);
            System.out.println("[VietQR Auth] Decoded creds: " + creds + " | Expected: " + expected);
            return creds.equals(expected);
        } catch (IllegalArgumentException ex) {
            System.out.println("[VietQR Auth] Failed to decode Basic header: " + header);
            return false;
        }
    }

    private boolean validateBearerScheme(String header) {
        if (!header.startsWith("Bearer ")) return false;
        String token = header.substring("Bearer ".length()).trim();
        return token.equals(partnerTokenSecret == null ? "" : partnerTokenSecret);
    }
}
