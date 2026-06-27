// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: Carries only simple primitive fields; all JSON parsing
//   has been moved to PayPalResponseMapper (SRP fix).
// Reason for Cohesion: Solely responsible for holding access-token data.
/**
 * SOLID Principles Analysis (refactored):
 * - **SRP Compliance**: Previously violated SRP by owning an ObjectMapper and
 *   a parseResponse() method. The DTO is now a pure data holder. All parsing
 *   logic lives in {@link PayPalResponseMapper}.
 */
package com.aims.subsystem.paypal;

/**
 * AccessTokenResponse — immutable data holder for a PayPal OAuth2 access-token
 * response.
 *
 * <p>Instances are created exclusively by
 * {@link PayPalResponseMapper#parseAccessToken(String)}.</p>
 */
class AccessTokenResponse {

    private final String accessToken;
    private final long expiresIn;

    /**
     * Constructs a fully populated access-token response.
     *
     * @param accessToken the bearer token string returned by PayPal.
     * @param expiresIn   the token lifetime in seconds.
     */
    AccessTokenResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

    /** @return the bearer access token. */
    String getAccessToken() {
        return accessToken;
    }

    /** @return the token lifetime in seconds. */
    long getExpiresIn() {
        return expiresIn;
    }
}
