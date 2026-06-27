package com.aims.subsystem.paypal;

import com.aims.exception.PaymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * PayPalAuthManager — manages the PayPal OAuth2 access-token lifecycle.
 *
 * <h3>Design Rationale</h3>
 * <p><strong>SRP (Single Responsibility Principle):</strong> Previously,
 * {@link PayPalController} owned the token cache ({@code accessToken},
 * {@code tokenExpiryTime}) and the private {@code getAccessToken()} method.
 * This mixed an <em>authentication lifecycle concern</em> with
 * <em>payment orchestration</em> — two distinct reasons to change.
 * Extracting token management here gives each class a single reason to
 * change.</p>
 *
 * <p><strong>Thread safety:</strong> The original fields were plain
 * {@code String} and {@code long} with no synchronisation, creating a race
 * condition under concurrent requests.  This class uses {@code volatile}
 * fields and a {@code synchronized} method to make token refresh atomic.</p>
 *
 * <p><strong>Hollywood Principle:</strong> The controller tells the auth
 * manager to "get a valid token" — it does not know <em>how</em> the token is
 * obtained or cached.</p>
 */
@Component
public class PayPalAuthManager {

    private final PayPalBoundary boundary;
    private final String clientId;
    private final String clientSecret;

    /** The cached bearer token — {@code volatile} for safe cross-thread visibility. */
    private volatile String cachedToken;

    /** The epoch-millisecond instant at which {@link #cachedToken} expires. */
    private volatile long expiryMillis;

    /**
     * Constructs the auth manager with its required collaborators.
     *
     * @param boundary      the PayPal HTTP boundary used to fetch tokens.
     * @param clientId      PayPal OAuth client ID (from {@code application.properties}).
     * @param clientSecret  PayPal OAuth client secret.
     */
    public PayPalAuthManager(
            PayPalBoundary boundary,
            @Value("${paypal.client.id}") String clientId,
            @Value("${paypal.client.secret}") String clientSecret) {
        this.boundary = boundary;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Returns a valid PayPal access token, using the cached one when still
     * fresh or fetching a new one when expired.
     *
     * <p>The method is {@code synchronized} to prevent concurrent threads from
     * triggering simultaneous token-refresh requests against the PayPal OAuth
     * endpoint.  The 10-second buffer before expiry guards against clock skew
     * between the local server and PayPal's authorization servers.</p>
     *
     * @return a non-null bearer token string.
     * @throws PaymentException if the token cannot be fetched or parsed.
     */
    public synchronized String getValidToken() throws PaymentException {
        // Developer Mock Mode: when clientId starts with the mock prefix, skip real HTTP calls.
        // Returns a sentinel token so callers can detect mock mode without knowing clientId.
        if (clientId != null && clientId.startsWith(PayPalMockMode.CLIENT_ID_PREFIX)) {
            return PayPalMockMode.TOKEN_PREFIX + clientId;
        }

        long now = System.currentTimeMillis();

        // Return cached token if it still has more than 10 seconds of life.
        if (cachedToken != null && now < expiryMillis - 10_000) {
            return cachedToken;
        }

        // Token missing or expired — fetch a new one.
        try {
            AccessTokenRequest request = new AccessTokenRequest(clientId, clientSecret);
            String authHeader = request.toAuthorizationHeader();

            String responseJson = boundary.getAccessToken(authHeader);
            AccessTokenResponse response = PayPalResponseMapper.parseAccessToken(responseJson);

            this.cachedToken = response.getAccessToken();
            this.expiryMillis = now + (response.getExpiresIn() * 1_000);

            return this.cachedToken;
        } catch (Exception e) {
            throw new PaymentException("Failed to obtain PayPal access token: " + e.getMessage());
        }
    }
}
