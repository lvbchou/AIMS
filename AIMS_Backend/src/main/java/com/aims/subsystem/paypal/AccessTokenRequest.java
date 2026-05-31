// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: It uses and passes simple types (String) to represent client credentials and encodes them into standard base64 formats without depending on complex object structures.
// Reason for Cohesion: All properties and methods inside this class are strictly focused on a single responsibility: modeling the OAuth2 access token request credentials and generating the Basic authorization header.
package com.aims.subsystem.paypal;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.Builder;
import lombok.Data;

@Data
@Builder

class AccessTokenRequest {

    private final String clientId;
    private final String clientSecret;

    AccessTokenRequest(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    String toAuthorizationHeader() {
        String combined = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(combined.getBytes(StandardCharsets.UTF_8));
    }

    String getClientId() {
        return this.clientId;
    }

    String getClientSecret() {
        return this.clientSecret;
    }

}
